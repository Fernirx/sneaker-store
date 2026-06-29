package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GhnProvider implements ShippingProvider {
    private final GhnClient ghnClient;

    @Override
    public List<LocalityResponse> getProvinces() {
        return ghnClient.getProvinces().stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .toList();
    }

    @Override
    public List<LocalityResponse> getWardsByProvince(Integer provinceId) {
        return ghnClient.getWardsByProvince(provinceId).stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .toList();
    }
}
