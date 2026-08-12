package br.com.logiflow.tracking.kafka;

import br.com.logiflow.tracking.dto.TrackingEventMessage;
import br.com.logiflow.tracking.service.TrackingCommandService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TrackingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TrackingEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final TrackingCommandService commandService;

    public TrackingEventConsumer(ObjectMapper objectMapper, TrackingCommandService commandService) {
        this.objectMapper = objectMapper;
        this.commandService = commandService;
    }

    @KafkaListener(topics = "${logiflow.kafka.tracking-topic}")
    public void consume(String payload) {
        try {
            TrackingEventMessage event = objectMapper.readValue(payload, TrackingEventMessage.class);
            commandService.register(event);
        } catch (JsonProcessingException ex) {
            log.error("Evento inválido recebido no Kafka: {}", payload, ex);
            throw new IllegalArgumentException("Evento Kafka inválido", ex);
        }
    }
}
