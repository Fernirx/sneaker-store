package com.fernirx.sneakerapi.collection.controller;

import com.fernirx.sneakerapi.collection.dto.request.CollectionFilterRequest;
import com.fernirx.sneakerapi.collection.dto.request.CreateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.request.SlugUpdateRequest;
import com.fernirx.sneakerapi.collection.dto.request.UpdateCollectionRequest;
import com.fernirx.sneakerapi.collection.dto.response.CollectionInternalResponse;
import com.fernirx.sneakerapi.collection.service.CollectionService;
import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
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
@RequestMapping("/internal/collections")
@RequiredArgsConstructor
@Tag(name = "Internal Collection API", description = "Quản lý bộ sưu tập (nội bộ)")
public class InternalCollectionController {
    private final CollectionService collectionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Danh sách bộ sưu tập")
    public ResponseEntity<PageResponse<CollectionInternalResponse>> getInternalCollections(
            @ParameterObject @ModelAttribute CollectionFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(collectionService.getInternalCollections(filter, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'MARKETING')")
    @Operation(summary = "Chi tiết bộ sưu tập")
    public ResponseEntity<SuccessResponse<CollectionInternalResponse>> getInternalById(@PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(collectionService.getInternalById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo bộ sưu tập mới")
    public ResponseEntity<SuccessResponse<CollectionInternalResponse>> createCollection(
            @Valid @RequestBody CreateCollectionRequest request) {
        CollectionInternalResponse response = collectionService.createCollection(request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.created", MessageUtil.getMessage("label.collection")),
                response
        ));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MARKETING')")
    @Operation(summary = "Cập nhật bộ sưu tập")
    public ResponseEntity<SuccessResponse<CollectionInternalResponse>> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCollectionRequest request) {
        CollectionInternalResponse response = collectionService.updateCollection(id, request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.collection")),
                response
        ));
    }

    @PatchMapping("/{id}/slug")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật slug bộ sưu tập")
    public ResponseEntity<SuccessResponse<CollectionInternalResponse>> updateCollectionSlug(
            @PathVariable Long id,
            @Valid @RequestBody SlugUpdateRequest request) {
        CollectionInternalResponse response = collectionService.updateCollectionSlug(id, request.slug());
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.updated", MessageUtil.getMessage("label.collection")),
                response
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa bộ sưu tập")
    public ResponseEntity<SuccessResponse<Void>> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.resource.deleted", MessageUtil.getMessage("label.collection"))
        ));
    }
}
