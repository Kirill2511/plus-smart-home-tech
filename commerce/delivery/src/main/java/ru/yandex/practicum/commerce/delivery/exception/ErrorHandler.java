package ru.yandex.practicum.commerce.delivery.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(DeliveryException.class)
    public ResponseEntity<DeliveryException> handle(DeliveryException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
