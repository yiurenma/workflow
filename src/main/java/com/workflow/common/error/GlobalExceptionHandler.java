package com.workflow.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final List<String> REQUEST_CORRELATION_HEADER_NAMES = List.of(
            "X-Request-Correlation-Id",
            "request-correlation-id",
            "X-Request-Id",
            "x-request-id",
            "X-Correlation-Id",
            "correlation-id"
    );

    private static final List<String> SESSION_CORRELATION_HEADER_NAMES = List.of(
            "X-Session-Correlation-Id",
            "session-correlation-id",
            "X-Session-Id",
            "x-session-id"
    );

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatusCode statusCode = ex.getStatusCode();
        String errorCode = resolveStatusName(statusCode);
        String errorDescription = StringUtils.hasText(ex.getReason()) ? ex.getReason() : errorCode;
        return buildResponse(statusCode, request, List.of(new ApiError(errorCode, errorDescription)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiError> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String description = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            errors.add(new ApiError("VALIDATION_ERROR", description));
        }
        ex.getBindingResult().getGlobalErrors().forEach(globalError ->
                errors.add(new ApiError("VALIDATION_ERROR", globalError.getDefaultMessage()))
        );
        if (errors.isEmpty()) {
            errors.add(new ApiError("VALIDATION_ERROR", "Validation failed"));
        }
        return buildResponse(HttpStatus.BAD_REQUEST, request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiError(
                        "VALIDATION_ERROR",
                        violation.getPropertyPath() + ": " + violation.getMessage()))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, request, errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String description = "Missing required parameter: " + ex.getParameterName();
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                request,
                List.of(new ApiError("MISSING_PARAMETER", description))
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String description = "Invalid value for parameter '" + ex.getName() + "'";
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                request,
                List.of(new ApiError("INVALID_PARAMETER", description))
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                request,
                List.of(new ApiError("INVALID_REQUEST_BODY", "Malformed request body"))
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                request,
                List.of(new ApiError("INTERNAL_SERVER_ERROR", "Unexpected internal error"))
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatusCode statusCode,
            HttpServletRequest request,
            List<ApiError> errors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                resolveRequestCorrelationId(request),
                resolveSessionCorrelationId(request),
                errors
        );
        return ResponseEntity.status(statusCode).body(body);
    }

    private String resolveRequestCorrelationId(HttpServletRequest request) {
        String headerValue = firstHeaderValue(request, REQUEST_CORRELATION_HEADER_NAMES);
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        Object requestAttribute = request.getAttribute("requestCorrelationId");
        if (requestAttribute instanceof String value && StringUtils.hasText(value)) {
            return value;
        }
        return UUID.randomUUID().toString();
    }

    private String resolveSessionCorrelationId(HttpServletRequest request) {
        String headerValue = firstHeaderValue(request, SESSION_CORRELATION_HEADER_NAMES);
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        Object requestAttribute = request.getAttribute("sessionCorrelationId");
        if (requestAttribute instanceof String value && StringUtils.hasText(value)) {
            return value;
        }
        HttpSession session = request.getSession(false);
        if (session != null && StringUtils.hasText(session.getId())) {
            return session.getId();
        }
        return UUID.randomUUID().toString();
    }

    private String firstHeaderValue(HttpServletRequest request, List<String> headerNames) {
        for (String headerName : headerNames) {
            String value = request.getHeader(headerName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String resolveStatusName(HttpStatusCode statusCode) {
        if (statusCode instanceof HttpStatus status) {
            return status.name();
        }
        return String.valueOf(statusCode.value());
    }
}
