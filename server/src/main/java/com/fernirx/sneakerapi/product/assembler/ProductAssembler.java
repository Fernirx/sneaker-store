package com.fernirx.sneakerapi.product.assembler;

import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductImage;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.mapper.ProductMapper;
import com.fernirx.sneakerapi.product.mapper.ProductVariantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductAssembler {

    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;

    public ProductResponse toResponse(Product product,
                                      List<ProductVariant> variants,
                                      List<ProductImage> images) {
        return new ProductResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getGender(),
                product.getBasePrice(),
                product.getOriginalPrice(),
                product.getNewArrival(),
                product.getOnSale(),
                productMapper.toBrandInfo(product.getBrand()),
                buildColorSwatches(variants, images)
        );
    }

    public ProductDetailResponse toDetailResponse(Product product,
                                                  List<ProductVariant> variants,
                                                  List<ProductImage> images) {
        return new ProductDetailResponse(
                product.getId(),
                product.getSlug(),
                product.getStyleCode(),
                product.getName(),
                product.getDescription(),
                product.getGender(),
                product.getUpperMaterial(),
                product.getSoleType(),
                product.getClosureType(),
                product.getShaftStyle(),
                product.getBasePrice(),
                product.getOriginalPrice(),
                product.getNewArrival(),
                product.getOnSale(),
                product.getSoldCount(),
                product.getViewCount(),
                productMapper.toDetailBrandInfo(product.getBrand()),
                buildColorDetails(variants, images)
        );
    }

    private List<ProductResponse.ColorSwatchResponse> buildColorSwatches(
            List<ProductVariant> variants, List<ProductImage> images) {

        Map<String, String> primaryImageByColorway = images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getPrimaryImage()))
                .collect(Collectors.toMap(
                        ProductImage::getColorway,
                        ProductImage::getImagePublicId,
                        (existing, duplicate) -> existing
                ));

        Map<String, List<ProductVariant>> variantsByColorway = variants.stream()
                .collect(Collectors.groupingBy(
                        ProductVariant::getColorway,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return variantsByColorway.entrySet().stream()
                .map(entry -> {
                    String colorway = entry.getKey();
                    ProductVariant first = entry.getValue().getFirst();
                    BigDecimal overridePrice = entry.getValue().stream()
                            .map(ProductVariant::getPrice)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    return new ProductResponse.ColorSwatchResponse(
                            colorway,
                            first.getColorwayCode(),
                            first.getColorHex(),
                            primaryImageByColorway.get(colorway),
                            overridePrice
                    );
                })
                .toList();
    }

    private List<ProductDetailResponse.ColorDetailResponse> buildColorDetails(
            List<ProductVariant> variants, List<ProductImage> images) {

        Map<String, List<ProductDetailResponse.ImageResponse>> imagesByColorway = images.stream()
                .collect(Collectors.groupingBy(
                        ProductImage::getColorway,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator
                                                .comparing(ProductImage::getPrimaryImage).reversed()
                                                .thenComparingInt(ProductImage::getDisplayOrder))
                                        .map(img -> new ProductDetailResponse.ImageResponse(
                                                img.getImagePublicId(),
                                                Boolean.TRUE.equals(img.getPrimaryImage())
                                        ))
                                        .toList()
                        )
                ));

        Map<String, List<ProductVariant>> variantsByColorway = variants.stream()
                .collect(Collectors.groupingBy(
                        ProductVariant::getColorway,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return variantsByColorway.entrySet().stream()
                .map(entry -> {
                    String colorway = entry.getKey();
                    ProductVariant first = entry.getValue().getFirst();
                    List<ProductDetailResponse.SizeResponse> sizes = entry.getValue().stream()
                            .sorted(Comparator.comparing(ProductVariant::getSize))
                            .map(productVariantMapper::toSizeResponse)
                            .toList();
                    return new ProductDetailResponse.ColorDetailResponse(
                            colorway,
                            first.getColorwayCode(),
                            first.getColorHex(),
                            imagesByColorway.getOrDefault(colorway, List.of()),
                            sizes
                    );
                })
                .toList();
    }
}
