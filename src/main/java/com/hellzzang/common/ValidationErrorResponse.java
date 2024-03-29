package com.hellzzang.common;

import java.util.List;
import java.util.Map;

public class ValidationErrorResponse {
    private Map<String, List<String>> errors;

    public ValidationErrorResponse(Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}

