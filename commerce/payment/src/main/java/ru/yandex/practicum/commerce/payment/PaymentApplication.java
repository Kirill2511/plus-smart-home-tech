package ru.yandex.practicum.commerce.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.commerce.interaction.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;

@SpringBootApplication
@EnableFeignClients(clients = {OrderClient.class, ShoppingStoreClient.class})
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
