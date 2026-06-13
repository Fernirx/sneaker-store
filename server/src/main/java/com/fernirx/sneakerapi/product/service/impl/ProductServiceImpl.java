package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.product.assembler.ProductAssembler;
import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductImage;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.repository.ProductImageRepository;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import com.fernirx.sneakerapi.product.repository.ProductSpec;
import com.fernirx.sneakerapi.product.repository.ProductVariantRepository;
import com.fernirx.sneakerapi.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductAssembler productAssembler;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(ProductFilterRequest filter, Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(ProductSpec.build(filter), pageable);

        List<Long> productIds = productPage.stream().map(Product::getId).toList();
        if (productIds.isEmpty()) {
            return productPage.map(p -> productAssembler.toResponse(p, List.of(), List.of()));
        }

        productRepository.findAllWithBrandByIds(productIds);

        Map<Long, List<ProductVariant>> variantsByProduct = productVariantRepository
                .findByProductIdInAndActiveTrueOrderByDisplayOrderAsc(productIds)
                .stream()
                .collect(Collectors.groupingBy(v -> v.getProduct().getId()));

        Map<Long, List<ProductImage>> imagesByProduct = productImageRepository
                .findByProductIdInOrderByPrimaryImageDescDisplayOrderAsc(productIds)
                .stream()
                .collect(Collectors.groupingBy(img -> img.getProduct().getId()));

        return productPage.map(product -> productAssembler.toResponse(
                product,
                variantsByProduct.getOrDefault(product.getId(), List.of()),
                imagesByProduct.getOrDefault(product.getId(), List.of())
        ));
    }

    @Override
    public ProductDetailResponse getBySlug(String slug) {
        Product product = productRepository.findActiveBySlugWithBrand(slug)
                .orElseThrow(() -> BusinessException.notFound("label.product"));

        productRepository.incrementViewCount(product.getId());

        List<ProductVariant> variants = productVariantRepository
                .findByProductIdAndActiveTrueOrderByDisplayOrderAsc(product.getId());

        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByPrimaryImageDescDisplayOrderAsc(product.getId());

        product.setViewCount(product.getViewCount() + 1);

        return productAssembler.toDetailResponse(product, variants, images);
    }
}
