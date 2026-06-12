package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {
    @Id
    private UUID paymentId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private BigDecimal productTotal;

    @Column(nullable = false)
    private BigDecimal deliveryTotal;

    @Column(nullable = false)
    private BigDecimal feeTotal;

    @Column(nullable = false)
    private BigDecimal totalPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentState state;
}
