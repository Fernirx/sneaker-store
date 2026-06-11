package com.fernirx.sneakerapi.customer.mapper;

import com.fernirx.sneakerapi.customer.dto.request.UpdateCustomerRequest;
import com.fernirx.sneakerapi.customer.dto.response.CustomerInternalResponse;
import com.fernirx.sneakerapi.customer.dto.response.CustomerResponse;
import com.fernirx.sneakerapi.customer.entity.Customer;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CustomerMapper {
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.userProfile.firstName")
    @Mapping(target = "lastName", source = "user.userProfile.lastName")
    @Mapping(target = "phone", source = "user.userProfile.phone")
    CustomerInternalResponse toInternalResponse(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCustomer(UpdateCustomerRequest request, @MappingTarget Customer customer);
}
