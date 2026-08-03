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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductAssembler productAssembler;

    /**
     * Lấy Map ảnh đại diện (Primary Image) cho một danh sách Product IDs.
     * Tối ưu truy vấn N+1 khi hiển thị danh sách sản phẩm.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getPrimaryImageMap(List<Long> productIds) {
        return productImageRepository
                .findByProductIdInAndPrimaryImageTrueOrderByProductIdAscDisplayOrderAsc(productIds)
                .stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId() + ":" + img.getColorway(),
                        ProductImage::getImagePublicId,
                        (a, b) -> a
                ));
    }

    /**
     * Lấy danh sách ảnh của một sản phẩm, group theo màu sắc (Colorway).
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageGroupResponse> getImages(Long productId) {
        findProduct(productId);
        List<ProductImage> images = productImageRepository
                .findByProductIdOrderByColorwayAscDisplayOrderAsc(productId);
        return productAssembler.toImageGroups(images);
    }

    /**
     * Thêm ảnh mới cho một màu sắc (Colorway) của sản phẩm.
     * Quy tắc bảo vệ: 
     * - Nếu ảnh được đánh dấu là Primary (ảnh đại diện), hệ thống tự động gỡ cờ Primary 
     *   của các ảnh cũ cùng màu để đảm bảo mỗi màu chỉ có 1 ảnh đại diện duy nhất.
     */
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

    /**
     * Cập nhật thông tin ảnh (thay đổi trạng thái Primary hoặc thứ tự hiển thị).
     * Tương tự addImage, nếu đổi thành Primary thì tự động gỡ Primary của các ảnh khác cùng màu.
     */
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

    /**
     * Xóa ảnh khỏi sản phẩm.
     */
    @Override
    public void deleteImage(Long productId, Long imageId) {
        findProduct(productId);
        ProductImage image = findImage(productId, imageId);
        productImageRepository.delete(image);
    }

    /**
     * Helper tìm sản phẩm theo ID.
     */
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> BusinessException.notFound("label.product"));
    }

    /**
     * Helper tìm ảnh theo ID và phải thuộc về Product tương ứng.
     */
    private ProductImage findImage(Long productId, Long imageId) {
        return productImageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> BusinessException.notFound("label.product.image"));
    }
}
