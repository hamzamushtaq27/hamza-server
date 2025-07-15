package com.dgsw.hamza.exception;

import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, List<String>> validationErrors;
    
    public ValidationException(String message, Map<String, List<String>> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    public Map<String, List<String>> getValidationErrors() {
        return validationErrors;
    }
}