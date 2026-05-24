package ru.yandex.practicum.commerce.cart.exception;

import org.springframework.http.HttpStatus;

public class CartException extends RuntimeException {
    private final HttpStatus httpStatus;

    public CartException(String message, HttpStatus httpStatus) {
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
