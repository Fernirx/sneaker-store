package com.fernirx.sneakerapi.brand.mapper;

import com.fernirx.sneakerapi.brand.dto.request.UpdateBrandRequest;
import com.fernirx.sneakerapi.brand.dto.response.BrandInternalResponse;
import com.fernirx.sneakerapi.brand.dto.response.BrandResponse;
import com.fernirx.sneakerapi.brand.entity.Brand;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface BrandMapper {

    BrandResponse toResponse(Brand brand);

    BrandInternalResponse toInternalResponse(Brand brand);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateBrand(UpdateBrandRequest request, @MappingTarget Brand brand);
}
