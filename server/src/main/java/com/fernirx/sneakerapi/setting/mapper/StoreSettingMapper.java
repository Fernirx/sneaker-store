package com.fernirx.sneakerapi.setting.mapper;

import com.fernirx.sneakerapi.setting.dto.request.CreateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.request.UpdateStoreSettingRequest;
import com.fernirx.sneakerapi.setting.dto.response.StoreSettingResponse;
import com.fernirx.sneakerapi.setting.entity.StoreSetting;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface StoreSettingMapper {
    StoreSettingResponse toResponse(StoreSetting entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StoreSetting toStoreSetting(CreateStoreSettingRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateStoreSetting(UpdateStoreSettingRequest request, @MappingTarget StoreSetting entity);
}
