package com.fernirx.sneakerapi.shipping.provider;

import com.fernirx.sneakerapi.shipping.dto.command.CalculateShippingFeeCommand;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.dto.response.ShippingFeeResponse;

import java.util.List;

public interface ShippingProvider {
    List<LocalityResponse> getProvinces();
    List<LocalityResponse> getDistricts(Integer provinceId);
    List<LocalityResponse> getWardsByDistrict(Integer districtId);
    ShippingFeeResponse calculateShippingFee(CalculateShippingFeeCommand request);
}
