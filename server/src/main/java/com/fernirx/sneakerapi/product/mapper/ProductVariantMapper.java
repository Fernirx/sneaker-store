package com.fernirx.sneakerapi.product.mapper;

import com.fernirx.sneakerapi.product.dto.request.UpdateVariantRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ProductVariantMapper {

    @Mapping(target = "variantId", source = "id")
    ProductDetailResponse.SizeResponse toSizeResponse(ProductVariant variant);

    ProductVariantGroupResponse.VariantResponse toVariantResponse(ProductVariant variant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVariant(UpdateVariantRequest request, @MappingTarget ProductVariant variant);
}
