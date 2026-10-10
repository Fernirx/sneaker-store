package com.fernirx.sneakerapi.review.service;

import com.fernirx.sneakerapi.review.dto.request.CreateCommentRequest;
import com.fernirx.sneakerapi.review.dto.request.InternalCommentFilterRequest;
import com.fernirx.sneakerapi.review.dto.request.UpdateCommentRequest;
import com.fernirx.sneakerapi.review.dto.response.CommentInternalResponse;
import com.fernirx.sneakerapi.review.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    // Public
    Page<CommentResponse> getApprovedCommentTree(String productSlug, Pageable pageable);

    // Me
    CommentResponse createComment(Long userId, CreateCommentRequest request);
    CommentResponse updateComment(Long userId, Long commentId, UpdateCommentRequest request);
    void deleteComment(Long userId, Long commentId);

    // Internal
    Page<CommentInternalResponse> getAll(InternalCommentFilterRequest filter, Pageable pageable);
    CommentInternalResponse getById(Long id);
    CommentInternalResponse setApproved(Long id, boolean approved);
    void delete(Long id);
}
