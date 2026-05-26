package ru.yandex.practicum.commerce.cart.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(CartException.class)
    public ResponseEntity<CartException> handle(CartException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
