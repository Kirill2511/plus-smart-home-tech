package ru.yandex.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.analyzer.model.Scenario;

import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    @EntityGraph(attributePaths = {
            "conditions",
            "conditions.condition",
            "conditions.sensor",
            "actions",
            "actions.action",
            "actions.sensor"
    })
    @Query("select distinct s from Scenario s where s.hubId = :hubId")
    List<Scenario> findByHubIdWithDetails(@Param("hubId") String hubId);

    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    void deleteByHubIdAndName(String hubId, String name);
}
