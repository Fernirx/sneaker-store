package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GhnProvince(
        @JsonAlias("ProvinceID")
        Integer id,

        @JsonAlias("ProvinceName")
        String name
) {}

