package com.fernirx.sneakerapi.common.annotation;

import com.fernirx.sneakerapi.common.constant.PatternConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NameValidator implements ConstraintValidator<ValidName, String> {
    private boolean allowNull;

    @Override
    public void initialize(ValidName constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return allowNull;
        return !value.isBlank() && value.matches(PatternConstants.NAME);
    }
}
