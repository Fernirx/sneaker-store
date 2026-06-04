package com.fernirx.sneakerapi.common.exception;

import com.fernirx.sneakerapi.common.enums.ErrorCode;
import com.fernirx.sneakerapi.common.response.ErrorDetail;
import com.fernirx.sneakerapi.common.response.ErrorResponse;
import com.fernirx.sneakerapi.common.utils.MessageUtil;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Lỗi không xác định — bắt tất cả exception chưa được xử lý cụ thể
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        String finalMessage = MessageUtil.getMessage(errorCode.getMessageKey());
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage), errorCode.getHttpStatus());
    }

    // Lỗi nghiệp vụ — ném ra từ service layer bằng BusinessException
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        String finalMessage = resolveMessage(ex.getErrorCode(), ex.getArgs());
        return new ResponseEntity<>(ErrorResponse.of(ex.getErrorCode(), finalMessage), ex.getErrorCode().getHttpStatus());
    }

    // Lỗi bảo mật — ném ra từ security filter hoặc service bằng SecurityCustomException
    @ExceptionHandler(SecurityCustomException.class)
    public ResponseEntity<ErrorResponse> handleSecurityCustomException(SecurityCustomException ex) {
        log.warn("Security exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        String finalMessage = resolveMessage(ex.getErrorCode(), ex.getArgs());
        return new ResponseEntity<>(ErrorResponse.of(ex.getErrorCode(), finalMessage), ex.getErrorCode().getHttpStatus());
    }

    // Lỗi sai kiểu dữ liệu trên path/query param
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch on field '{}': {}", ex.getName(), ex.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        String labelKey = "label." + ex.getName();
        String translatedField = MessageUtil.getMessage(labelKey);
        String displayField = translatedField.equals(labelKey) ? ex.getName() : translatedField;
        String finalMessage = MessageUtil.getMessage(errorCode.getMessageKey(), displayField);
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage), errorCode.getHttpStatus());
    }

    // Lỗi validation trên @RequestBody — bắt lỗi từ @Valid trên DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {} field error(s)", ex.getBindingResult().getFieldErrorCount());
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        List<ErrorDetail> errorDetails = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        String finalMessage = MessageUtil.getMessage("error.bus.validation_summary");
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage, errorDetails), errorCode.getHttpStatus());
    }

    // Lỗi validation trên @RequestParam / @PathVariable — bắt lỗi từ @Validated trên controller
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {} violation(s)", ex.getConstraintViolations().size());
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        List<ErrorDetail> errorDetails = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
                    return new ErrorDetail(fieldName, violation.getMessage());
                })
                .toList();
        String finalMessage = MessageUtil.getMessage("error.bus.validation_summary");
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage, errorDetails), errorCode.getHttpStatus());
    }

    // Lỗi cấu trúc JSON sai — request body không parse được hoặc sai định dạng ngày tháng
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        ErrorCode errorCode = ErrorCode.INVALID_DATA;
        String finalMessage = MessageUtil.getMessage("error.server.malformed_json");
        return new ResponseEntity<>(ErrorResponse.of(errorCode, finalMessage), errorCode.getHttpStatus());
    }

    // Dịch args của exception: nếu arg là String thì thử tra cứu i18n, không thì giữ nguyên
    private String resolveMessage(ErrorCode errorCode, Object[] args) {
        Object[] translatedArgs = Arrays.stream(args)
                .map(arg -> arg instanceof String s ? MessageUtil.getMessage(s) : arg)
                .toArray();
        return MessageUtil.getMessage(errorCode.getMessageKey(), translatedArgs);
    }
}