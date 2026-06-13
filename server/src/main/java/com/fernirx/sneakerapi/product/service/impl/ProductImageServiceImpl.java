package com.fernirx.sneakerapi.product.service.impl;

import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.product.assembler.ProductAssembler;
import com.fernirx.sneakerapi.product.dto.request.AddImageRequest;
import com.fernirx.sneakerapi.product.dto.request.UpdateImageRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductImageGroupResponse;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.entity.ProductImage;
import com.fernirx.sneakerapi.product.repository.ProductImageRepository;
import com.fernirx.sneakerapi.product.repository.ProductRepository;
import com.fernirx.sneakerapi.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductAssembler productAssembler;

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageGroupResponse> getImages(Long productId) {
        findProduct(productId);
        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByColorwayAscDisplayOrderAsc(productId);
        return productAssembler.toImageGroups(images);
    }

    @Override
    public ProductImageGroupResponse.ImageResponse addImage(Long productId, AddImageRequest request) {
        Product product = findProduct(productId);

        boolean isPrimary = Boolean.TRUE.equals(request.primaryImage());
        if (isPrimary) {
            productImageRepository.clearPrimaryByProductAndColorway(productId, request.colorway());
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setColorway(request.colorway());
        image.setColorHex(request.colorHex());
        image.setImagePublicId(request.imagePublicId());
        image.setPrimaryImage(isPrimary);
        image.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);

        ProductImage saved = productImageRepository.save(image);
        return new ProductImageGroupResponse.ImageResponse(
                saved.getId(),
                saved.getImagePublicId(),
                saved.getPrimaryImage(),
                saved.getDisplayOrder()
        );
    }

    @Override
    public ProductImageGroupResponse.ImageResponse updateImage(Long productId, Long imageId, UpdateImageRequest request) {
        findProduct(productId);
        ProductImage image = findImage(productId, imageId);

        if (Boolean.TRUE.equals(request.primaryImage()) && !Boolean.TRUE.equals(image.getPrimaryImage())) {
            productImageRepository.clearPrimaryByProductAndColorway(productId, image.getColorway());
            image.setPrimaryImage(true);
        } else if (Boolean.FALSE.equals(request.primaryImage())) {
            image.setPrimaryImage(false);
        }

        if (request.displayOrder() != null) {
            image.setDisplayOrder(request.displayOrder());
        }

        ProductImage saved = productImageRepository.save(image);
        return new ProductImageGroupResponse.ImageResponse(
                saved.getId(),
                saved.getImagePublicId(),
                saved.getPrimaryImage(),
                saved.getDisplayOrder()
        );
    }

    @Override
    public void deleteImage(Long productId, Long imageId) {
        findProduct(productId);
        ProductImage image = findImage(productId, imageId);
        productImageRepository.delete(image);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }

    private ProductImage findImage(Long productId, Long imageId) {
        return productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> BusinessException.notFound("label.product.image"));
    }
}
