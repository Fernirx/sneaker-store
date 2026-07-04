package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernirx.sneakerapi.shipping.config.GhnProperties;
import com.fernirx.sneakerapi.shipping.dto.ghn.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GhnClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GhnClient(GhnProperties properties) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getUrl())
                .defaultHeader("token", properties.getToken())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        if (properties.getShopId() != null) {
            builder.defaultHeader("ShopId", String.valueOf(properties.getShopId()))
                   .defaultHeader("shop_id", String.valueOf(properties.getShopId()));
        }
        this.restClient = builder.build();
    }

    public List<GhnProvince> getProvinces() {
        try {
            GhnApiResponse<GhnProvince> response = restClient.get()
                    .uri("/master-data/province")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return (response != null && response.data() != null) ? response.data() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching provinces from GHN: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<GhnDistrict> getDistricts(Integer provinceId) {
        try {
            GhnApiResponse<GhnDistrict> response = restClient.post()
                    .uri("/master-data/district")
                    .body(Map.of("province_id", provinceId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return (response != null && response.data() != null) ? response.data() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching districts from GHN for provinceId {}: {}", provinceId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<GhnWard> getWardsByDistrict(Integer districtId) {
        try {
            GhnApiResponse<GhnWard> response = restClient.post()
                    .uri("/master-data/ward")
                    .body(Map.of("district_id", districtId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return (response != null && response.data() != null) ? response.data() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching wards from GHN for districtId {}: {}", districtId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public BigDecimal calculateFee(GhnFeeRequest request, BigDecimal fallbackFee) {
        try {
            GhnFeeApiResponse response = restClient.post()
                    .uri("/v2/shipping-order/fee")
                    .body(request)
                    .retrieve()
                    .body(GhnFeeApiResponse.class);

            if (response != null && response.code() != null && response.code() != 200) {
                log.warn("GHN Fee API returned non-200 code {}: message={}, code_message={}: {}",
                        response.code(), response.message(), response.codeMessage(), response.codeMessageValue());
            }

            if (response != null && response.data() != null && response.data().total() != null) {
                return response.data().total();
            }
            log.warn("Could not retrieve total fee from GHN response, fallback to fee {}. Response: {}", fallbackFee, response);
            return fallbackFee;
        } catch (RestClientResponseException ex) {
            String errorBody = ex.getResponseBodyAsString();
            try {
                GhnFeeApiResponse errResp = objectMapper.readValue(errorBody, GhnFeeApiResponse.class);
                log.warn("GHN Fee API error status {}: code={}, message={}, code_message={}: {}",
                        ex.getStatusCode(), errResp.code(), errResp.message(), errResp.codeMessage(), errResp.codeMessageValue());
            } catch (Exception parseEx) {
                log.warn("GHN Fee API error status {}: {}", ex.getStatusCode(), errorBody);
            }
            return fallbackFee;
        } catch (Exception e) {
            log.error("Error calculating shipping fee from GHN: {}", e.getMessage(), e);
            return fallbackFee;
        }
    }

    public BigDecimal previewOrder(GhnPreviewRequest request, BigDecimal fallbackFee) {
        try {
            GhnPreviewApiResponse response = restClient.post()
                    .uri("/v2/shipping-order/preview")
                    .body(request)
                    .retrieve()
                    .body(GhnPreviewApiResponse.class);

            if (response != null && response.code() != null && response.code() != 200) {
                log.warn("GHN Preview API returned non-200 code {}: message={}, code_message={}",
                        response.code(), response.message(), response.codeMessage());
            }

            if (response != null && response.data() != null && response.data().totalFee() != null) {
                return response.data().totalFee();
            }
            log.warn("Could not retrieve total_fee from GHN preview response, fallback to fee {}. Response: {}", fallbackFee, response);
            return fallbackFee;
        } catch (RestClientResponseException ex) {
            String errorBody = ex.getResponseBodyAsString();
            try {
                GhnPreviewApiResponse errResp = objectMapper.readValue(errorBody, GhnPreviewApiResponse.class);
                log.warn("GHN Preview API error status {}: code={}, message={}, code_message={}",
                        ex.getStatusCode(), errResp.code(), errResp.message(), errResp.codeMessage());
            } catch (Exception parseEx) {
                log.warn("GHN Preview API error status {}: {}", ex.getStatusCode(), errorBody);
            }
            return fallbackFee;
        } catch (Exception e) {
            log.error("Error previewing order fee from GHN: {}", e.getMessage(), e);
            return fallbackFee;
        }
    }
}
