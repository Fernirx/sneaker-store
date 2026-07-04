package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fernirx.sneakerapi.shipping.config.GhnProperties;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnPreviewItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnPreviewRequest;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnPreviewResponse;
import com.fernirx.sneakerapi.shipping.dto.command.CalculateShippingFeeCommand;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.dto.response.ShippingFeeResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
