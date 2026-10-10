package com.fernirx.sneakerapi.coupon.mapper;

import com.fernirx.sneakerapi.coupon.dto.request.UpdateCouponRequest;
import com.fernirx.sneakerapi.coupon.dto.response.CouponInternalResponse;
import com.fernirx.sneakerapi.coupon.entity.Coupon;
import org.mapstruct.*;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CouponMapper {

    CouponInternalResponse toInternalResponse(Coupon coupon);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "discountType", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "couponUsages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCoupon(UpdateCouponRequest request, @MappingTarget Coupon entity);
}
