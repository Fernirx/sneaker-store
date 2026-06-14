package com.fernirx.sneakerapi.product.mapper;

import com.fernirx.sneakerapi.brand.entity.Brand;
import com.fernirx.sneakerapi.product.dto.request.UpdateProductRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductInternalResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ProductMapper {

    ProductResponse.BrandInfo toBrandInfo(Brand brand);

    ProductDetailResponse.BrandInfo toDetailBrandInfo(Brand brand);

    ProductInternalResponse.BrandInfo toInternalBrandInfo(Brand brand);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "soldCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProduct(UpdateProductRequest request, @MappingTarget Product product);
}
