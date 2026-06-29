package com.fernirx.sneakerapi.shipping.provider;

import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;

import java.util.List;

public interface ShippingProvider {
    List<LocalityResponse> getProvinces();
    List<LocalityResponse> getWardsByProvince(Integer provinceId);
}
