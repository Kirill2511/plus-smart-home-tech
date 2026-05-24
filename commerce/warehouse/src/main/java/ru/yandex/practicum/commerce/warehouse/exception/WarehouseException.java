package ru.yandex.practicum.commerce.warehouse.exception;

import org.springframework.http.HttpStatus;

public class WarehouseException extends RuntimeException {
    private final HttpStatus httpStatus;

    public WarehouseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getUserMessage() {
        return getMessage();
    }
}
