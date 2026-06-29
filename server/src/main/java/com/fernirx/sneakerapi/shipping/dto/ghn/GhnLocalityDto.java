package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GhnLocalityDto(
        @JsonAlias({"_id", "ProvinceID", "WardCode", "ward_code"})
        String id,
        String name
) {}
