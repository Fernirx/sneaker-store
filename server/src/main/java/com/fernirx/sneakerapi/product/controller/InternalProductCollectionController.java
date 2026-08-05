package com.fernirx.sneakerapi.product.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.product.dto.request.AssignCollectionsRequest;
import com.fernirx.sneakerapi.product.dto.response.CollectionBriefResponse;
import com.fernirx.sneakerapi.product.service.ProductCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/products/{productId}/collections")
@RequiredArgsConstructor
@Tag(name = "Internal Product Collection API", description = "Quản lý bộ sưu tập sản phẩm (nội bộ)")
public class InternalProductCollectionController {

    private final ProductCollectionService productCollectionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Danh sách bộ sưu tập của sản phẩm")
    public ResponseEntity<SuccessResponse<List<CollectionBriefResponse>>> getCollections(
            @PathVariable Long productId) {
        return ResponseEntity.ok(SuccessResponse.of(productCollectionService.getCollections(productId)));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gán lại bộ sưu tập cho sản phẩm (thay thế toàn bộ)")
    public ResponseEntity<SuccessResponse<List<CollectionBriefResponse>>> assignCollections(
            @PathVariable Long productId,
            @Valid @RequestBody AssignCollectionsRequest request) {
        List<CollectionBriefResponse> response = productCollectionService.assignCollections(productId, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.collection")),
                response
        ));
    }
}
