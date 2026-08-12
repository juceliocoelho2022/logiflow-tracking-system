package br.com.logiflow.tracking.kafka;

import br.com.logiflow.tracking.dto.TrackingEventMessage;
import br.com.logiflow.tracking.dto.TrackingResponse;
import br.com.logiflow.tracking.service.TrackingCommandService;
import br.com.logiflow.tracking.service.TrackingQueryService;
import br.com.logiflow.tracking.service.TrackingSseService;
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
    private final TrackingQueryService queryService;
    private final TrackingSseService sseService;

    public TrackingEventConsumer(
            ObjectMapper objectMapper,
            TrackingCommandService commandService,
            TrackingQueryService queryService,
            TrackingSseService sseService
    ) {
        this.objectMapper = objectMapper;
        this.commandService = commandService;
        this.queryService = queryService;
        this.sseService = sseService;
    }

    @KafkaListener(topics = "${logiflow.kafka.tracking-topic}")
    public void consume(String payload) {
        try {
            TrackingEventMessage event = objectMapper.readValue(payload, TrackingEventMessage.class);
            boolean persisted = commandService.register(event);

            if (persisted) {
                TrackingResponse response = queryService.findByTrackingCode(event.trackingCode());
                sseService.publish(event.trackingCode(), response);
            }
        } catch (JsonProcessingException ex) {
            log.error("Evento inválido recebido no Kafka: {}", payload, ex);
            throw new IllegalArgumentException("Evento Kafka inválido", ex);
        }
    }
}
