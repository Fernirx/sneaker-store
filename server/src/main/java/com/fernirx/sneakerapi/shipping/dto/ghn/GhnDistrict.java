package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GhnDistrict(
        @JsonAlias("DistrictID")
        Integer id,

        @JsonAlias("DistrictName")
        String name
) {}

