package com.fernirx.sneakerapi.common.exception;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.response.ErrorDetail;
import com.fernirx.sneakerapi.common.response.ErrorResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String finalMessage = MessageUtil.getMessage(ErrorCode.INTERNAL_SERVER_ERROR.getMessageKey());
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage), errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        String fieldName = ex.getName();
        String labelKey = "label." + fieldName;
        String translatedField = MessageUtil.getMessage(labelKey);
        String displayField = translatedField.equals(labelKey) ? fieldName : translatedField;
        String finalMessage = MessageUtil.getMessage(errorCode.getMessageKey(), displayField);
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage), errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        List<ErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        String finalMessage = MessageUtil.getMessage("error.bus.validation_summary");
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage, fieldErrors), errorCode.getHttpStatus());
    }
}
