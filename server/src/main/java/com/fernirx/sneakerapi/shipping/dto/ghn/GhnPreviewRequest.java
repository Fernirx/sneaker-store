package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GhnPreviewRequest(
        @JsonProperty("payment_type_id") Integer paymentTypeId,
        @JsonProperty("required_note") String requiredNote,
        @JsonProperty("to_name") String toName,
        @JsonProperty("to_phone") String toPhone,
        @JsonProperty("to_address") String toAddress,
        @JsonProperty("to_ward_name") String toWardName,
        @JsonProperty("to_district_name") String toDistrictName,
        @JsonProperty("to_province_name") String toProvinceName,
        @JsonProperty("length") Integer length,
        @JsonProperty("width") Integer width,
        @JsonProperty("height") Integer height,
        @JsonProperty("weight") Integer weight,
        @JsonProperty("service_type_id") Integer serviceTypeId,
        @JsonProperty("items") List<GhnPreviewItem> items
) {}
