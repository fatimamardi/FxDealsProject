package com.bloomberg.fxdeals.exception;

import com.bloomberg.fxdeals.controller.FxDealController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 * 
 * Provides centralized exception handling and consistent error responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("Validation errors: {}", errors);

        Map<String, Object> response = new HashMap<>();
        response.put("errorCode", "VALIDATION_ERROR");
        response.put("message", "Request validation failed");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FxDealController.ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        
        logger.error("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new FxDealController.ErrorResponse("INVALID_ARGUMENT", ex.getMessage()));
    }

    /**
     * Handle IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<FxDealController.ErrorResponse> handleIllegalStateException(
            IllegalStateException ex) {
        
        logger.error("Illegal state: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new FxDealController.ErrorResponse("ILLEGAL_STATE", ex.getMessage()));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<FxDealController.ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new FxDealController.ErrorResponse(
                "INTERNAL_ERROR", 
                "An unexpected error occurred: " + ex.getMessage()));
    }
}

