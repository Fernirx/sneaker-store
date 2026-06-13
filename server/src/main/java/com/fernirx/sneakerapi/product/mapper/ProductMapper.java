package com.fernirx.sneakerapi.product.mapper;

import com.fernirx.sneakerapi.brand.entity.Brand;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ProductMapper {

    ProductResponse.BrandInfo toBrandInfo(Brand brand);

    ProductDetailResponse.BrandInfo toDetailBrandInfo(Brand brand);
}
