package com.fernirx.sneakerapi.payment.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.order.config.OrderProperties;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.enums.OrderPaymentStatus;
import com.fernirx.sneakerapi.order.enums.OrderStatus;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import com.fernirx.sneakerapi.order.service.OrderService;
import com.fernirx.sneakerapi.payment.dto.request.PaymentFilterRequest;
import com.fernirx.sneakerapi.payment.dto.request.BuildPaymentUrlRequest;
import com.fernirx.sneakerapi.payment.dto.response.PaymentInternalResponse;
import com.fernirx.sneakerapi.payment.entity.Payment;
import com.fernirx.sneakerapi.payment.enums.PaymentStatus;
import com.fernirx.sneakerapi.payment.mapper.PaymentMapper;
import com.fernirx.sneakerapi.payment.provider.PaymentProvider;
import com.fernirx.sneakerapi.payment.repository.PaymentRepository;
import com.fernirx.sneakerapi.payment.repository.PaymentSpec;
import com.fernirx.sneakerapi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentProvider paymentProvider;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;
    private final OrderProperties orderProperties;

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng.
     * Các bước kiểm tra trước khi sinh URL:
     * 1. Đơn hàng phải do chính user (hoặc guest) này sở hữu.
     * 2. Đơn hàng phải chọn phương thức VNPAY, đang ở trạng thái PENDING và chưa thanh toán.
     * 3. Đơn hàng chưa quá hạn thanh toán.
     * 4. Số lần thanh toán thất bại trước đó chưa vượt ngưỡng cho phép (tránh spam).
     */
    @Override
    public String create(Long orderId, Long userId, String guestToken, String ipAddress) {
        Order order = orderService.findOwnedEntityById(orderId, userId, guestToken);

        if (order.getPaymentMethod() != PaymentMethod.VNPAY
                || order.getStatus() != OrderStatus.PENDING
                || order.getPaymentStatus() != OrderPaymentStatus.UNPAID) {
            throw BusinessException.bad("label.payment");
        }
        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw BusinessException.expired("label.order");
        }
        if (paymentRepository.countByOrder_IdAndStatus(orderId, PaymentStatus.FAILED) >= orderProperties.getMaxPaymentAttempts()) {
            throw BusinessException.tooMany("label.payment");
        }

        BuildPaymentUrlRequest request = new BuildPaymentUrlRequest(order.getId(), order.getCode(), order.getTotalAmount());
        return paymentProvider.buildPaymentUrl(request, ipAddress);
    }

    /**
     * Webhook/IPN Callback nhận từ VNPay để xử lý kết quả thanh toán.
     * Luồng xử lý và chống gian lận:
     * 1. Xác thực chữ ký (Checksum) từ VNPay gửi sang.
     * 2. Phân tích Order ID, khóa dòng đơn hàng (SELECT FOR UPDATE) để tránh Race Condition nếu user click thanh toán liên tục.
     * 3. Kiểm tra tính hợp lệ: Đơn chưa được xác nhận thanh toán trước đó, và số tiền VNPay báo về phải khớp 100% với đơn hàng.
     * 4. Ghi nhận giao dịch (Payment) vào CSDL:
     *    - Nếu THÀNH CÔNG (00): 
     *      + Nếu đơn đang PENDING -> Xác nhận thanh toán (markAsPaid).
     *      + Nếu đơn ĐÃ BỊ HỦY hoặc ĐỔI TRẠNG THÁI khác (Late Payment) -> Không khôi phục đơn mà cắm cờ (AdminNote) để xử lý hoàn tiền thủ công.
     *    - Nếu THẤT BẠI: Cấp nhật trạng thái FAILED. Nếu số lần fail vượt quá giới hạn -> Tự động Hủy đơn hàng.
     * 5. Trả về mã phản hồi theo chuẩn VNPay.
     */
    @Override
    public Map<String, String> handleIpn(Map<String, String> params) {
        if (!paymentProvider.verifySignature(params)) {
            return ipnResponse("97", "Invalid Checksum");
        }

        Long orderId = parseOrderId(params.get("vnp_TxnRef"));
        if (orderId == null) {
            return ipnResponse("01", "Order Not Found");
        }

        Order order;
        try {
            order = orderService.findEntityByIdForUpdate(orderId);
        } catch (BusinessException e) {
            return ipnResponse("01", "Order Not Found");
        }

        if (order.getPaymentStatus() != OrderPaymentStatus.UNPAID) {
            return ipnResponse("02", "Order already confirmed");
        }

        BigDecimal vnpAmount = new BigDecimal(params.get("vnp_Amount")).divide(BigDecimal.valueOf(100));
        if (vnpAmount.compareTo(order.getTotalAmount()) != 0) {
            return ipnResponse("04", "Invalid amount");
        }

        String responseCode = params.get("vnp_ResponseCode");
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(vnpAmount);
        payment.setTransactionId(params.get("vnp_TransactionNo"));
        payment.setResponseCode(responseCode);

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            if (order.getStatus() == OrderStatus.PENDING) {
                orderService.markAsPaid(order.getId());
            } else {
                orderService.flagLatePayment(order.getId(),
                        "Đã nhận thanh toán VNPay (mã GD " + params.get("vnp_TransactionNo")
                                + ") sau khi đơn đã chuyển trạng thái " + order.getStatus()
                                + " — cần đối soát/hoàn tiền thủ công.");
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            long failedCount = paymentRepository.countByOrder_IdAndStatus(order.getId(), PaymentStatus.FAILED);
            if (failedCount >= orderProperties.getMaxPaymentAttempts()) {
                orderService.cancelOrder(order.getId(),
                        "Hủy do quá " + orderProperties.getMaxPaymentAttempts() + " lần thanh toán thất bại");
            }
        }

        return ipnResponse("00", "Confirm Success");
    }

    /**
     * Lấy danh sách lịch sử thanh toán (CMS).
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PaymentInternalResponse> getAll(PaymentFilterRequest filter, Pageable pageable) {
        return paymentRepository.findAll(PaymentSpec.build(filter), pageable).map(paymentMapper::toInternalResponse);
    }

    /**
     * Lấy chi tiết lịch sử thanh toán (CMS).
     */
    @Override
    @Transactional(readOnly = true)
    public PaymentInternalResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.payment"));
        return paymentMapper.toInternalResponse(payment);
    }

    /**
     * Helper bóc tách Order ID từ mã giao dịch VNPay gửi về.
     */
    private Long parseOrderId(String txnRef) {
        if (txnRef == null) return null;
        try {
            return Long.parseLong(txnRef.split("_")[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Helper đóng gói phản hồi theo chuẩn cấu trúc VNPay IPN.
     */
    private Map<String, String> ipnResponse(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }
}
