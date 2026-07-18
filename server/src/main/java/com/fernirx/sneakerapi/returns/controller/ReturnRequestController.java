package com.fernirx.sneakerapi.returns.controller;

import com.fernirx.sneakerapi.common.response.PageResponse;
import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import com.fernirx.sneakerapi.returns.dto.request.CreateReturnRequest;
import com.fernirx.sneakerapi.returns.dto.request.UpdateTrackingRequest;
import com.fernirx.sneakerapi.returns.dto.response.EligibleOrderItemResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestResponse;
import com.fernirx.sneakerapi.returns.service.ReturnRequestService;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/returns")
@RequiredArgsConstructor
@Tag(name = "My Return Request API", description = "Yêu cầu đổi/trả hàng của tôi")
public class ReturnRequestController {
    private final ReturnRequestService returnRequestService;

    @GetMapping("/eligible-items")
    @Operation(summary = "Danh sách sản phẩm trong đơn đủ điều kiện đổi/trả + variant có thể đổi sang")
    public ResponseEntity<SuccessResponse<List<EligibleOrderItemResponse>>> getEligibleItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderId) {
        return ResponseEntity.ok(SuccessResponse.of(
                returnRequestService.getEligibleItems(orderId, userDetails.getId())));
    }

    @PostMapping
    @Operation(summary = "Tạo yêu cầu đổi/trả hàng")
    public ResponseEntity<SuccessResponse<ReturnRequestResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReturnRequest request) {
        ReturnRequestResponse response = returnRequestService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessResponse.of(
                MessageUtil.getMessage("success.return.created"), response
        ));
    }

    @GetMapping
    @Operation(summary = "Danh sách yêu cầu đổi/trả của tôi (tùy chọn lọc theo orderId)")
    public ResponseEntity<PageResponse<ReturnRequestResponse>> getMyReturns(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long orderId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(PageResponse.of(returnRequestService.getMyReturns(userDetails.getId(), orderId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết yêu cầu đổi/trả của tôi")
    public ResponseEntity<SuccessResponse<ReturnRequestResponse>> getMyReturnDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(SuccessResponse.of(returnRequestService.getMyReturnDetail(id, userDetails.getId())));
    }

    @PatchMapping("/{id}/tracking")
    @Operation(summary = "Nhập mã vận đơn tự gửi hàng trả về (chỉ khi yêu cầu đã được duyệt)")
    public ResponseEntity<SuccessResponse<ReturnRequestResponse>> updateTracking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTrackingRequest request) {
        ReturnRequestResponse response = returnRequestService.updateTracking(id, userDetails.getId(), request);
        return ResponseEntity.ok(SuccessResponse.of(
                MessageUtil.getMessage("success.return.tracking_updated"), response
        ));
    }
}
