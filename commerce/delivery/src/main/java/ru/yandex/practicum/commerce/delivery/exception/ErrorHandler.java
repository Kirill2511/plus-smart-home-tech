package ru.yandex.practicum.commerce.delivery.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(DeliveryException.class)
    public ResponseEntity<DeliveryException> handle(DeliveryException exception) {
        log.error("Delivery exception with status {}: {}", exception.getHttpStatus(), exception.getMessage(), exception);
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
