package br.com.logiflow.tracking.kafka;

import br.com.logiflow.tracking.dto.TrackingEventMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class TrackingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TrackingEventPublisher.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public TrackingEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${logiflow.kafka.tracking-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(TrackingEventMessage event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.trackingCode(), payload);
            record.headers().add(
                    CORRELATION_ID_HEADER,
                    event.correlationId().getBytes(StandardCharsets.UTF_8)
            );

            kafkaTemplate.send(record);

            log.info(
                    "tracking_event_published correlationId={} eventId={} trackingCode={} status={} topic={}",
                    event.correlationId(),
                    event.eventId(),
                    event.trackingCode(),
                    event.status(),
                    topic
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Falha ao serializar evento de rastreamento", ex);
        }
    }
}
