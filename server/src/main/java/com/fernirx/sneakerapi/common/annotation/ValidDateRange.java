package com.fernirx.sneakerapi.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    String startField();
    String endField();

    String message() default "{validation.date.range_invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
