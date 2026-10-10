package com.fernirx.sneakerapi.supplier.mapper;

import com.fernirx.sneakerapi.supplier.dto.response.PurchaseItemResponse;
import com.fernirx.sneakerapi.supplier.dto.response.PurchaseResponse;
import com.fernirx.sneakerapi.supplier.entity.Purchase;
import com.fernirx.sneakerapi.supplier.entity.PurchaseItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface PurchaseMapper {

    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "sku", source = "variant.sku")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "size", source = "variant.size")
    @Mapping(target = "colorway", source = "variant.colorway")
    PurchaseItemResponse toItemResponse(PurchaseItem item);

    @Mapping(target = "supplierId", source = "purchase.supplier.id")
    @Mapping(target = "supplierName", source = "purchase.supplier.name")
    @Mapping(target = "createdById",
            expression = "java(purchase.getCreatedBy() != null ? purchase.getCreatedBy().getId() : null)")
    @Mapping(target = "createdByEmail",
            expression = "java(purchase.getCreatedBy() != null ? purchase.getCreatedBy().getEmail() : null)")
    @Mapping(target = "receivedById",
            expression = "java(purchase.getReceivedBy() != null ? purchase.getReceivedBy().getId() : null)")
    @Mapping(target = "receivedByEmail",
            expression = "java(purchase.getReceivedBy() != null ? purchase.getReceivedBy().getEmail() : null)")
    @Mapping(target = "items", source = "items")
    PurchaseResponse toResponse(Purchase purchase, List<PurchaseItemResponse> items);
}
