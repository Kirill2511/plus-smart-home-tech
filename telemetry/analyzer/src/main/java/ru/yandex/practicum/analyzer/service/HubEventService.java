package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.model.Sensor;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.analyzer.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;

    @Transactional
    public void handle(HubEventAvro event) {
        Object payload = event.getPayload();
        if (payload instanceof DeviceAddedEventAvro deviceAdded) {
            sensorRepository.save(new Sensor(deviceAdded.getId(), event.getHubId()));
            log.info("Зарегистрирован датчик {} для хаба {}", deviceAdded.getId(), event.getHubId());
        } else if (payload instanceof DeviceRemovedEventAvro deviceRemoved) {
            sensorRepository.findByIdAndHubId(deviceRemoved.getId(), event.getHubId())
                    .ifPresent(sensorRepository::delete);
            log.info("Удалён датчик {} из хаба {}", deviceRemoved.getId(), event.getHubId());
        } else if (payload instanceof ScenarioAddedEventAvro scenarioAdded) {
            saveScenario(event.getHubId(), scenarioAdded);
        } else if (payload instanceof ScenarioRemovedEventAvro scenarioRemoved) {
            scenarioRepository.deleteByHubIdAndName(event.getHubId(), scenarioRemoved.getName());
            log.info("Удалён сценарий {} для хаба {}", scenarioRemoved.getName(), event.getHubId());
        }
    }

    private void saveScenario(String hubId, ScenarioAddedEventAvro event) {
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .orElseGet(() -> new Scenario(hubId, event.getName()));

        scenario.replaceConditions(toConditions(hubId, event.getConditions()));
        scenario.replaceActions(toActions(hubId, event.getActions()));

        scenarioRepository.save(scenario);
        log.info("Сохранён сценарий {} для хаба {}", event.getName(), hubId);
    }

    private List<ScenarioCondition> toConditions(String hubId, List<ScenarioConditionAvro> conditions) {
        return conditions.stream()
                .map(condition -> {
                    Object value = condition.getValue();
                    Integer conditionValue = value instanceof Boolean bool ? (bool ? 1 : 0) : (Integer) value;
                    Sensor sensor = sensorRepository.findByIdAndHubId(condition.getSensorId(), hubId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Не найден датчик " + condition.getSensorId() + " для хаба " + hubId));
                    return new ScenarioCondition(sensor,
                            new Condition(condition.getType(), condition.getOperation(), conditionValue));
                })
                .toList();
    }

    private List<ScenarioAction> toActions(String hubId, List<DeviceActionAvro> actions) {
        return actions.stream()
                .map(action -> {
                    Sensor sensor = sensorRepository.findByIdAndHubId(action.getSensorId(), hubId)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Не найден датчик " + action.getSensorId() + " для хаба " + hubId));
                    return new ScenarioAction(sensor, new Action(action.getType(), action.getValue()));
                })
                .toList();
    }
}
