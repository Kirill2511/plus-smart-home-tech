package ru.yandex.practicum.commerce.payment.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<PaymentException> handle(PaymentException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
