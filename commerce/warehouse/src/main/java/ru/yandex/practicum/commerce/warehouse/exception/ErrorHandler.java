package ru.yandex.practicum.commerce.warehouse.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(WarehouseException.class)
    public ResponseEntity<WarehouseException> handle(WarehouseException exception) {
        return ResponseEntity.status(exception.getHttpStatus()).body(exception);
    }
}
