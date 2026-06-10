package com.fernirx.sneakerapi.customer.mapper;

import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CustomerMapper {
    CustomerResponse toResponse(Customer customer);
}
