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
import ru.yandex.practicum.analyzer.kafka.HubEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final HubEventService hubEventService;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.hubs}")
    private String hubsTopic;

    @Value("${kafka.consumers.hubs.group-id}")
    private String groupId;

    @Value("${kafka.consumers.hubs.poll-timeout}")
    private long pollTimeout;

    @Override
    public void run() {
        KafkaConsumer<String, HubEventAvro> consumer = new KafkaConsumer<>(consumerProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(hubsTopic));
            log.info("Analyzer subscribed to hub events topic {}", hubsTopic);

            while (true) {
                for (ConsumerRecord<String, HubEventAvro> record : consumer.poll(Duration.ofMillis(pollTimeout))) {
                    hubEventService.handle(record.value());
                }

                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Hub event processor received shutdown signal");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий хаба", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер событий хаба");
                consumer.close();
            }
        }
    }

    private Properties consumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getName());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }
}
