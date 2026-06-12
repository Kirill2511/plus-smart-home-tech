package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AddressEmbeddable {
    private String country;
    private String city;
    private String street;
    private String house;
    private String flat;
}
