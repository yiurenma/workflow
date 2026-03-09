package com.workflow.common.error;

import java.util.List;

public record ApiErrorResponse(
        String requestCorrelationId,
        String sessionCorrelationId,
        List<ApiError> errors
) {
}
