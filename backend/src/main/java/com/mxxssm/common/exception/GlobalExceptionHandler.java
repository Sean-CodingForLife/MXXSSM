package com.mxxssm.common.exception;

import com.mxxssm.common.api.ApiResponse;
import com.mxxssm.common.api.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception,
            HttpServletRequest request) {
        ErrorCode code = exception.getErrorCode();
        String message = localize(code, exception.getArgs());
        if (code.status().is5xxServerError()) {
            log.error("Business failure at {} {}: {}", request.getMethod(), request.getRequestURI(),
                    exception.getMessage(), exception);
        } else {
            log.debug("Business failure at {} {}: {}", request.getMethod(), request.getRequestURI(), code);
        }
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ValidationError>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationError)
                .collect(Collectors.toList());
        return validationResponse(errors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<List<ValidationError>>> handleBindException(BindException exception) {
        List<ValidationError> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationError)
                .collect(Collectors.toList());
        return validationResponse(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<ValidationError>>> handleConstraintViolation(
            ConstraintViolationException exception) {
        List<ValidationError> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.toList());
        return validationResponse(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<List<ValidationError>>> handleHandlerMethodValidation(
            HandlerMethodValidationException exception) {
        List<ValidationError> errors = exception.getAllErrors().stream()
                .map(error -> new ValidationError("request", error.getDefaultMessage()))
                .collect(Collectors.toList());
        return validationResponse(errors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        ErrorCode code = ErrorCode.BAD_REQUEST;
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, localize(code)));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException exception) {
        ErrorCode code = ErrorCode.AUTH_REQUIRED;
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, localize(code)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        ErrorCode code = ErrorCode.ACCESS_DENIED;
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, localize(code)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException exception) {
        ErrorCode code = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, localize(code)));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException exception,
            HttpServletRequest request) {
        log.error("Database failure at {} {}", request.getMethod(), request.getRequestURI(), exception);
        ErrorCode code = ErrorCode.DATABASE_ERROR;
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, localize(code)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected failure at {} {}", request.getMethod(), request.getRequestURI(), exception);
        ErrorCode code = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(code.status()).body(ApiResponse.failure(code, localize(code)));
    }

    private ResponseEntity<ApiResponse<List<ValidationError>>> validationResponse(List<ValidationError> errors) {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        return ResponseEntity.status(code.status())
                .body(ApiResponse.failure(code, localize(code), errors));
    }

    private ValidationError toValidationError(FieldError fieldError) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(fieldError, locale);
        if (message == null || message.isBlank()) {
            message = fieldError.getDefaultMessage();
        }
        return new ValidationError(fieldError.getField(), message);
    }

    private String localize(ErrorCode code, Object... args) {
        return messageSource.getMessage("error." + code.name(), args, code.name(), LocaleContextHolder.getLocale());
    }

    public record ValidationError(String field, String message) {
    }
}
