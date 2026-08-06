package com.fernirx.sneakerapi.coupon.service.impl;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.coupon.dto.request.CouponFilterRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CouponPreviewRequest;
import com.fernirx.sneakerapi.coupon.dto.request.CreateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.request.UpdateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.response.CouponApplyResult;
import com.fernirx.sneakerapi.coupon.dto.response.CouponInternalResponse;
import com.fernirx.sneakerapi.coupon.dto.response.CouponPreviewResponse;
import com.fernirx.sneakerapi.coupon.entity.Coupon;
import com.fernirx.sneakerapi.coupon.entity.CouponUsage;
import com.fernirx.sneakerapi.coupon.enums.DiscountType;
import com.fernirx.sneakerapi.coupon.mapper.CouponMapper;
import com.fernirx.sneakerapi.coupon.repository.CouponRepository;
import com.fernirx.sneakerapi.coupon.repository.CouponSpec;
import com.fernirx.sneakerapi.coupon.repository.CouponUsageRepository;
import com.fernirx.sneakerapi.coupon.service.CouponService;
import com.fernirx.sneakerapi.notification.event.CouponCreatedEvent;
import com.fernirx.sneakerapi.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CouponMapper couponMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Dùng thử Coupon để xem trước số tiền được giảm giá (cho Front-end).
     * Luồng xử lý:
     * 1. Tìm Coupon theo mã (không phân biệt hoa/thường).
     * 2. Gọi hàm validate để kiểm tra trạng thái, thời gian, và điều kiện tối thiểu.
     * 3. Kiểm tra giới hạn số lần sử dụng của user hiện tại (nếu có email/phone).
     * 4. Tính toán số tiền được giảm giá dựa trên loại Coupon.
     * 5. Trả về kết quả dự kiến.
     */
    @Override
    @Transactional(readOnly = true)
    public CouponPreviewResponse preview(CouponPreviewRequest request) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(request.code())
                .orElseThrow(() -> BusinessException.notFound("label.coupon"));
        validate(coupon, request.orderAmount());
        validateUserUsageLimit(coupon, request.email(), request.phone());
        BigDecimal discountAmount = calculateDiscount(coupon, request.orderAmount());
        BigDecimal finalAmount = request.orderAmount().subtract(discountAmount);
        return new CouponPreviewResponse(
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMinOrderAmount(),
                coupon.getMaxDiscountAmount(),
                discountAmount,
                finalAmount
        );
    }

    /**
     * Lấy danh sách toàn bộ Coupon cho trang quản trị CMS.
     * Hỗ trợ phân trang và tìm kiếm theo bộ lọc.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CouponInternalResponse> getAll(CouponFilterRequest filter, Pageable pageable) {
        return couponRepository.findAll(CouponSpec.build(filter), pageable)
                .map(couponMapper::toInternalResponse);
    }

    /**
     * Lấy chi tiết thông tin Coupon theo ID cho hệ thống quản trị.
     */
    @Override
    @Transactional(readOnly = true)
    public CouponInternalResponse getById(Long id) {
        return couponMapper.toInternalResponse(findById(id));
    }

    /**
     * Tạo mới một mã giảm giá.
     * Luồng xử lý:
     * 1. Kiểm tra xem mã code đã tồn tại chưa.
     * 2. Validate tính hợp lệ của Ngày tháng và Giá trị giảm (không quá 100%).
     * 3. Gán các thông tin cấu hình, ép mã code thành chữ HOA.
     * 4. Lưu xuống DB, sau đó phát Event báo cho hệ thống Notification.
     */
    @Override
    public CouponInternalResponse create(CreateCouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.code())) {
            throw BusinessException.alreadyExists("label.coupon");
        }
        
        validateBusinessRules(request.discountType(), request.discountValue(), request.startDate(), request.endDate());
        
        Coupon coupon = new Coupon();
        coupon.setCode(request.code().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountType(request.discountType());
        coupon.setDiscountValue(request.discountValue());
        coupon.setMinOrderAmount(request.minOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
        coupon.setUsageLimit(request.usageLimit());
        coupon.setUsedCount(0);
        coupon.setUserUsageLimit(request.userUsageLimit());
        coupon.setStartDate(request.startDate());
        coupon.setEndDate(request.endDate());
        coupon.setActive(true);
        Coupon saved = couponRepository.save(coupon);
        eventPublisher.publishEvent(new CouponCreatedEvent(saved.getId(), saved.getCode()));
        return couponMapper.toInternalResponse(saved);
    }

    /**
     * Cập nhật thông tin Coupon.
     * Luồng xử lý:
     * 1. Lấy dữ liệu Coupon cũ.
     * 2. Validate tính hợp lệ của cấu hình mới (Thời gian & Giá trị giảm).
     * 3. Cập nhật các trường được phép thay đổi. (Không cho phép đổi mã Code).
     * 4. Lưu dữ liệu.
     */
    @Override
    public CouponInternalResponse update(Long id, UpdateCouponRequest request) {
        Coupon coupon = findById(id);
        
        DiscountType type = coupon.getDiscountType();
        BigDecimal discountValue = request.discountValue() != null ? request.discountValue() : coupon.getDiscountValue();
        LocalDateTime startDate = request.startDate() != null ? request.startDate() : coupon.getStartDate();
        LocalDateTime endDate = request.endDate() != null ? request.endDate() : coupon.getEndDate();
        
        validateBusinessRules(type, discountValue, startDate, endDate);
        
        couponMapper.updateCoupon(request, coupon);
        return couponMapper.toInternalResponse(couponRepository.save(coupon));
    }

    /**
     * Xóa một mã giảm giá khỏi hệ thống.
     * Luồng xử lý:
     * 1. Tìm Coupon cần xóa.
     * 2. Kiểm tra xem Coupon này đã từng được sử dụng trong Đơn hàng nào chưa.
     * 3. Nếu ĐÃ có lịch sử sử dụng, CHẶN XÓA để bảo toàn lịch sử đối soát kế toán.
     *    (Thay vào đó Admin chỉ được update active = false).
     * 4. Nếu chưa từng sử dụng, cho phép xóa cứng khỏi cơ sở dữ liệu.
     */
    @Override
    public void delete(Long id) {
        Coupon coupon = findById(id);
        if (couponUsageRepository.existsByCoupon_Id(id)) {
            throw BusinessException.of(ErrorCode.IN_USE_REASONS, "label.coupon", "lịch sử sử dụng");
        }
        couponRepository.delete(coupon);
    }

    /**
     * Xác thực hợp lệ của Coupon và tính toán số tiền giảm (dành cho bước Tạo đơn).
     * Trả về kết quả để Service đơn hàng lưu lại thông tin.
     */
    @Override
    @Transactional(readOnly = true)
    public CouponApplyResult validate(String code, BigDecimal orderAmount, String email, String phone) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> BusinessException.notFound("label.coupon"));
        validate(coupon, orderAmount);
        validateUserUsageLimit(coupon, email, phone);
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        return new CouponApplyResult(coupon.getId(), coupon.getCode(), discountAmount);
    }

    /**
     * Ghi nhận lịch sử người dùng đã thực sự áp dụng thành công Coupon cho đơn hàng.
     * Thực hiện tăng số đếm (usedCount) bằng query Atomic để chống race-condition.
     */
    @Override
    public void recordUsage(Long couponId, Order order, String email, String phone) {
        int updatedRows = couponRepository.incrementUsedCount(couponId);
        if (updatedRows == 0) {
            throw BusinessException.of(ErrorCode.COUPON_EXHAUSTED);
        }
        Coupon coupon = findById(couponId);

        CouponUsage usage = new CouponUsage();
        usage.setCoupon(coupon);
        usage.setEmail(email);
        usage.setPhone(phone);
        usage.setOrder(order);
        couponUsageRepository.save(usage);
    }

    /**
     * Hủy áp dụng Coupon cho Đơn hàng (khi đơn hàng bị hủy bỏ/thất bại).
     * Giảm đi số đếm đã sử dụng và xóa lịch sử ghi nhận tương ứng.
     */
    @Override
    public void releaseUsage(Order order) {
        CouponUsage usage = order.getCouponUsage();
        if (usage == null) {
            return;
        }
        couponRepository.decrementUsedCount(usage.getCoupon().getId());
        order.setCouponUsage(null);
        couponUsageRepository.delete(usage);
        order.setCouponUsage(null);
    }

    // ---- Private helpers ----

    /**
     * Kiểm tra số lần giới hạn sử dụng của 1 User cụ thể đối với mã Coupon này.
     * Nếu đã sử dụng hết lượt cho phép thì văng lỗi từ chối.
     */
    private void validateUserUsageLimit(Coupon coupon, String email, String phone) {
        if (coupon.getUserUsageLimit() == null) {
            return;
        }
        long usedByEmail = email != null
                ? couponUsageRepository.countByCoupon_IdAndEmailIgnoreCase(coupon.getId(), email) : 0;
        long usedByPhone = phone != null
                ? couponUsageRepository.countByCoupon_IdAndPhone(coupon.getId(), phone) : 0;
        if (Math.max(usedByEmail, usedByPhone) >= coupon.getUserUsageLimit()) {
            throw BusinessException.of(ErrorCode.COUPON_USAGE_LIMIT);
        }
    }
    
    /**
     * Validate các quy tắc nghiệp vụ khi tạo/sửa Coupon:
     * 1. Thời gian: Ngày kết thúc không được nhỏ hơn ngày bắt đầu.
     * 2. Giá trị: Nếu là PERCENTAGE, % giảm không được lớn hơn 100.
     */
    private void validateBusinessRules(DiscountType type, BigDecimal discountValue, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw BusinessException.bad("label.coupon");
        }
        if (type == DiscountType.PERCENTAGE && discountValue != null && discountValue.compareTo(new BigDecimal("100")) > 0) {
            throw BusinessException.bad("label.coupon");
        }
    }

    /**
     * Validate các điều kiện sử dụng của Coupon tại thời điểm hiện tại:
     * - Cờ Active
     * - Chưa tới ngày bắt đầu / Đã quá ngày kết thúc.
     * - Đã hết lượt dùng chung của toàn hệ thống (usageLimit).
     * - Tổng tiền đơn hàng chưa đạt giá trị tối thiểu.
     */
    private void validate(Coupon coupon, BigDecimal orderAmount) {
        if (!coupon.getActive()) {
            throw BusinessException.bad("label.coupon");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate())) {
            throw BusinessException.bad("label.coupon");
        }
        if (now.isAfter(coupon.getEndDate())) {
            throw BusinessException.expired("label.coupon");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw BusinessException.of(ErrorCode.COUPON_EXHAUSTED);
        }
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw BusinessException.of(ErrorCode.COUPON_MIN_AMOUNT, coupon.getMinOrderAmount());
        }
    }

    /**
     * Tính toán số tiền được giảm thực tế dựa trên Loại Coupon (Phần trăm / Trừ thẳng tiền).
     * Nếu là Phần trăm, sẽ có thêm màng lọc khống chế số tiền giảm tối đa (maxDiscountAmount).
     */
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        return switch (coupon.getDiscountType()) {
            case PERCENTAGE -> {
                BigDecimal discount = orderAmount
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
                if (coupon.getMaxDiscountAmount() != null) {
                    discount = discount.min(coupon.getMaxDiscountAmount());
                }
                yield discount;
            }
            case FIXED_AMOUNT -> coupon.getDiscountValue().min(orderAmount);
        };
    }

    /**
     * Lấy Coupon theo ID, văng lỗi 404 nếu không tìm thấy.
     */
    private Coupon findById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.coupon"));
    }
}
