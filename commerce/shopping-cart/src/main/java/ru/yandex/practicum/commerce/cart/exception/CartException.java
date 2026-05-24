package ru.yandex.practicum.commerce.cart.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CartException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String userMessage;

    public CartException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.userMessage = message;
    }
}
