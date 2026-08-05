package com.fernirx.sneakerapi.shipping.service.impl;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.setting.service.SettingService;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.command.CalculateShippingFeeCommand;
import com.fernirx.sneakerapi.shipping.dto.command.CreateShipmentCommand;
import com.fernirx.sneakerapi.shipping.dto.request.PreviewShippingFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.request.ShippingItemRequest;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentStatusResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShippingFeeResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private static final String PLACEHOLDER_RECIPIENT_NAME = "Khách hàng";
    private static final String PLACEHOLDER_RECIPIENT_PHONE = "0900000000";

    private final ShippingProvider shippingProvider;
    private final ProductVariantService productVariantService;
    private final SettingService settingService;

    /**
     * Lấy danh sách Tỉnh/Thành phố.
     * Sử dụng Redis/Spring Cache (@Cacheable) để tối ưu, tránh gọi API hãng vận chuyển (GHN) liên tục
     * do dữ liệu địa giới hành chính rất hiếm khi thay đổi.
     */
    @Override
    @Cacheable("shipping_provinces")
    public List<LocalityResponse> getProvinces() {
        return shippingProvider.getProvinces();
    }

    /**
     * Lấy danh sách Quận/Huyện theo Tỉnh/Thành phố (có Cache).
     */
    @Override
    @Cacheable(value = "shipping_districts", key = "#provinceId")
    public List<LocalityResponse> getDistricts(Integer provinceId) {
        return shippingProvider.getDistricts(provinceId);
    }

    /**
     * Lấy danh sách Phường/Xã theo Quận/Huyện (có Cache).
     */
    @Override
    @Cacheable(value = "shipping_wards", key = "#districtId")
    public List<LocalityResponse> getWardsByDistrict(Integer districtId) {
        return shippingProvider.getWardsByDistrict(districtId);
    }

    /**
     * Tính trước phí vận chuyển cho màn hình Giỏ Hàng (Cart) khi khách chưa điền thông tin người nhận.
     * Tự động điền dữ liệu giả (Placeholder Name/Phone) để bypass validation của hãng vận chuyển.
     */
    @Override
    public ShippingFeeResponse previewShippingFee(PreviewShippingFeeRequest request) {
        List<Long> variantIds = request.items().stream().map(ShippingItemRequest::variantId).toList();
        Map<Long, ProductVariant> variantsById = productVariantService.findAllActiveByIds(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<ParcelItem> items = new ArrayList<>();
        for (ShippingItemRequest item : request.items()) {
            ProductVariant variant = variantsById.get(item.variantId());
            subtotal = subtotal.add(variant.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
            items.add(ParcelItem.from(variant, item.quantity()));
        }

        CalculateShippingFeeCommand resolved = new CalculateShippingFeeCommand(
                PLACEHOLDER_RECIPIENT_NAME,
                PLACEHOLDER_RECIPIENT_PHONE,
                request.shippingStreet(),
                request.shippingWard(),
                request.shippingDistrict(),
                request.shippingProvince(),
                subtotal,
                items
        );
        return shippingProvider.calculateShippingFee(resolved);
    }

    /**
     * Tính phí vận chuyển thực tế (Checkout).
     * Logic bảo vệ: Kiểm tra tổng giá trị đơn hàng (Subtotal) so với mốc FreeShip trong Cấu hình (StoreSetting).
     * Nếu đạt mốc, trả về phí = 0 ngay lập tức, không cần gọi API hãng vận chuyển cho đỡ tốn tài nguyên.
     */
    @Override
    public ShippingFeeResponse calculateShippingFee(CalculateShippingFeeCommand request) {
        BigDecimal freeShipThreshold = settingService.getStoreSetting().freeShipThreshold();
        if (request.subtotal().compareTo(freeShipThreshold) >= 0) {
            return new ShippingFeeResponse(BigDecimal.ZERO, null);
        }
        return shippingProvider.calculateShippingFee(request);
    }

    /**
     * Bắn API sang hãng vận chuyển (ví dụ: GHN) để tạo đơn giao hàng thực tế.
     */
    @Override
    public ShipmentResult createShipment(CreateShipmentCommand command) {
        return shippingProvider.createShipment(command);
    }

    /**
     * Hủy đơn giao hàng phía hãng vận chuyển.
     */
    @Override
    public void cancelShipment(String shippingOrderCode) {
        shippingProvider.cancelShipment(shippingOrderCode);
    }

    /**
     * Lấy trạng thái hiện tại của đơn giao hàng từ hãng vận chuyển.
     * Sử dụng ClientOrderCode (ví dụ: ORD123456) thay vì mã nội bộ của hãng.
     */
    @Override
    public ShipmentStatusResult getShipmentStatus(String clientOrderCode) {
        return shippingProvider.getShipmentStatus(clientOrderCode);
    }
}
