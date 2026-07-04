package com.fernirx.sneakerapi.shipping.service.impl;

import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.request.CalculateShippingFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.request.PreviewOrderFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final ShippingProvider shippingProvider;
    private final ProductVariantService productVariantService;

    @Override
    @Cacheable("shipping_provinces_v2")
    public List<LocalityResponse> getProvinces() {
        return shippingProvider.getProvinces();
    }

    @Override
    @Cacheable(value = "shipping_districts_v2", key = "#provinceId")
    public List<LocalityResponse> getDistricts(Integer provinceId) {
        return shippingProvider.getDistricts(provinceId);
    }

    @Override
    @Cacheable(value = "shipping_wards_v2", key = "#districtId")
    public List<LocalityResponse> getWardsByDistrict(Integer districtId) {
        return shippingProvider.getWardsByDistrict(districtId);
    }

    @Override
    public BigDecimal calculateFee(CalculateShippingFeeRequest request) {
        List<ParcelItem> items = request.items().stream()
                .map(item -> {
                    ProductVariant variant = productVariantService.findActiveById(item.variantId());
                    return ParcelItem.from(variant, item.quantity());
                })
                .toList();
        return shippingProvider.calculateFee(request.toDistrictCode(), request.toWardCode(), items);
    }

    @Override
    public BigDecimal previewOrderFee(PreviewOrderFeeRequest request) {
        return shippingProvider.previewOrderFee(request);
    }
}
