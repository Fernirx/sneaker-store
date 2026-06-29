package com.fernirx.sneakerapi.order.dto.request;

import com.fernirx.sneakerapi.common.annotation.NullableNotBlank;
import com.fernirx.sneakerapi.common.annotation.ValidName;
import com.fernirx.sneakerapi.common.annotation.ValidPhone;
import com.fernirx.sneakerapi.order.enums.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 200, message = "{validation.size.max}")
        @ValidName
        String recipientName,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 20, message = "{validation.size.max}")
        @ValidPhone
        String recipientPhone,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 255, message = "{validation.size.max}")
        String shippingStreet,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String shippingWard,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 50, message = "{validation.size.max}")
        String shippingWardCode,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 100, message = "{validation.size.max}")
        String shippingProvince,

        @NotBlank(message = "{validation.field.not_blank}")
        @Size(max = 50, message = "{validation.size.max}")
        String shippingProvinceCode,

        @NotNull(message = "{validation.field.not_blank}")
        PaymentMethod paymentMethod,

        @NullableNotBlank
        @Size(max = 50, message = "{validation.size.max}")
        String couponCode,

        String note,

        // Bắt buộc nếu là guest (userId == null) — validate trong service vì phụ thuộc context
        @NullableNotBlank
        @Email(message = "{validation.format.invalid}")
        String guestEmail,

        @NullableNotBlank
        String otpCode
) {}
