package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fernirx.sneakerapi.shipping.config.GhnProperties;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnApiResponse;
import com.fernirx.sneakerapi.shipping.dto.ghn.GhnLocalityDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GhnClient {
    private final RestClient restClient;

    public GhnClient(GhnProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .defaultHeader("token", properties.getToken())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<GhnLocalityDto> getProvinces() {
        try {
            GhnApiResponse<GhnLocalityDto> response = restClient.get()
                    .uri("/master-data/province/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return (response != null && response.data() != null) ? response.data() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching provinces from GHN: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<GhnLocalityDto> getWardsByProvince(Integer provinceId) {
        try {
            GhnApiResponse<GhnLocalityDto> response = restClient.post()
                    .uri("/master-data/ward/all-by-province-id")
                    .body(Map.of("province_id", provinceId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return (response != null && response.data() != null) ? response.data() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching wards from GHN for provinceId {}: {}", provinceId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
