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

    @Override
    @Cacheable("shipping_provinces")
    public List<LocalityResponse> getProvinces() {
        return shippingProvider.getProvinces();
    }

    @Override
    @Cacheable(value = "shipping_districts", key = "#provinceId")
    public List<LocalityResponse> getDistricts(Integer provinceId) {
        return shippingProvider.getDistricts(provinceId);
    }

    @Override
    @Cacheable(value = "shipping_wards", key = "#districtId")
    public List<LocalityResponse> getWardsByDistrict(Integer districtId) {
        return shippingProvider.getWardsByDistrict(districtId);
    }

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
        return calculateShippingFee(resolved);
    }

    @Override
    public ShippingFeeResponse calculateShippingFee(CalculateShippingFeeCommand request) {
        BigDecimal freeShipThreshold = settingService.getStoreSetting().freeShipThreshold();
        if (request.subtotal().compareTo(freeShipThreshold) >= 0) {
            return new ShippingFeeResponse(BigDecimal.ZERO, null);
        }
        return shippingProvider.calculateShippingFee(request);
    }

    @Override
    public ShipmentResult createShipment(CreateShipmentCommand command) {
        return shippingProvider.createShipment(command);
    }

    @Override
    public void cancelShipment(String shippingOrderCode) {
        shippingProvider.cancelShipment(shippingOrderCode);
    }

    @Override
    public ShipmentStatusResult getShipmentStatus(String clientOrderCode) {
        return shippingProvider.getShipmentStatus(clientOrderCode);
    }
}
