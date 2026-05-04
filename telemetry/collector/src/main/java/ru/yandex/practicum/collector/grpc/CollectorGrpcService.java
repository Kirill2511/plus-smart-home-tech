package ru.yandex.practicum.collector.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.collector.model.enumeration.ActionType;
import ru.yandex.practicum.collector.model.enumeration.ConditionOperation;
import ru.yandex.practicum.collector.model.enumeration.ConditionType;
import ru.yandex.practicum.collector.model.enumeration.DeviceType;
import ru.yandex.practicum.collector.model.hub.DeviceAction;
import ru.yandex.practicum.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.collector.model.hub.DeviceRemovedEvent;
import ru.yandex.practicum.collector.model.hub.HubEvent;
import ru.yandex.practicum.collector.model.hub.ScenarioAddedEvent;
import ru.yandex.practicum.collector.model.hub.ScenarioCondition;
import ru.yandex.practicum.collector.model.hub.ScenarioRemovedEvent;
import ru.yandex.practicum.collector.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.collector.model.sensor.LightSensorEvent;
import ru.yandex.practicum.collector.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.collector.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.collector.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.collector.service.EventService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final EventService eventService;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEvent event = toSensorEvent(request);
            log.info("Получено gRPC-событие датчика: type={}, id={}, hubId={}", event.getType(), event.getId(),
                    event.getHubId());
            eventService.collectSensorEvent(event);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toInternalError(e));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            HubEvent event = toHubEvent(request);
            log.info("Получено gRPC-событие хаба: type={}, hubId={}", event.getType(), event.getHubId());
            eventService.collectHubEvent(event);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(toInternalError(e));
        }
    }

    private SensorEvent toSensorEvent(SensorEventProto proto) {
        SensorEvent event = switch (proto.getPayloadCase()) {
            case MOTION_SENSOR -> {
                MotionSensorEvent e = new MotionSensorEvent();
                e.setLinkQuality(proto.getMotionSensor().getLinkQuality());
                e.setMotion(proto.getMotionSensor().getMotion());
                e.setVoltage(proto.getMotionSensor().getVoltage());
                yield e;
            }
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorEvent e = new TemperatureSensorEvent();
                e.setTemperatureC(proto.getTemperatureSensor().getTemperatureC());
                e.setTemperatureF(proto.getTemperatureSensor().getTemperatureF());
                yield e;
            }
            case LIGHT_SENSOR -> {
                LightSensorEvent e = new LightSensorEvent();
                e.setLinkQuality(proto.getLightSensor().getLinkQuality());
                e.setLuminosity(proto.getLightSensor().getLuminosity());
                yield e;
            }
            case CLIMATE_SENSOR -> {
                ClimateSensorEvent e = new ClimateSensorEvent();
                e.setTemperatureC(proto.getClimateSensor().getTemperatureC());
                e.setHumidity(proto.getClimateSensor().getHumidity());
                e.setCo2Level(proto.getClimateSensor().getCo2Level());
                yield e;
            }
            case SWITCH_SENSOR -> {
                SwitchSensorEvent e = new SwitchSensorEvent();
                e.setState(proto.getSwitchSensor().getState());
                yield e;
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor event payload is not set");
        };

        event.setId(proto.getId());
        event.setHubId(proto.getHubId());
        event.setTimestamp(toInstant(proto.getTimestamp()));
        return event;
    }

    private HubEvent toHubEvent(HubEventProto proto) {
        HubEvent event = switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent e = new DeviceAddedEvent();
                e.setId(proto.getDeviceAdded().getId());
                e.setDeviceType(DeviceType.valueOf(proto.getDeviceAdded().getType().name()));
                yield e;
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent e = new DeviceRemovedEvent();
                e.setId(proto.getDeviceRemoved().getId());
                yield e;
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent e = new ScenarioAddedEvent();
                e.setName(proto.getScenarioAdded().getName());
                e.setConditions(proto.getScenarioAdded().getConditionList().stream()
                        .map(this::toScenarioCondition)
                        .toList());
                e.setActions(proto.getScenarioAdded().getActionList().stream()
                        .map(this::toDeviceAction)
                        .toList());
                yield e;
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent e = new ScenarioRemovedEvent();
                e.setName(proto.getScenarioRemoved().getName());
                yield e;
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub event payload is not set");
        };

        event.setHubId(proto.getHubId());
        event.setTimestamp(toInstant(proto.getTimestamp()));
        return event;
    }

    private ScenarioCondition toScenarioCondition(ScenarioConditionProto proto) {
        ScenarioCondition condition = new ScenarioCondition();
        condition.setSensorId(proto.getSensorId());
        condition.setType(ConditionType.valueOf(proto.getType().name()));
        condition.setOperation(ConditionOperation.valueOf(proto.getOperation().name()));
        condition.setValue(getConditionValue(proto));
        return condition;
    }

    private DeviceAction toDeviceAction(DeviceActionProto proto) {
        DeviceAction action = new DeviceAction();
        action.setSensorId(proto.getSensorId());
        action.setType(ActionType.valueOf(proto.getType().name()));
        if (proto.hasValue()) {
            action.setValue(proto.getValue());
        }
        return action;
    }

    private Object getConditionValue(ScenarioConditionProto proto) {
        return switch (proto.getValueCase()) {
            case BOOL_VALUE -> proto.getBoolValue();
            case INT_VALUE -> proto.getIntValue();
            case VALUE_NOT_SET -> null;
        };
    }

    private Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private StatusRuntimeException toInternalError(Exception e) {
        return new StatusRuntimeException(Status.INTERNAL
                .withDescription(e.getLocalizedMessage())
                .withCause(e));
    }
}
