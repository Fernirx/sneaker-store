package com.fernirx.sneakerapi.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordConfirmable> {
    @Override
    public boolean isValid(PasswordConfirmable value, ConstraintValidatorContext context) {
        if (value.password() == null || value.confirmPassword() == null) {
            return true;
        }
        boolean matches = value.password().equals(value.confirmPassword());
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return matches;
    }
}