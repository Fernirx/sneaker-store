package com.fernirx.sneakerapi.shipping.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;

public record GhnWard(
        @JsonAlias({"WardCode", "ward_code"})
        Integer id,

        @JsonAlias("WardName")
        String name
) {}

