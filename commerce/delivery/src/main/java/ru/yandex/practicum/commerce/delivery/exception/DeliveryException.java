package ru.yandex.practicum.commerce.delivery.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeliveryException extends RuntimeException {
    private final HttpStatus httpStatus;

    public DeliveryException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
