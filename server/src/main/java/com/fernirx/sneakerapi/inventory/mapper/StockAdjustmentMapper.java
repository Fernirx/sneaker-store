package com.fernirx.sneakerapi.inventory.mapper;

import com.fernirx.sneakerapi.inventory.dto.response.StockAdjustmentItemResponse;
import com.fernirx.sneakerapi.inventory.dto.response.StockAdjustmentResponse;
import com.fernirx.sneakerapi.inventory.entity.StockAdjustment;
import com.fernirx.sneakerapi.inventory.entity.StockAdjustmentItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface StockAdjustmentMapper {

    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "sku", source = "variant.sku")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "colorway", source = "variant.colorway")
    StockAdjustmentItemResponse toItemResponse(StockAdjustmentItem item);

    @Mapping(target = "createdById",
            expression = "java(adjustment.getCreatedBy() != null ? adjustment.getCreatedBy().getId() : null)")
    @Mapping(target = "createdByEmail",
            expression = "java(adjustment.getCreatedBy() != null ? adjustment.getCreatedBy().getEmail() : null)")
    @Mapping(target = "approvedById",
            expression = "java(adjustment.getApprovedBy() != null ? adjustment.getApprovedBy().getId() : null)")
    @Mapping(target = "approvedByEmail",
            expression = "java(adjustment.getApprovedBy() != null ? adjustment.getApprovedBy().getEmail() : null)")
    @Mapping(target = "items", source = "items")
    StockAdjustmentResponse toResponse(StockAdjustment adjustment, List<StockAdjustmentItemResponse> items);
}
