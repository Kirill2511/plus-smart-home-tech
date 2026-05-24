package ru.yandex.practicum.commerce.store.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProductNotFoundException> handle(ProductNotFoundException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
