package com.gamerzone.auth.exception;

import java.util.List;

public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String code,
        List<ApiFieldError> errors) {
}
