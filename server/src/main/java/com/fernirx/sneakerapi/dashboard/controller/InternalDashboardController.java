package com.fernirx.sneakerapi.dashboard.controller;

import com.fernirx.sneakerapi.common.response.SuccessResponse;
import com.fernirx.sneakerapi.dashboard.dto.response.DashboardSummaryResponse;
import com.fernirx.sneakerapi.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.fernirx.sneakerapi.security.model.CustomUserDetails;

@RestController
@RequestMapping("/internal/dashboard")
@RequiredArgsConstructor
@Tag(name = "Internal Dashboard API", description = "Số liệu tổng quan cho trang chủ Admin")
public class InternalDashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'SALE', 'WAREHOUSE')")
    @Operation(summary = "Tổng quan doanh thu, đơn hàng, tồn kho, sản phẩm bán chạy, khách hàng mới")
    public ResponseEntity<SuccessResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(SuccessResponse.of(dashboardService.getSummary(isAdmin)));
    }
}
