package ru.yandex.practicum.commerce.store.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }

    public String getUserMessage() {
        return getMessage();
    }
}
