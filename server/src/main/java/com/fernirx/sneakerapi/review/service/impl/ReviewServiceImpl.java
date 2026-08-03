package com.fernirx.sneakerapi.review.service.impl;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.exception.BusinessException;
import com.fernirx.sneakerapi.common.exception.SecurityCustomException;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.service.OrderService;
import com.fernirx.sneakerapi.product.entity.Product;
import com.fernirx.sneakerapi.product.service.ProductService;
import com.fernirx.sneakerapi.review.dto.request.CreateReviewRequest;
import com.fernirx.sneakerapi.review.dto.request.InternalReviewFilterRequest;
import com.fernirx.sneakerapi.review.dto.request.UpdateReviewRequest;
import com.fernirx.sneakerapi.review.dto.response.ReviewImageResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewInternalResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewSummaryResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewerResponse;
import com.fernirx.sneakerapi.review.entity.ProductReview;
import com.fernirx.sneakerapi.review.entity.ReviewImage;
import com.fernirx.sneakerapi.review.mapper.ReviewMapper;
import com.fernirx.sneakerapi.review.repository.InternalReviewSpec;
import com.fernirx.sneakerapi.review.repository.ProductReviewRepository;
import com.fernirx.sneakerapi.review.repository.ReviewImageRepository;
import com.fernirx.sneakerapi.review.service.ReviewService;
import com.fernirx.sneakerapi.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewMapper reviewMapper;
    private final ProductService productService;
    private final OrderService orderService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lấy danh sách Đánh giá (Review) đã được duyệt cho Storefront.
     * Áp dụng kỹ thuật Batch Fetching để gom nhóm hình ảnh của review, chống N+1 query.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getApprovedReviews(String productSlug, Pageable pageable) {
        Product product = productService.findActiveBySlug(productSlug);
        Page<ProductReview> page = productReviewRepository.findApprovedByProductId(product.getId(), pageable);

        List<Long> reviewIds = page.getContent().stream().map(ProductReview::getId).toList();
        Map<Long, List<ReviewImageResponse>> imagesByReview = mapImagesByReviewId(reviewIds);

        return page.map(review -> reviewMapper.toResponse(
                review,
                ReviewerResponse.from(review.getUser()),
                imagesByReview.getOrDefault(review.getId(), List.of())));
    }

    /**
     * Tính toán tổng quan đánh giá (Review Summary).
     * Tạo sẵn dải phân bổ từ 5 sao tới 1 sao để tránh bị thiếu cột nếu không có lượt đánh giá nào.
     * Tính trung bình điểm số làm tròn tới 1 chữ số thập phân bằng Java thay vì query phức tạp.
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(String productSlug) {
        Product product = productService.findActiveBySlug(productSlug);
        List<Object[]> rows = productReviewRepository.countByRatingForProduct(product.getId());

        Map<Short, Long> distribution = new LinkedHashMap<>();
        for (short star = 5; star >= 1; star--) {
            distribution.put(star, 0L);
        }

        long totalReviews = 0;
        double weightedSum = 0;
        for (Object[] row : rows) {
            Short rating = ((Number) row[0]).shortValue();
            Long count = ((Number) row[1]).longValue();
            distribution.put(rating, count);
            totalReviews += count;
            weightedSum += rating * count;
        }

        Double averageRating = totalReviews == 0 ? null : Math.round((weightedSum / totalReviews) * 10) / 10.0;
        return new ReviewSummaryResponse(averageRating, totalReviews, distribution);
    }

    /**
     * Khách hàng tạo Đánh giá mới cho sản phẩm.
     * Quy tắc bảo vệ (Chống fake review):
     * 1. Mỗi User chỉ được đánh giá 1 Sản phẩm đúng 1 lần (existsByUser_IdAndProduct_Id).
     * 2. Chỉ được đánh giá khi khách hàng ĐÃ MUA và ĐƠN HÀNG ĐÃ GIAO THÀNH CÔNG (findDeliveredOrderForProduct).
     */
    @Override
    public ReviewResponse createReview(Long userId, CreateReviewRequest request) {
        Product product = productService.findEntityById(request.productId());

        if (productReviewRepository.existsByUser_IdAndProduct_Id(userId, request.productId())) {
            throw BusinessException.alreadyExists("label.review");
        }

        Order order = orderService.findDeliveredOrderForProduct(userId, request.productId())
                .orElseThrow(() -> BusinessException.of(ErrorCode.REVIEW_NOT_ELIGIBLE));

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(entityManager.getReference(User.class, userId));
        review.setOrder(order);
        review.setRating(request.rating());
        review.setTitle(request.title());
        review.setComment(request.comment());
        review.setApproved(true);
        review = productReviewRepository.save(review);

        List<ReviewImageResponse> images = saveImages(review, request.imagePublicIds());

        return reviewMapper.toResponse(review, ReviewerResponse.from(review.getUser()), images);
    }

    /**
     * Khách hàng sửa Đánh giá.
     * Quy tắc bảo vệ: Nếu User sửa lại nội dung hoặc hình ảnh, Đánh giá sẽ BỊ TƯỚC QUYỀN APPROVED (setApproved = false).
     * Phải chờ Admin duyệt lại mới được hiện lên Storefront. Chống việc user sửa thành Spam/Quảng cáo sau khi đã duyệt.
     */
    @Override
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        ProductReview review = findOwnedReview(userId, reviewId);
        reviewMapper.updateReview(request, review);
        review.setApproved(false);
        review = productReviewRepository.save(review);

        List<ReviewImageResponse> images;
        if (request.imagePublicIds() != null) {
            reviewImageRepository.deleteByReview_Id(reviewId);
            reviewImageRepository.flush();
            images = saveImages(review, request.imagePublicIds());
        } else {
            images = mapImagesByReviewId(List.of(reviewId)).getOrDefault(reviewId, List.of());
        }

        return reviewMapper.toResponse(review, ReviewerResponse.from(review.getUser()), images);
    }

    /**
     * Khách hàng xóa Đánh giá.
     */
    @Override
    public void deleteReview(Long userId, Long reviewId) {
        ProductReview review = findOwnedReview(userId, reviewId);
        productReviewRepository.delete(review);
    }

    /**
     * Admin/Staff lấy danh sách Đánh giá nội bộ.
     * Áp dụng Batch Fetching lấy hình ảnh giống getApprovedReviews.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewInternalResponse> getAll(InternalReviewFilterRequest filter, Pageable pageable) {
        Page<ProductReview> page = productReviewRepository.findAll(InternalReviewSpec.build(filter), pageable);
        List<Long> reviewIds = page.getContent().stream().map(ProductReview::getId).toList();
        Map<Long, List<ReviewImageResponse>> imagesByReview = mapImagesByReviewId(reviewIds);
        return page.map(review -> reviewMapper.toInternalResponse(
                review, imagesByReview.getOrDefault(review.getId(), List.of())));
    }

    /**
     * Admin/Staff lấy chi tiết Đánh giá.
     */
    @Override
    @Transactional(readOnly = true)
    public ReviewInternalResponse getById(Long id) {
        ProductReview review = findById(id);
        List<ReviewImageResponse> images = mapImagesByReviewId(List.of(id)).getOrDefault(id, List.of());
        return reviewMapper.toInternalResponse(review, images);
    }

    /**
     * Admin/Staff Duyệt hoặc Ẩn Đánh giá.
     */
    @Override
    public ReviewInternalResponse setApproved(Long id, boolean approved) {
        ProductReview review = findById(id);
        review.setApproved(approved);
        review = productReviewRepository.save(review);
        List<ReviewImageResponse> images = mapImagesByReviewId(List.of(id)).getOrDefault(id, List.of());
        return reviewMapper.toInternalResponse(review, images);
    }

    /**
     * Admin/Staff Xóa Đánh giá cứng khỏi DB.
     */
    @Override
    public void delete(Long id) {
        productReviewRepository.delete(findById(id));
    }

    // ---- Helpers ----

    /**
     * Helper tìm đánh giá và xác minh tính sở hữu.
     */
    private ProductReview findOwnedReview(Long userId, Long reviewId) {
        ProductReview review = findById(reviewId);
        if (!review.getUser().getId().equals(userId)) {
            throw SecurityCustomException.forbidden();
        }
        return review;
    }

    /**
     * Helper tìm đánh giá chung.
     */
    private ProductReview findById(Long id) {
        return productReviewRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("label.review"));
    }

    /**
     * Helper lưu danh sách ảnh của đánh giá.
     */
    private List<ReviewImageResponse> saveImages(ProductReview review, List<String> imagePublicIds) {
        if (CollectionUtils.isEmpty(imagePublicIds)) {
            return List.of();
        }
        List<ReviewImage> images = new ArrayList<>();
        for (int i = 0; i < imagePublicIds.size(); i++) {
            ReviewImage image = new ReviewImage();
            image.setReview(review);
            image.setImagePublicId(imagePublicIds.get(i));
            image.setDisplayOrder(i);
            images.add(image);
        }
        return reviewImageRepository.saveAll(images).stream().map(reviewMapper::toImageResponse).toList();
    }

    /**
     * Helper tối ưu (Batch Fetching) để lấy ảnh của 1 list ReviewIDs thay vì dùng Lazy Loading chọc DB từng cái.
     */
    private Map<Long, List<ReviewImageResponse>> mapImagesByReviewId(List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }
        return reviewImageRepository.findByReview_IdInOrderByReview_IdAscDisplayOrderAsc(reviewIds).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getReview().getId(),
                        Collectors.mapping(reviewMapper::toImageResponse, Collectors.toList())));
    }
}
