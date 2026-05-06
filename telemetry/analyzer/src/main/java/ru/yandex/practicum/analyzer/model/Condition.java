package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@NoArgsConstructor
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ConditionOperationAvro operation;

    @Column(name = "value")
    private Integer value;

    public Condition(ConditionTypeAvro type, ConditionOperationAvro operation, Integer value) {
        this.type = type;
        this.operation = operation;
        this.value = value;
    }
}
