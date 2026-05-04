package ru.yandex.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.aggregator.kafka.AvroSerializer;
import ru.yandex.practicum.aggregator.kafka.SensorEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final SnapshotService snapshotService;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.consumer.poll-timeout}")
    private long pollTimeout;

    public void start() {
        KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(consumerProperties());
        KafkaProducer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(producerProperties());

        try {
            consumer.subscribe(List.of(sensorsTopic));
            log.info("Aggregator subscribed to topic {}", sensorsTopic);

            while (true) {
                for (ConsumerRecord<String, SensorEventAvro> record : consumer.poll(Duration.ofMillis(pollTimeout))) {
                    SensorEventAvro event = record.value();
                    Optional<SensorsSnapshotAvro> snapshot = snapshotService.updateState(event);
                    snapshot.ifPresent(value -> producer.send(
                            new ProducerRecord<>(snapshotsTopic, value.getHubId(), value)
                    ));
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Aggregator received shutdown signal");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private Properties consumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    private Properties producerProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        return properties;
    }
}
