package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GhnCreateOrderRequest(
        @JsonProperty("to_name") String toName,
        @JsonProperty("to_phone") String toPhone,
        @JsonProperty("to_address") String toAddress,
        @JsonProperty("to_ward_name") String toWardName,
        @JsonProperty("to_district_name") String toDistrictName,
        @JsonProperty("to_province_name") String toProvinceName,
        @JsonProperty("client_order_code") String clientOrderCode,
        @JsonProperty("cod_amount") Long codAmount,
        @JsonProperty("content") String content,
        @JsonProperty("length") Integer length,
        @JsonProperty("width") Integer width,
        @JsonProperty("height") Integer height,
        @JsonProperty("weight") Integer weight,
        @JsonProperty("service_type_id") Integer serviceTypeId,
        @JsonProperty("payment_type_id") Integer paymentTypeId,
        @JsonProperty("note") String note,
        @JsonProperty("required_note") String requiredNote,
        @JsonProperty("items") List<GhnCreateOrderItem> items
) {}
