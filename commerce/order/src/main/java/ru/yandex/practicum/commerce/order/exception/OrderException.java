package ru.yandex.practicum.commerce.order.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OrderException extends RuntimeException {
    private final HttpStatus httpStatus;

    public OrderException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
