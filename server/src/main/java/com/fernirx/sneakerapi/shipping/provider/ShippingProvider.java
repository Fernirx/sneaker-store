package com.fernirx.sneakerapi.shipping.provider;

import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ShippingProvider {
    List<LocalityResponse> getProvinces();
    List<LocalityResponse> getDistricts(Integer provinceId);
    List<LocalityResponse> getWardsByDistrict(Integer districtId);
    BigDecimal calculateFee(Integer toWardCode, String toAddress, List<ParcelItem> items);
}
