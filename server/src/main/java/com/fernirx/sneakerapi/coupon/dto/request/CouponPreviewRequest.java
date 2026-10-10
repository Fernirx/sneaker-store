package com.fernirx.sneakerapi.coupon.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CouponPreviewRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        String code,

        @NotNull(message = "{validation.field.not_blank}")
        @Positive(message = "{validation.number.positive}")
        BigDecimal orderAmount,

        // Optional - có thì preview check luôn giới hạn dùng theo khách hàng (giống validate() lúc tạo đơn
        // thật), tránh khách thấy "áp dụng thành công" ở Checkout rồi bị từ chối lúc bấm Đặt hàng.
        @NullableNotBlank
        @Email(message = "{validation.format.invalid}")
        String email,

        @NullableNotBlank
        String phone
) {}
