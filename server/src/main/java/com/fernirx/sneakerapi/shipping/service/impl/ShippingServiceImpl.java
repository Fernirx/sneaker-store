package com.fernirx.sneakerapi.shipping.service.impl;

import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import com.fernirx.sneakerapi.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {
    private final ShippingProvider shippingProvider;

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
    public java.math.BigDecimal calculateFee(Integer toWardCode, String toAddress, List<com.fernirx.sneakerapi.shipping.dto.ParcelItem> items) {
        return shippingProvider.calculateFee(toWardCode, toAddress, items);
    }
}
