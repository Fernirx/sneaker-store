package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fernirx.sneakerapi.shipping.config.GhnProperties;
import com.fernirx.sneakerapi.shipping.dto.ParcelItem;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnFeeRequest;
import com.fernirx.sneakerapi.shipping.dto.response.LocalityResponse;
import com.fernirx.sneakerapi.shipping.provider.ShippingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GhnProvider implements ShippingProvider {
    private final GhnClient ghnClient;
    private final GhnProperties properties;

    @Override
    public List<LocalityResponse> getProvinces() {
        return ghnClient.getProvinces().stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .toList();
    }

    @Override
    public List<LocalityResponse> getDistricts(Integer provinceId) {
        return ghnClient.getDistricts(provinceId).stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .toList();
    }

    @Override
    public List<LocalityResponse> getWardsByDistrict(Integer districtId) {
        return ghnClient.getWardsByDistrict(districtId).stream()
                .map(dto -> new LocalityResponse(dto.id(), dto.name()))
                .toList();
    }

    @Override
    public BigDecimal calculateFee(Integer toWardCode, String toAddress, List<ParcelItem> items) {
        int totalWeight = items.stream()
                .mapToInt(item -> (item.weight() != null && item.weight() > 0 ? item.weight() : 800) * (item.quantity() != null ? item.quantity() : 1))
                .sum();
        if (totalWeight <= 0) {
            totalWeight = 800;
        }

        int maxLength = items.stream()
                .mapToInt(item -> item.length() != null && item.length() > 0 ? item.length() : 33)
                .max()
                .orElse(33);

        int maxWidth = items.stream()
                .mapToInt(item -> item.width() != null && item.width() > 0 ? item.width() : 22)
                .max()
                .orElse(22);

        int totalHeight = items.stream()
                .mapToInt(item -> (item.height() != null && item.height() > 0 ? item.height() : 12) * (item.quantity() != null ? item.quantity() : 1))
                .sum();
        if (totalHeight <= 0) {
            totalHeight = 12;
        }

        String fromAddressV2 = String.format("%s, %s",
                properties.getFromStreet() != null ? properties.getFromStreet() : "",
                properties.getFromWardName() != null ? properties.getFromWardName() : "");

        GhnFeeRequest requestDto = new GhnFeeRequest(
                properties.getShopId(),
                2,
                properties.getFromWardCode(),
                fromAddressV2,
                toWardCode,
                toAddress != null && !toAddress.isBlank() ? toAddress : ("Khách hàng, Phường/Xã " + toWardCode),
                true,
                true,
                totalHeight,
                maxLength,
                totalWeight,
                maxWidth
        );

        return ghnClient.calculateFee(requestDto, properties.getFallbackFee());
    }
}
