package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fernirx.sneakerapi.shipping.config.GhnProperties;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnCreateOrderItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnCreateOrderRequest;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnCreateOrderResponse;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnOrderDetailItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnPreviewItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnPreviewRequest;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnPreviewResponse;
import com.fernirx.sneakerapi.shipping.dto.command.CalculateShippingFeeCommand;
import com.fernirx.sneakerapi.shipping.dto.command.CreateShipmentCommand;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentStatusResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShippingFeeResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GhnProvider implements ShippingProvider {
    /** Chính sách xem hàng mặc định khi tạo đơn GHN - cho xem, không cho thử/mở hộp */
    private static final String DEFAULT_REQUIRED_NOTE = "CHOXEMHANGKHONGTHU";
    /** payment_type_id = 1: người gửi (shop) trả phí GHN, khớp với model shippingFee gộp vào totalAmount khách trả cho shop */
    private static final Integer DEFAULT_PAYMENT_TYPE_ID = 1;
    /** service_type_id = 2: hàng nhẹ - GHN tính cước theo length/width/height/weight top-level, không đọc items[] */
    private static final Integer SERVICE_TYPE_ID = 2;
    /** GHN trả expected_delivery_time theo UTC - hệ thống chỉ vận hành ở VN nên quy đổi trực tiếp sang giờ VN */
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    /** Hệ thống cần admin xác nhận đơn trước khi bàn giao GHN nên cộng thêm 1 ngày vào mốc GHN dự kiến khi hiển thị cho khách */
    private static final int CONFIRM_DELAY_DAYS = 1;

    /**
     * Chuẩn hóa 22 trạng thái GHN thành 5 nhóm nội bộ - không để nguyên vocabulary riêng của GHN lộ ra
     * ngoài package này (Order module, FE chỉ biết 5 giá trị). Nhóm "delivering" gồm cả các trạng thái
     * giao/trả hàng chưa ngã ngũ (delivery_fail, return*, exception, damage, lost) - khớp đúng quy tắc
     * nghiệp vụ đã chốt: chỉ "cancel"/"returned" mới thật sự chuyển Order sang CANCELLED, các trạng thái
     * còn lại vẫn coi là đơn đang trong quá trình vận chuyển (SHIPPING).
     */
    private static final Map<String, String> GHN_STATUS_GROUPS = Map.ofEntries(
            Map.entry("ready_to_pick", "ready_to_pick"),
            Map.entry("picking", "picking"),
            Map.entry("money_collect_picking", "picking"),
            Map.entry("picked", "picking"),
            Map.entry("storing", "picking"),
            Map.entry("transporting", "picking"),
            Map.entry("sorting", "picking"),
            Map.entry("delivering", "delivering"),
            Map.entry("money_collect_delivering", "delivering"),
            Map.entry("delivery_fail", "delivering"),
            Map.entry("waiting_to_return", "delivering"),
            Map.entry("return", "delivering"),
            Map.entry("return_transporting", "delivering"),
            Map.entry("return_sorting", "delivering"),
            Map.entry("returning", "delivering"),
            Map.entry("return_fail", "delivering"),
            Map.entry("exception", "delivering"),
            Map.entry("damage", "delivering"),
            Map.entry("lost", "delivering"),
            Map.entry("delivered", "delivered"),
            Map.entry("cancel", "cancel"),
            Map.entry("returned", "cancel")
    );

    private final GhnClient ghnClient;
    private final GhnProperties properties;

    @Override
    public List<LocalityResponse> getProvinces() {
        return ghnClient.getProvinces().stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<LocalityResponse> getDistricts(Integer provinceId) {
        return ghnClient.getDistricts(provinceId).stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public List<LocalityResponse> getWardsByDistrict(Integer districtId) {
        return ghnClient.getWardsByDistrict(districtId).stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ShippingFeeResponse calculateShippingFee(CalculateShippingFeeCommand request) {
        PackageDimensions dimensions = aggregateDimensions(request.items());

        List<GhnPreviewItem> previewItems = request.items().stream()
                .map(item -> new GhnPreviewItem(item.name(), item.code(), item.quantity()))
                .toList();

        GhnPreviewRequest requestDto = new GhnPreviewRequest(
                DEFAULT_PAYMENT_TYPE_ID,
                DEFAULT_REQUIRED_NOTE,
                request.recipientName(),
                request.recipientPhone(),
                request.shippingStreet(),
                request.shippingWard(),
                request.shippingDistrict(),
                request.shippingProvince(),
                dimensions.length(),
                dimensions.width(),
                dimensions.height(),
                dimensions.weight(),
                SERVICE_TYPE_ID,
                previewItems
        );

        GhnPreviewResponse response = ghnClient.previewOrder(requestDto, properties.getFallbackFee());

        LocalDateTime expectedDeliveryTime = response.expectedDeliveryTime() != null
                ? LocalDateTime.ofInstant(response.expectedDeliveryTime(), VN_ZONE).plusDays(CONFIRM_DELAY_DAYS)
                : null;

        return new ShippingFeeResponse(response.totalFee(), expectedDeliveryTime);
    }

    @Override
    public ShipmentResult createShipment(CreateShipmentCommand command) {
        PackageDimensions dimensions = aggregateDimensions(command.items());

        List<GhnCreateOrderItem> orderItems = command.items().stream()
                .map(item -> new GhnCreateOrderItem(
                        item.name(),
                        item.code(),
                        item.quantity(),
                        item.price() != null ? item.price().longValue() : 0L))
                .toList();

        GhnCreateOrderRequest requestDto = new GhnCreateOrderRequest(
                command.recipientName(),
                command.recipientPhone(),
                command.shippingStreet(),
                command.shippingWard(),
                command.shippingDistrict(),
                command.shippingProvince(),
                command.clientOrderCode(),
                command.codAmount(),
                buildContent(command.items()),
                dimensions.length(),
                dimensions.width(),
                dimensions.height(),
                dimensions.weight(),
                SERVICE_TYPE_ID,
                command.paymentTypeId(),
                command.note(),
                DEFAULT_REQUIRED_NOTE,
                orderItems
        );

        GhnCreateOrderResponse response = ghnClient.createOrder(requestDto);

        LocalDateTime expectedDeliveryAt = response.expectedDeliveryTime() != null
                ? LocalDateTime.ofInstant(response.expectedDeliveryTime(), VN_ZONE)
                : null;

        return new ShipmentResult(response.orderCode(), expectedDeliveryAt);
    }

    @Override
    public void cancelShipment(String shippingOrderCode) {
        ghnClient.cancelOrder(shippingOrderCode);
    }

    @Override
    public ShipmentStatusResult getShipmentStatus(String clientOrderCode) {
        GhnOrderDetailItem detail = ghnClient.getOrderDetail(clientOrderCode);

        LocalDateTime expectedDeliveryAt = detail.leadtime() != null
                ? LocalDateTime.ofInstant(detail.leadtime(), VN_ZONE)
                : null;
        LocalDateTime deliveredAt = detail.finishDate() != null
                ? LocalDateTime.ofInstant(detail.finishDate(), VN_ZONE)
                : null;

        String normalizedStatus = GHN_STATUS_GROUPS.getOrDefault(detail.status(), "picking");
        return new ShipmentStatusResult(normalizedStatus, expectedDeliveryAt, deliveredAt);
    }

    private String buildContent(List<ParcelItem> items) {
        return items.stream().map(ParcelItem::name).distinct().collect(Collectors.joining(", "));
    }

    private PackageDimensions aggregateDimensions(List<ParcelItem> items) {
        int totalWeight = items.stream()
                .mapToInt(item -> (item.weight() != null && item.weight() > 0 ? item.weight() : 800) * (item.quantity() != null ? item.quantity() : 1))
                .sum();
        if (totalWeight <= 0) {
            totalWeight = 800;
        }

        int maxLength = items.stream()
                .mapToInt(item -> item.length() != null && item.length() > 0 ? item.length() : 33)
                .max()
                .orElse(33);

        int maxWidth = items.stream()
                .mapToInt(item -> item.width() != null && item.width() > 0 ? item.width() : 22)
                .max()
                .orElse(22);

        int totalHeight = items.stream()
                .mapToInt(item -> (item.height() != null && item.height() > 0 ? item.height() : 12) * (item.quantity() != null ? item.quantity() : 1))
                .sum();
        if (totalHeight <= 0) {
            totalHeight = 12;
        }

        return new PackageDimensions(totalWeight, maxLength, maxWidth, totalHeight);
    }

    private record PackageDimensions(int weight, int length, int width, int height) {}
}
