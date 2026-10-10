package com.fernirx.sneakerapi.collection.controller;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionResponse;
import com.fernirx.sneakerapi.collection.service.CollectionService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.product.dto.request.BySlugProductFilterRequest;
import com.fernirx.sneakerapi.product.dto.response.ProductResponse;
import com.fernirx.sneakerapi.product.service.ProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
@Tag(name = "Collection API", description = "Bộ sưu tập sản phẩm")
public class CollectionController {
    private final CollectionService collectionService;
    private final ProductQueryService productQueryService;

    @GetMapping
    @Operation(summary = "Danh sách bộ sưu tập")
    public ResponseEntity<PageResponse<CollectionResponse>> getCollections(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                collectionService.getCollections(new CollectionFilterRequest(search, null), pageable)
        ));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết bộ sưu tập theo slug")
    public ResponseEntity<SuccessResponse<CollectionResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(SuccessResponse.of(collectionService.getBySlug(slug)));
    }

    @GetMapping("/{slug}/products")
    @Operation(summary = "Danh sách sản phẩm theo bộ sưu tập")
    public ResponseEntity<PageResponse<ProductResponse>> getProductsByCollection(
            @PathVariable String slug,
            @ParameterObject @ModelAttribute BySlugProductFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(
                productQueryService.getProductsByCollectionSlug(slug, filter, pageable)
        ));
    }
}
