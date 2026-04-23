package ru.yandex.practicum.collector.model.hub;

import ru.yandex.practicum.collector.model.enumeration.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAction {

    private String sensorId;
    private ActionType type;
    private Integer value;
}
