package com.fernirx.sneakerapi.shipping.service;

import com.fernirx.sneakerapi.shipping.dto.command.CalculateShippingFeeCommand;
import com.fernirx.sneakerapi.shipping.dto.command.CreateShipmentCommand;
import com.fernirx.sneakerapi.shipping.dto.request.PreviewShippingFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentStatusResult;
import com.fernirx.sneakerapi.shipping.dto.response.ShippingFeeResponse;

import java.util.List;

public interface ShippingService {
    List<LocalityResponse> getProvinces();
    List<LocalityResponse> getDistricts(Integer provinceId);
    List<LocalityResponse> getWardsByDistrict(Integer districtId);
    ShippingFeeResponse previewShippingFee(PreviewShippingFeeRequest request);
    ShippingFeeResponse calculateShippingFee(CalculateShippingFeeCommand command);
    ShipmentResult createShipment(CreateShipmentCommand command);
    void cancelShipment(String shippingOrderCode);
    ShipmentStatusResult getShipmentStatus(String clientOrderCode);
}
