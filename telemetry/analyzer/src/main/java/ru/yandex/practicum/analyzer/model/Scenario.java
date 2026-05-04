package ru.yandex.practicum.analyzer.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "scenarios", uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScenarioCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScenarioAction> actions = new ArrayList<>();

    public Scenario(String hubId, String name) {
        this.hubId = hubId;
        this.name = name;
    }

    public void replaceConditions(List<ScenarioCondition> newConditions) {
        conditions.clear();
        newConditions.forEach(scenarioCondition -> {
            scenarioCondition.setScenario(this);
            conditions.add(scenarioCondition);
        });
    }

    public void replaceActions(List<ScenarioAction> newActions) {
        actions.clear();
        newActions.forEach(scenarioAction -> {
            scenarioAction.setScenario(this);
            actions.add(scenarioAction);
        });
    }
}
