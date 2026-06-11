package ru.yandex.practicum.commerce.interaction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {
    @Valid
    @NotNull
    private ShoppingCartDto shoppingCart;

    @Valid
    @NotNull
    private AddressDto deliveryAddress;
}
