package com.fernirx.sneakerapi.order.mapper;

import com.fernirx.sneakerapi.order.dto.response.OrderInternalResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderItemResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderResponse;
import com.fernirx.sneakerapi.order.dto.response.OrderStatusHistoryResponse;
import com.fernirx.sneakerapi.order.entity.Order;
import com.fernirx.sneakerapi.order.entity.OrderItem;
import com.fernirx.sneakerapi.order.entity.OrderStatusHistory;
import com.fernirx.sneakerapi.shipping.dto.response.ShipmentResponse;
import com.fernirx.sneakerapi.shipping.entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface OrderMapper {

    @Mapping(target = "variantId", source = "variant.id")
    OrderItemResponse toItemResponse(OrderItem item);

    OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history);

    ShipmentResponse toShipmentResponse(Shipment shipment);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "shipment", source = "shipment")
    OrderResponse toResponse(Order order, List<OrderItemResponse> items, Shipment shipment);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "updatedAt", source = "order.updatedAt")
    @Mapping(target = "customerEmail",
            expression = "java(order.getCustomer() != null ? order.getCustomer().getUser().getEmail() : null)")
    @Mapping(target = "assignedToId",
            expression = "java(order.getAssignedTo() != null ? order.getAssignedTo().getId() : null)")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "shipment", source = "shipment")
    OrderInternalResponse toInternalResponse(Order order, List<OrderItemResponse> items, Shipment shipment);
}
