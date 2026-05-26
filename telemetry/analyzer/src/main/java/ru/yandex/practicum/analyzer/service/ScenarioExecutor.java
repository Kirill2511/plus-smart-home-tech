package ru.yandex.practicum.analyzer.service;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.analyzer.model.Action;
import ru.yandex.practicum.analyzer.model.Condition;
import ru.yandex.practicum.analyzer.model.Scenario;
import ru.yandex.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.analyzer.model.ScenarioCondition;
import ru.yandex.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ScenarioExecutor {
    private final ScenarioRepository scenarioRepository;
    private final String hubRouterAddress;
    private ManagedChannel hubRouterChannel;
    private HubRouterControllerBlockingStub hubRouterClient;

    public ScenarioExecutor(ScenarioRepository scenarioRepository,
                            @Value("${grpc.client.hub-router.address}") String hubRouterAddress) {
        this.scenarioRepository = scenarioRepository;
        this.hubRouterAddress = hubRouterAddress;
    }

    @Transactional(readOnly = true)
    public void executeMatchingScenarios(SensorsSnapshotAvro snapshot) {
        List<Scenario> scenarios = findScenarios(snapshot.getHubId());

        scenarios.stream()
                .filter(scenario -> scenario.getConditions().stream()
                        .allMatch(condition -> matches(condition, snapshot)))
                .forEach(scenario -> executeActions(snapshot, scenario));
    }

    private List<Scenario> findScenarios(String hubId) {
        sleepBeforeRetry();
        List<Scenario> scenarios = scenarioRepository.findByHubIdWithDetails(hubId);
        for (int attempt = 0; scenarios.isEmpty() && attempt < 5; attempt++) {
            sleepBeforeRetry();
            scenarios = scenarioRepository.findByHubIdWithDetails(hubId);
        }
        return scenarios;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean matches(ScenarioCondition scenarioCondition, SensorsSnapshotAvro snapshot) {
        Condition condition = scenarioCondition.getCondition();
        SensorStateAvro state = snapshot.getSensorsState().get(scenarioCondition.getSensorId());
        if (state == null) {
            return false;
        }

        Optional<Object> actualValue = extractValue(condition.getType(), state.getData());
        return actualValue
                .map(value -> compare(value, condition))
                .orElse(false);
    }

    private Optional<Object> extractValue(ConditionTypeAvro type, Object data) {
        return switch (type) {
            case MOTION -> data instanceof MotionSensorAvro sensor
                    ? Optional.of(sensor.getMotion()) : Optional.empty();
            case LUMINOSITY -> data instanceof LightSensorAvro sensor
                    ? Optional.of(sensor.getLuminosity()) : Optional.empty();
            case SWITCH -> data instanceof SwitchSensorAvro sensor
                    ? Optional.of(sensor.getState()) : Optional.empty();
            case TEMPERATURE -> {
                if (data instanceof TemperatureSensorAvro sensor) {
                    yield Optional.of(sensor.getTemperatureC());
                }
                yield data instanceof ClimateSensorAvro sensor
                        ? Optional.of(sensor.getTemperatureC()) : Optional.empty();
            }
            case CO2LEVEL -> data instanceof ClimateSensorAvro sensor
                    ? Optional.of(sensor.getCo2Level()) : Optional.empty();
            case HUMIDITY -> data instanceof ClimateSensorAvro sensor
                    ? Optional.of(sensor.getHumidity()) : Optional.empty();
        };
    }

    private boolean compare(Object actualValue, Condition condition) {
        if (actualValue instanceof Boolean actual && condition.getValue() != null) {
            return condition.getOperation() == ConditionOperationAvro.EQUALS
                    && Boolean.compare(actual, condition.getValue() == 1) == 0;
        }

        if (actualValue instanceof Integer actual && condition.getValue() != null) {
            return switch (condition.getOperation()) {
                case EQUALS -> actual.equals(condition.getValue());
                case GREATER_THAN -> actual > condition.getValue();
                case LOWER_THAN -> actual < condition.getValue();
            };
        }

        return false;
    }

    private void executeActions(SensorsSnapshotAvro snapshot, Scenario scenario) {
        scenario.getActions().forEach(scenarioAction -> {
            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(snapshot.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(toProto(scenarioAction))
                    .setTimestamp(toTimestamp(Instant.now()))
                    .build();

            if (sendWithRetry(request)) {
                log.info("Отправлена команда {} для датчика {} по сценарию {}",
                        scenarioAction.getAction().getType(), scenarioAction.getSensorId(), scenario.getName());
            }
        });
    }

    private boolean sendWithRetry(DeviceActionRequest request) {
        StatusRuntimeException lastException = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            try {
                hubRouterClient().withDeadlineAfter(500, TimeUnit.MILLISECONDS)
                        .handleDeviceAction(request);
                return true;
            } catch (StatusRuntimeException e) {
                lastException = e;
                sleepBeforeRetry();
            }
        }

        log.warn("Не удалось отправить команду для датчика {} по сценарию {}: {}",
                request.getAction().getSensorId(), request.getScenarioName(),
                lastException != null ? lastException.getStatus() : "unknown");
        return false;
    }

    private synchronized HubRouterControllerBlockingStub hubRouterClient() {
        if (hubRouterClient == null) {
            HostPort hostPort = parseHubRouterAddress();
            hubRouterChannel = ManagedChannelBuilder.forAddress(hostPort.host(), hostPort.port())
                    .usePlaintext()
                    .build();
            hubRouterClient = HubRouterControllerGrpc.newBlockingStub(hubRouterChannel);
        }

        return hubRouterClient;
    }

    private HostPort parseHubRouterAddress() {
        String address = hubRouterAddress.replace("static://", "");
        int separator = address.lastIndexOf(':');
        if (separator < 0) {
            throw new IllegalStateException("Некорректный адрес Hub Router: " + hubRouterAddress);
        }
        return new HostPort(address.substring(0, separator), Integer.parseInt(address.substring(separator + 1)));
    }

    @PreDestroy
    public void shutdown() {
        if (hubRouterChannel != null) {
            hubRouterChannel.shutdownNow();
        }
    }

    private DeviceActionProto toProto(ScenarioAction scenarioAction) {
        Action action = scenarioAction.getAction();
        DeviceActionProto.Builder builder = DeviceActionProto.newBuilder()
                .setSensorId(scenarioAction.getSensorId())
                .setType(ActionTypeProto.valueOf(action.getType().name()));

        if (action.getValue() != null) {
            builder.setValue(action.getValue());
        }

        return builder.build();
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private record HostPort(String host, int port) {
    }
}
