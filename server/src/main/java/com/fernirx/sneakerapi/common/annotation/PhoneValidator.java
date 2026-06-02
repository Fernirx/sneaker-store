package com.fernirx.sneakerapi.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private boolean allowNull;

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return allowNull;
        return !value.isBlank() && value.matches("");
    }
}
