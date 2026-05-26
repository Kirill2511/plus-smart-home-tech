package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scenario_conditions")
@Getter
@Setter
@NoArgsConstructor
public class ScenarioCondition {
    @EmbeddedId
    private ScenarioConditionId id = new ScenarioConditionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapsId("conditionId")
    @JoinColumn(name = "condition_id")
    private Condition condition;

    public ScenarioCondition(Sensor sensor, Condition condition) {
        this.sensor = sensor;
        this.condition = condition;
    }

    public String getSensorId() {
        return sensor.getId();
    }
}
