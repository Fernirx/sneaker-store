package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.product.assembler.ProductAssembler;
import com.fernirx.sneakerapi.product.dto.request.CreateVariantRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateVariantRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductVariantGroupResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.mapper.ProductVariantMapper;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import com.fernirx.sneakerapi.product.repository.ProductVariantRepository;
import com.fernirx.sneakerapi.product.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductVariantMapper productVariantMapper;
    private final ProductAssembler productAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantGroupResponse> getVariants(Long productId) {
        findProduct(productId);
        List<ProductVariant> variants = productVariantRepository
                .findByProductIdOrderByColorwayAscSizeAsc(productId);
        return productAssembler.toVariantGroups(variants);
    }

    @Override
    public ProductVariantGroupResponse.VariantResponse addVariant(Long productId, CreateVariantRequest request) {
        Product product = findProduct(productId);

        if (productVariantRepository.existsBySku(request.sku())) {
            throw BusinessException.alreadyExists("label.product.variant");
        }
        if (productVariantRepository.existsByProductIdAndSizeAndColorwayAndShoeWidth(
                productId, request.size(), request.colorway(), request.shoeWidth())) {
            throw BusinessException.alreadyExists("label.product.variant");
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize(request.size());
        variant.setShoeWidth(request.shoeWidth());
        variant.setColorway(request.colorway());
        variant.setColorwayCode(request.colorwayCode());
        variant.setColorHex(request.colorHex());
        variant.setPrice(request.price());
        variant.setSku(request.sku());
        variant.setStockQuantity(request.stockQuantity());
        variant.setMinStockLevel(request.minStockLevel() != null ? request.minStockLevel() : 5);
        variant.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        variant.setActive(true);

        return productVariantMapper.toVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    public ProductVariantGroupResponse.VariantResponse updateVariant(Long productId, Long variantId, UpdateVariantRequest request) {
        findProduct(productId);
        ProductVariant variant = findVariant(productId, variantId);

        if (request.sku() != null && !request.sku().equals(variant.getSku())) {
            if (productVariantRepository.existsBySkuAndIdNot(request.sku(), variantId)) {
                throw BusinessException.alreadyExists("label.product.variant");
            }
        }

        productVariantMapper.updateVariant(request, variant);
        return productVariantMapper.toVariantResponse(productVariantRepository.save(variant));
    }

    @Override
    public void deleteVariant(Long productId, Long variantId) {
        findProduct(productId);
        ProductVariant variant = findVariant(productId, variantId);
        productVariantRepository.delete(variant);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }

    private ProductVariant findVariant(Long productId, Long variantId) {
        return productVariantRepository.findByIdAndProductId(variantId, productId)
                .orElseThrow(() -> BusinessException.notFound("label.product.variant"));
    }
}
