package com.fernirx.sneakerapi.shipping.provider.ghn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
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

    public GhnPreviewResponse previewOrder(GhnPreviewRequest request, BigDecimal fallbackFee) {
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
                return response.data();
            }
            log.warn("Could not retrieve total_fee from GHN preview response, fallback to fee {}. Response: {}", fallbackFee, response);
            return new GhnPreviewResponse(fallbackFee, null);
        } catch (RestClientResponseException ex) {
            String errorBody = ex.getResponseBodyAsString();
            try {
                GhnPreviewApiResponse errResp = objectMapper.readValue(errorBody, GhnPreviewApiResponse.class);
                log.warn("GHN Preview API error status {}: code={}, message={}, code_message={}",
                        ex.getStatusCode(), errResp.code(), errResp.message(), errResp.codeMessage());
            } catch (Exception parseEx) {
                log.warn("GHN Preview API error status {}: {}", ex.getStatusCode(), errorBody);
            }
            return new GhnPreviewResponse(fallbackFee, null);
        } catch (Exception e) {
            log.error("Error previewing order fee from GHN: {}", e.getMessage(), e);
            return new GhnPreviewResponse(fallbackFee, null);
        }
    }

    /** Khác previewOrder: tạo vận đơn thật không có fallback hợp lý - lỗi phải được ném ra để admin biết và thử lại */
    public GhnCreateOrderResponse createOrder(GhnCreateOrderRequest request) {
        try {
            GhnCreateOrderApiResponse response = restClient.post()
                    .uri("/v2/shipping-order/create")
                    .body(request)
                    .retrieve()
                    .body(GhnCreateOrderApiResponse.class);

            if (response == null || response.data() == null || response.data().orderCode() == null) {
                log.error("GHN Create Order API did not return order_code. Response: {}", response);
                throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
            }
            return response.data();
        } catch (RestClientResponseException ex) {
            String errorBody = ex.getResponseBodyAsString();
            try {
                GhnCreateOrderApiResponse errResp = objectMapper.readValue(errorBody, GhnCreateOrderApiResponse.class);
                log.error("GHN Create Order API error status {}: code={}, message={}, code_message={}",
                        ex.getStatusCode(), errResp.code(), errResp.message(), errResp.codeMessage());
            } catch (Exception parseEx) {
                log.error("GHN Create Order API error status {}: {}", ex.getStatusCode(), errorBody);
            }
            throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error creating order via GHN: {}", e.getMessage(), e);
            throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    /** Hủy vận đơn GHN. Ném lỗi nếu gọi API thất bại hoặc GHN trả result=false cho mã đơn này */
    public void cancelOrder(String shippingOrderCode) {
        try {
            GhnCancelOrderApiResponse response = restClient.post()
                    .uri("/v2/switch-status/cancel")
                    .body(new GhnCancelOrderRequest(List.of(shippingOrderCode)))
                    .retrieve()
                    .body(GhnCancelOrderApiResponse.class);

            boolean success = response != null && response.data() != null && response.data().stream()
                    .anyMatch(item -> shippingOrderCode.equals(item.orderCode()) && Boolean.TRUE.equals(item.result()));

            if (!success) {
                log.error("GHN Cancel Order API did not confirm success for order_code {}. Response: {}",
                        shippingOrderCode, response);
                throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
            }
        } catch (RestClientResponseException ex) {
            log.error("GHN Cancel Order API error status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error cancelling order via GHN: {}", e.getMessage(), e);
            throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }

    /** Lấy trạng thái mới nhất của vận đơn theo mã đơn hàng hệ thống (client_order_code = Order.code) */
    public GhnOrderDetailItem getOrderDetail(String clientOrderCode) {
        try {
            GhnOrderDetailApiResponse response = restClient.post()
                    .uri("/v2/shipping-order/detail-by-client-code")
                    .body(new GhnOrderDetailRequest(clientOrderCode))
                    .retrieve()
                    .body(GhnOrderDetailApiResponse.class);

            if (response == null || response.data() == null) {
                log.error("GHN Order Detail API returned no data for client_order_code {}. Response: {}",
                        clientOrderCode, response);
                throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
            }
            return response.data();
        } catch (RestClientResponseException ex) {
            log.error("GHN Order Detail API error status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("Error fetching order detail from GHN: {}", e.getMessage(), e);
            throw BusinessException.of(ErrorCode.SERVICE_UNAVAILABLE);
        }
    }
}
