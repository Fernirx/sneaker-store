package com.fernirx.sneakerapi.shipping.service;

import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;

import java.util.List;

public interface ShippingService {
    List<LocalityResponse> getProvinces();
    List<LocalityResponse> getWardsByProvince(Integer provinceId);
}
