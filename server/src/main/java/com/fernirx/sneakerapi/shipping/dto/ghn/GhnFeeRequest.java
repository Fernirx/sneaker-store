package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GhnFeeRequest(
        @JsonProperty("shop_id") Integer shopId,
        @JsonProperty("service_type_id") Integer serviceTypeId,
        @JsonProperty("from_ward_id_v2") Integer fromWardIdV2,
        @JsonProperty("from_address_v2") String fromAddressV2,
        @JsonProperty("to_ward_id_v2") Integer toWardIdV2,
        @JsonProperty("to_address_v2") String toAddressV2,
        @JsonProperty("is_new_from_address") Boolean isNewFromAddress,
        @JsonProperty("is_new_to_address") Boolean isNewToAddress,
        @JsonProperty("height") Integer height,
        @JsonProperty("length") Integer length,
        @JsonProperty("weight") Integer weight,
        @JsonProperty("width") Integer width
) {}
