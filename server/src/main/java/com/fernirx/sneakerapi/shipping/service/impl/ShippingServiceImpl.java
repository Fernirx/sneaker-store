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
    @Cacheable(value = "shipping_wards_v2", key = "#provinceId")
    public List<LocalityResponse> getWardsByProvince(Integer provinceId) {
        return shippingProvider.getWardsByProvince(provinceId);
    }
}
