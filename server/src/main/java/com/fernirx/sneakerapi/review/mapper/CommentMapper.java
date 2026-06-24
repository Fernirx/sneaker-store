package com.fernirx.sneakerapi.review.mapper;

import com.fernirx.sneakerapi.review.dto.request.UpdateCommentRequest;
import com.fernirx.sneakerapi.review.dto.response.CommentInternalResponse;
import com.fernirx.sneakerapi.review.dto.response.CommentResponse;
import com.fernirx.sneakerapi.review.dto.response.ReviewerResponse;
import com.fernirx.sneakerapi.review.entity.ProductComment;
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
public interface CommentMapper {

    @Mapping(target = "user", source = "author")
    @Mapping(target = "replies", source = "replies")
    CommentResponse toResponse(ProductComment comment, ReviewerResponse author, List<CommentResponse> replies);

    @Mapping(target = "productId", source = "comment.product.id")
    @Mapping(target = "productName", source = "comment.product.name")
    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "userEmail", source = "comment.user.email")
    @Mapping(target = "parentId", expression = "java(comment.getParent() != null ? comment.getParent().getId() : null)")
    CommentInternalResponse toInternalResponse(ProductComment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "productComments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateComment(UpdateCommentRequest request, @MappingTarget ProductComment entity);
}
