package com.fernirx.sneakerapi.shipping.service;

import com.fernirx.sneakerapi.shipping.dto.request.CalculateShippingFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.request.PreviewOrderFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingService {
    List<LocalityResponse> getProvinces();
    List<LocalityResponse> getDistricts(Integer provinceId);
    List<LocalityResponse> getWardsByDistrict(Integer districtId);
    BigDecimal calculateFee(CalculateShippingFeeRequest request);
    BigDecimal previewOrderFee(PreviewOrderFeeRequest request);
}
