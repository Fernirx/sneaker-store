package com.fernirx.sneakerapi.review.mapper;

import com.fernirx.sneakerapi.review.dto.request.UpdateReviewRequest;
import com.fernirx.sneakerapi.review.dto.response.ReviewImageResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewInternalResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewerResponse;
import com.fernirx.sneakerapi.review.entity.ProductReview;
import com.fernirx.sneakerapi.review.entity.ReviewImage;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ReviewMapper {

    ReviewImageResponse toImageResponse(ReviewImage image);

    @Mapping(target = "user", source = "author")
    @Mapping(target = "images", source = "images")
    ReviewResponse toResponse(ProductReview review, ReviewerResponse author, List<ReviewImageResponse> images);

    @Mapping(target = "productId", source = "review.product.id")
    @Mapping(target = "productName", source = "review.product.name")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "userEmail", source = "review.user.email")
    @Mapping(target = "orderId", source = "review.order.id")
    @Mapping(target = "orderCode", source = "review.order.code")
    @Mapping(target = "images", source = "images")
    ReviewInternalResponse toInternalResponse(ProductReview review, List<ReviewImageResponse> images);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "reviewImages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateReview(UpdateReviewRequest request, @MappingTarget ProductReview entity);
}
