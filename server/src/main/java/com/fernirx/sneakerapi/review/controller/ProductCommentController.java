package com.fernirx.sneakerapi.review.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.review.dto.response.CommentResponse;
import com.fernirx.sneakerapi.review.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products/{slug}/comments")
@RequiredArgsConstructor
@Tag(name = "Product Comment API", description = "Bình luận sản phẩm (công khai)")
public class ProductCommentController {
    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "Danh sách bình luận đã duyệt của sản phẩm")
    public ResponseEntity<PageResponse<CommentResponse>> getComments(
            @PathVariable String slug,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(commentService.getApprovedCommentTree(slug, pageable)));
    }
}
