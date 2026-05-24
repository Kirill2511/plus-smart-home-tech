package ru.yandex.practicum.aggregator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("aggregator.kafka")
public class KafkaConfig {
    private String bootstrapServers;
    private Topics topics;
    private Consumer consumer;

    @Getter
    @Setter
    public static class Topics {
        private String sensors;
        private String snapshots;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String groupId;
        private long pollTimeout;
    }
}
