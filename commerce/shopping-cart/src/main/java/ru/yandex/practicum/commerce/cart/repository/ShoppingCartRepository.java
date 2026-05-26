package ru.yandex.practicum.commerce.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.cart.model.ShoppingCartEntity;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, UUID> {
    Optional<ShoppingCartEntity> findFirstByUsernameAndActiveTrue(String username);
}
