package com.fernirx.sneakerapi.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    private String startField;
    private String endField;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startField = constraintAnnotation.startField();
        this.endField = constraintAnnotation.endField();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        try {
            Field startF = getField(value.getClass(), startField);
            Field endF = getField(value.getClass(), endField);
            
            if (startF == null || endF == null) return true;
            
            startF.setAccessible(true);
            endF.setAccessible(true);

            Comparable start = (Comparable) startF.get(value);
            Comparable end = (Comparable) endF.get(value);

            if (start == null || end == null) {
                return true;
            }

            if (start.compareTo(end) > 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(endField)
                        .addConstraintViolation();
                return false;
            }

            return true;
        } catch (Exception e) {
            return true;
        }
    }

    private Field getField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
