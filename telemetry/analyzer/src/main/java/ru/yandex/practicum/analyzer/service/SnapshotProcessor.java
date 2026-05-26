package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.analyzer.kafka.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private final ScenarioExecutor scenarioExecutor;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    @Value("${kafka.consumers.snapshots.group-id}")
    private String groupId;

    @Value("${kafka.consumers.snapshots.poll-timeout}")
    private long pollTimeout;

    public void start() {
        KafkaConsumer<String, SensorsSnapshotAvro> consumer = new KafkaConsumer<>(consumerProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(snapshotsTopic));
            log.info("Analyzer subscribed to snapshots topic {}", snapshotsTopic);

            while (true) {
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : consumer.poll(Duration.ofMillis(pollTimeout))) {
                    scenarioExecutor.executeMatchingScenarios(record.value());
                    consumer.commitSync();
                }
            }
        } catch (WakeupException ignored) {
            log.info("Snapshot processor received shutdown signal");
        } catch (Exception e) {
            log.error("Ошибка во время обработки снапшотов", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер снапшотов");
                consumer.close();
            }
        }
    }

    private Properties consumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }
}
