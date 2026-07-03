package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GhnLocality(
        @JsonAlias({"_id", "ProvinceID", "DistrictID", "WardCode", "ward_code"})
        Integer id,
        @JsonAlias({"ProvinceName", "DistrictName", "WardName"})
        String name
) {}
