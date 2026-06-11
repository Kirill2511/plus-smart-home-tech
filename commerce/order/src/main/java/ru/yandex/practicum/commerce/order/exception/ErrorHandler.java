package ru.yandex.practicum.commerce.order.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<OrderException> handle(OrderException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
