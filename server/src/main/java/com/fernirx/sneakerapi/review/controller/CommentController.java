package com.fernirx.sneakerapi.review.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.review.dto.request.CreateCommentRequest;
import com.fernirx.sneakerapi.review.dto.request.UpdateCommentRequest;
import com.fernirx.sneakerapi.review.dto.response.CommentResponse;
import com.fernirx.sneakerapi.review.service.CommentService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/comments")
@RequiredArgsConstructor
@Tag(name = "My Comment API", description = "Quản lý bình luận của tôi")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Viết bình luận hoặc trả lời bình luận")
    public ResponseEntity<SuccessResponse<CommentResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.createComment(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.comment")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật bình luận của tôi")
    public ResponseEntity<SuccessResponse<CommentResponse>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentResponse response = commentService.updateComment(userDetails.getId(), id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.comment")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa bình luận của tôi")
    public ResponseEntity<SuccessResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        commentService.deleteComment(userDetails.getId(), id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.comment"))
        ));
    }
}
