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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "scenarios", uniqueConstraints = @UniqueConstraint(columnNames = {"hub_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false, length = 100)
    private String hubId;

    @Column(nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScenarioCondition> conditions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScenarioAction> actions = new LinkedHashSet<>();

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
