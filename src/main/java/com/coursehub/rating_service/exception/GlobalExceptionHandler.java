package com.coursehub.rating_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> customHandler(RuntimeException exception) {
        return switch (exception) {
            case NotFoundException e -> status(NOT_FOUND).body(e.getMessage());
            case AccessDeniedException e -> status(FORBIDDEN).body(e.getMessage());
            default -> status(INTERNAL_SERVER_ERROR).body(exception.getMessage());
        };
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> globalHandler(Exception exception) {
        return status(INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }


}
