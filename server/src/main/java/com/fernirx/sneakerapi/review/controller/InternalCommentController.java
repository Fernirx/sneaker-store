package com.fernirx.sneakerapi.review.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.review.dto.request.InternalCommentFilterRequest;
import com.fernirx.sneakerapi.review.dto.request.SetApprovedRequest;
import com.fernirx.sneakerapi.review.dto.response.CommentInternalResponse;
import com.fernirx.sneakerapi.review.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/comments")
@RequiredArgsConstructor
@Tag(name = "Internal Comment API", description = "Quản lý bình luận (nội bộ)")
public class InternalCommentController {
    private final CommentService commentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Danh sách bình luận")
    public ResponseEntity<PageResponse<CommentInternalResponse>> getAll(
            @ParameterObject @ModelAttribute InternalCommentFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(commentService.getAll(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Chi tiết bình luận")
    public ResponseEntity<SuccessResponse<CommentInternalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(commentService.getById(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Duyệt/Ẩn bình luận")
    public ResponseEntity<SuccessResponse<CommentInternalResponse>> setApproved(
            @PathVariable Long id, @Valid @RequestBody SetApprovedRequest request) {
        CommentInternalResponse response = commentService.setApproved(id, request.approved());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.comment")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa bình luận")
    public ResponseEntity<SuccessResponse<Void>> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.comment"))
        ));
    }
}
