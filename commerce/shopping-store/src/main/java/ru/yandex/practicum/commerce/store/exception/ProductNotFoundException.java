package ru.yandex.practicum.commerce.store.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    private final String userMessage;

    public ProductNotFoundException(String message) {
        super(message);
        this.userMessage = message;
    }
}
