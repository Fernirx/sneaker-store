package com.fernirx.sneakerapi.payment.mapper;

import com.fernirx.sneakerapi.payment.dto.response.PaymentInternalResponse;
import com.fernirx.sneakerapi.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.code")
    PaymentInternalResponse toInternalResponse(Payment payment);
}
