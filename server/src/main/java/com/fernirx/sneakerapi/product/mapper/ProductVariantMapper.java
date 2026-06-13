package com.fernirx.sneakerapi.product.mapper;

import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ProductVariantMapper {

    @Mapping(target = "variantId", source = "id")
    ProductDetailResponse.SizeResponse toSizeResponse(ProductVariant variant);
}
