package com.fernirx.sneakerapi.banner.mapper;

import com.fernirx.sneakerapi.banner.dto.request.UpdateBannerRequest;
import com.fernirx.sneakerapi.banner.dto.response.BannerInternalResponse;
import com.fernirx.sneakerapi.banner.dto.response.BannerResponse;
import com.fernirx.sneakerapi.banner.entity.Banner;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface BannerMapper {

    BannerResponse toResponse(Banner banner);

    BannerInternalResponse toInternalResponse(Banner banner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateBanner(UpdateBannerRequest request, @MappingTarget Banner banner);
}
