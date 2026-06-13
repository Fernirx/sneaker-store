package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.brand.entity.Brand;
import com.fernirx.sneakerapi.brand.repository.BrandRepository;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.product.assembler.ProductAssembler;
import com.fernirx.sneakerapi.product.dto.request.CreateProductRequest;
import com.fernirx.sneakerapi.product.dto.request.InternalProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.ProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateProductRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductDetailResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductInternalResponse;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductImage;
import com.fernirx.sneakerapi.product.entity.ProductVariant;
import com.fernirx.sneakerapi.product.mapper.ProductMapper;
import com.fernirx.sneakerapi.product.repository.InternalProductSpec;
import com.fernirx.sneakerapi.product.repository.ProductImageRepository;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import com.fernirx.sneakerapi.product.repository.ProductSpec;
import com.fernirx.sneakerapi.product.repository.ProductVariantRepository;
import com.fernirx.sneakerapi.product.service.ProductService;
import com.github.slugify.Slugify;
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
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final Slugify slugify;

    // ─── Public ──────────────────────────────────────────────────────────────

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

    // ─── Internal ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ProductInternalResponse> getInternalProducts(InternalProductFilterRequest filter, Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(InternalProductSpec.build(filter), pageable);
        List<Long> productIds = productPage.stream().map(Product::getId).toList();
        if (productIds.isEmpty()) {
            return productPage.map(p -> productAssembler.toInternalResponse(p, null));
        }

        productRepository.findAllWithBrandByIds(productIds);

        Map<Long, String> primaryImageByProduct = productImageRepository
                .findByProductIdInAndPrimaryImageTrueOrderByProductIdAscDisplayOrderAsc(productIds)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        ProductImage::getImagePublicId,
                        (existing, duplicate) -> existing
                ));

        return productPage.map(product -> productAssembler.toInternalResponse(
                product,
                primaryImageByProduct.get(product.getId())
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInternalResponse getInternalById(Long id) {
        Product product = findByIdWithBrand(id);
        String primaryImage = productImageRepository
                .findByProductIdOrderByPrimaryImageDescDisplayOrderAsc(id)
                .stream()
                .filter(img -> Boolean.TRUE.equals(img.getPrimaryImage()))
                .map(ProductImage::getImagePublicId)
                .findFirst()
                .orElse(null);
        return productAssembler.toInternalResponse(product, primaryImage);
    }

    @Override
    public ProductInternalResponse createProduct(CreateProductRequest request) {
        Brand brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> BusinessException.notFound("label.brand"));

        if (productRepository.existsByCode(request.code())) {
            throw BusinessException.alreadyExists("label.product");
        }

        Product product = new Product();
        product.setBrand(brand);
        product.setCode(request.code());
        product.setStyleCode(request.styleCode());
        product.setName(request.name());
        product.setSlug(generateUniqueSlug(request.name()));
        product.setDescription(request.description());
        product.setGender(request.gender());
        product.setUpperMaterial(request.upperMaterial());
        product.setSoleType(request.soleType());
        product.setClosureType(request.closureType());
        product.setShaftStyle(request.shaftStyle());
        product.setBasePrice(request.basePrice());
        product.setOriginalPrice(request.originalPrice());
        product.setCostPrice(request.costPrice());
        product.setNewArrival(request.newArrival() != null ? request.newArrival() : false);
        product.setOnSale(request.onSale() != null ? request.onSale() : false);
        product.setActive(true);
        product.setSoldCount(0);
        product.setViewCount(0);

        return productAssembler.toInternalResponse(productRepository.save(product), null);
    }

    @Override
    public ProductInternalResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = findByIdWithBrand(id);

        if (request.brandId() != null && !request.brandId().equals(product.getBrand().getId())) {
            Brand brand = brandRepository.findById(request.brandId())
                    .orElseThrow(() -> BusinessException.notFound("label.brand"));
            product.setBrand(brand);
        }

        if (request.code() != null && !request.code().equals(product.getCode())) {
            if (productRepository.existsByCodeAndIdNot(request.code(), id)) {
                throw BusinessException.alreadyExists("label.product");
            }
        }

        productMapper.updateProduct(request, product);
        return productAssembler.toInternalResponse(productRepository.save(product), null);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
        productRepository.delete(product);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Product findByIdWithBrand(Long id) {
        return productRepository.findByIdWithBrand(id)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }

    private String generateUniqueSlug(String name) {
        String base = slugify.slugify(name);
        if (!productRepository.existsBySlug(base)) return base;
        int suffix = 1;
        String candidate;
        do {
            candidate = base + "-" + suffix++;
        } while (productRepository.existsBySlug(candidate));
        return candidate;
    }
}
