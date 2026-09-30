package com.politicalops.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Every error response is Problem Details shaped and carries a request ID so a user can
 * report an error and an operator can find the matching audit/log entry, without ever
 * leaking an internal exception message (see application.yml server.error.include-message).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiProblem> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiProblem.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiProblem.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());
        ApiProblem body = new ApiProblem("about:blank", "Validation failed",
                HttpStatus.BAD_REQUEST.value(), "One or more fields failed validation",
                requestId(), java.time.Instant.now(), errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiProblem> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiProblem.of("Access denied", HttpStatus.FORBIDDEN.value(),
                        "You do not have permission to perform this action", requestId()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiProblem> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiProblem.of("Not found", HttpStatus.NOT_FOUND.value(), ex.getMessage(), requestId()));
    }

    @ExceptionHandler(ReconciliationException.class)
    public ResponseEntity<ApiProblem> handleReconciliation(ReconciliationException ex) {
        return ResponseEntity.unprocessableEntity()
                .body(ApiProblem.of("Reconciliation failed", 422, ex.getMessage(), requestId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiProblem> handleGeneric(Exception ex) {
        // Deliberately generic: real detail goes to logs/traces keyed by requestId, never to the client.
        return ResponseEntity.internalServerError()
                .body(ApiProblem.of("Unexpected error", 500, "An unexpected error occurred", requestId()));
    }

    private String requestId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        Object id = attrs == null ? null : attrs.getAttribute("requestId", RequestAttributes.SCOPE_REQUEST);
        return id == null ? "unknown" : id.toString();
    }
}
