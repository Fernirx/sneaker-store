package com.fernirx.sneakerapi.customer.mapper;

import com.fernirx.sneakerapi.customer.dto.request.CreateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.request.UpdateAddressRequest;
import com.fernirx.sneakerapi.customer.dto.response.AddressResponse;
import com.fernirx.sneakerapi.customer.entity.Address;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface AddressMapper {
    AddressResponse toResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toAddress(CreateAddressRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAddress(UpdateAddressRequest request, @MappingTarget Address address);
}
