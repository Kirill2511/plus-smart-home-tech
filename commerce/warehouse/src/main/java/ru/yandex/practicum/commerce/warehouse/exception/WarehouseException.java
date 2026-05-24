package ru.yandex.practicum.commerce.warehouse.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WarehouseException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String userMessage;

    public WarehouseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.userMessage = message;
    }
}
