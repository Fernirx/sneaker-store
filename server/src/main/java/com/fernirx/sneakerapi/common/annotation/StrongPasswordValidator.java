package com.fernirx.sneakerapi.common.annotation;

import com.fernirx.sneakerapi.common.constant.PatternConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches(PatternConstants.PASSWORD);
    }
}
