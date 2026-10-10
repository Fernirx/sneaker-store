package com.fernirx.sneakerapi.returns.mapper;

import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestInternalResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestItemResponse;
import com.fernirx.sneakerapi.returns.dto.response.ReturnRequestResponse;
import com.fernirx.sneakerapi.returns.entity.ReturnRequest;
import com.fernirx.sneakerapi.returns.entity.ReturnRequestItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface ReturnRequestMapper {

    @Mapping(target = "orderItemId", source = "orderItem.id")
    @Mapping(target = "productName", source = "orderItem.productName")
    @Mapping(target = "variantSku", source = "orderItem.variantSku")
    @Mapping(target = "variantSize", source = "orderItem.variantSize")
    @Mapping(target = "variantColor", source = "orderItem.variantColor")
    @Mapping(target = "unitPrice", source = "orderItem.unitPrice")
    @Mapping(target = "exchangeVariantId",
            expression = "java(item.getExchangeVariant() != null ? item.getExchangeVariant().getId() : null)")
    @Mapping(target = "exchangeVariantSku",
            expression = "java(item.getExchangeVariant() != null ? item.getExchangeVariant().getSku() : null)")
    @Mapping(target = "exchangeVariantSize",
            expression = "java(item.getExchangeVariant() != null ? item.getExchangeVariant().getSize() : null)")
    @Mapping(target = "exchangeVariantColorway",
            expression = "java(item.getExchangeVariant() != null ? item.getExchangeVariant().getColorway() : null)")
    ReturnRequestItemResponse toItemResponse(ReturnRequestItem item);

    @Mapping(target = "orderId", source = "returnRequest.order.id")
    @Mapping(target = "orderCode", source = "returnRequest.order.code")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "imagePublicIds", source = "imagePublicIds")
    ReturnRequestResponse toResponse(ReturnRequest returnRequest, List<ReturnRequestItemResponse> items, List<String> imagePublicIds);

    @Mapping(target = "orderId", source = "returnRequest.order.id")
    @Mapping(target = "orderCode", source = "returnRequest.order.code")
    @Mapping(target = "customerId", source = "returnRequest.customer.id")
    @Mapping(target = "customerEmail", source = "returnRequest.customer.user.email")
    @Mapping(target = "approvedById",
            expression = "java(returnRequest.getApprovedBy() != null ? returnRequest.getApprovedBy().getId() : null)")
    @Mapping(target = "approvedByEmail",
            expression = "java(returnRequest.getApprovedBy() != null ? returnRequest.getApprovedBy().getEmail() : null)")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "imagePublicIds", source = "imagePublicIds")
    ReturnRequestInternalResponse toInternalResponse(ReturnRequest returnRequest, List<ReturnRequestItemResponse> items, List<String> imagePublicIds);
}
