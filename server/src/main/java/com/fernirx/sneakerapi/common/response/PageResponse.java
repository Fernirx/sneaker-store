package com.fernirx.sneakerapi.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        List<T> data,
        MetaResponse meta,
        Instant timestamp
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        MetaResponse meta = new MetaResponse(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
        return new PageResponse<>(page.getContent(), meta, Instant.now());
    }
}
