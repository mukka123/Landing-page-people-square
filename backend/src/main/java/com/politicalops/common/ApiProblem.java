package com.politicalops.common;

import java.time.Instant;

/**
 * Problem Details (RFC 7807) compatible error body, per the HLD API conventions:
 * "Problem Details compatible errors with request ID, safe message and field validation
 * details." Never includes stack traces or internal exception messages.
 */
public record ApiProblem(
        String type,
        String title,
        int status,
        String detail,
        String requestId,
        Instant timestamp,
        java.util.List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ApiProblem of(String title, int status, String detail, String requestId) {
        return new ApiProblem("about:blank", title, status, detail, requestId, Instant.now(), java.util.List.of());
    }
}
