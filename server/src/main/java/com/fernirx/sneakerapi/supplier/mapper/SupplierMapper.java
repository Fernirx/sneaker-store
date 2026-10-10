package com.fernirx.sneakerapi.supplier.mapper;

import com.fernirx.sneakerapi.supplier.dto.request.UpdateSupplierRequest;
import com.fernirx.sneakerapi.supplier.dto.response.SupplierResponse;
import com.fernirx.sneakerapi.supplier.entity.Supplier;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface SupplierMapper {

    SupplierResponse toResponse(Supplier supplier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchases", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateSupplier(UpdateSupplierRequest request, @MappingTarget Supplier supplier);
}
