package com.fernirx.sneakerapi.product.assembler;

import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductImageGroupResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductInternalResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;
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

/**
 * Component đóng vai trò "Nhà máy lắp ráp" (Assembler) cho Product.
 * Chịu trách nhiệm gộp (group) các Entity rời rạc (Product, Variants, Images)
 * thành các DTO phân cấp phức tạp (theo Colorway, Size) thông qua Stream API.
 */
@Component
@RequiredArgsConstructor
public class ProductAssembler {

    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;

    /**
     * Lắp ráp ProductResponse rút gọn (dành cho danh sách sản phẩm - Storefront).
     * Chỉ map thông tin cơ bản và Color Swatches.
     */
    public ProductResponse toResponse(Product product,
                                      List<ProductVariant> variants,
                                      List<ProductImage> images) {
        return new ProductResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getGender(),
                product.getMinPrice(),
                product.getMaxPrice(),
                product.getNewArrival(),
                product.getOnSale(),
                productMapper.toBrandInfo(product.getBrand()),
                buildColorSwatches(variants, images)
        );
    }

    /**
     * Lắp ráp ProductDetailResponse chi tiết (dành cho trang chi tiết sản phẩm - Storefront).
     * Bao gồm mô tả, số lượng tồn kho theo từng Size, và hình ảnh chi tiết theo từng màu.
     */
    public ProductDetailResponse toDetailResponse(Product product,
                                                  List<ProductVariant> variants,
                                                  List<ProductImage> images) {
        return new ProductDetailResponse(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getDescription(),
                product.getGender(),
                product.getUpperMaterial(),
                product.getSoleType(),
                product.getClosureType(),
                product.getShaftStyle(),
                product.getMinPrice(),
                product.getMaxPrice(),
                product.getNewArrival(),
                product.getOnSale(),
                product.getSoldCount(),
                product.getViewCount(),
                productMapper.toDetailBrandInfo(product.getBrand()),
                buildColorDetails(variants, images)
        );
    }

    /**
     * Lắp ráp ProductInternalResponse (dành cho CMS).
     * Trả về toàn bộ thông tin nội bộ của sản phẩm (bao gồm cả các cờ Active, Code gốc).
     */
    public ProductInternalResponse toInternalResponse(Product product, String primaryImagePublicId) {
        return new ProductInternalResponse(
                product.getId(),
                product.getSlug(),
                product.getCode(),
                product.getName(),
                product.getGender(),
                product.getDescription(),
                product.getUpperMaterial(),
                product.getSoleType(),
                product.getClosureType(),
                product.getShaftStyle(),
                product.getMinPrice(),
                product.getMaxPrice(),
                product.getNewArrival(),
                product.getOnSale(),
                product.getActive(),
                product.getSoldCount(),
                product.getViewCount(),
                productMapper.toInternalBrandInfo(product.getBrand()),
                primaryImagePublicId,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    /**
     * Group danh sách Variant thành các nhóm theo Colorway (màu sắc).
     * Sử dụng LinkedHashMap để bảo toàn thứ tự sắp xếp gốc (thường là theo DisplayOrder).
     */
    public List<ProductVariantGroupResponse> toVariantGroups(List<ProductVariant> variants) {
        // Nhóm các biến thể (Variant) theo trường Colorway
        Map<String, List<ProductVariant>> byColorway = variants.stream()
                .collect(Collectors.groupingBy(
                        ProductVariant::getColorway,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return byColorway.entrySet().stream()
                .map(entry -> {
                    ProductVariant first = entry.getValue().getFirst();
                    List<ProductVariantGroupResponse.VariantResponse> variantResponses = entry.getValue().stream()
                            .map(productVariantMapper::toVariantResponse)
                            .toList();
                    return new ProductVariantGroupResponse(
                            entry.getKey(),
                            first.getColorwayCode(),
                            first.getColorHex(),
                            variantResponses
                    );
                })
                .toList();
    }

    /**
     * Group danh sách Hình ảnh thành các nhóm theo Colorway (màu sắc).
     * Tương tự Variants, dùng LinkedHashMap để giữ thứ tự ưu tiên của ảnh.
     */
    public List<ProductImageGroupResponse> toImageGroups(List<ProductImage> images) {
        // Nhóm các ảnh theo trường Colorway
        Map<String, List<ProductImage>> byColorway = images.stream()
                .collect(Collectors.groupingBy(
                        ProductImage::getColorway,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return byColorway.entrySet().stream()
                .map(entry -> {
                    ProductImage first = entry.getValue().getFirst();
                    List<ProductImageGroupResponse.ImageResponse> imageResponses = entry.getValue().stream()
                            .map(img -> new ProductImageGroupResponse.ImageResponse(
                                    img.getId(),
                                    img.getImagePublicId(),
                                    img.getPrimaryImage(),
                                    img.getDisplayOrder()
                            ))
                            .toList();
                    return new ProductImageGroupResponse(
                            entry.getKey(),
                            first.getColorHex(),
                            imageResponses
                    );
                })
                .toList();
    }

    /**
     * Helper xây dựng danh sách Color Swatch (chấm màu hiển thị ngoài card sản phẩm).
     * 1. Lọc lấy các ảnh Primary cho mỗi màu.
     * 2. Group Variants theo màu để lấy đại diện mã Hex và giá bán (nếu màu đó có giá riêng).
     */
    private List<ProductResponse.ColorSwatchResponse> buildColorSwatches(
            List<ProductVariant> variants, List<ProductImage> images) {

        // Lọc stream chỉ lấy ảnh Primary và map sang Map<Colorway, ImageUrl>
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

    /**
     * Helper xây dựng chi tiết cho từng Colorway (trong trang Chi tiết sản phẩm).
     * Bao gồm: Hình ảnh (sắp xếp Primary lên đầu) và Danh sách Size.
     */
    private List<ProductDetailResponse.ColorDetailResponse> buildColorDetails(
            List<ProductVariant> variants, List<ProductImage> images) {

        // Group ảnh theo Colorway, đồng thời sắp xếp (Sort) trong từng group: Primary đưa lên đầu, sau đó theo DisplayOrder
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
