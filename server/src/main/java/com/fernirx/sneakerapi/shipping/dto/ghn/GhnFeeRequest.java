package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GhnFeeRequest(
        @JsonProperty("shop_id") Integer shopId,
        @JsonProperty("service_type_id") Integer serviceTypeId,
        @JsonProperty("to_district_id") Integer toDistrictId,
        @JsonProperty("to_ward_id") Integer toWardId,
        @JsonProperty("height") Integer height,
        @JsonProperty("length") Integer length,
        @JsonProperty("weight") Integer weight,
        @JsonProperty("width") Integer width
) {}
