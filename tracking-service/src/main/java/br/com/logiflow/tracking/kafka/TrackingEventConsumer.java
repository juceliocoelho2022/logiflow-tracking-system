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
import org.slf4j.MDC;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class TrackingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TrackingEventConsumer.class);
    private static final int MAX_LOG_PAYLOAD_LENGTH = 500;

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

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000),
            retryTopicSuffix = ".retry",
            dltTopicSuffix = ".DLT",
            autoCreateTopics = "true",
            numPartitions = "3",
            replicationFactor = "1"
    )
    @KafkaListener(topics = "${logiflow.kafka.tracking-topic}")
    public void consume(String payload) {
        try {
            TrackingEventMessage event = objectMapper.readValue(payload, TrackingEventMessage.class);

            try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", event.correlationId())) {
                log.info(
                        "tracking_event_received correlationId={} eventId={} trackingCode={} status={}",
                        event.correlationId(),
                        event.eventId(),
                        event.trackingCode(),
                        event.status()
                );

                boolean persisted = commandService.register(event);

                if (persisted) {
                    TrackingResponse response = queryService.findByTrackingCode(event.trackingCode());
                    sseService.publish(event.trackingCode(), response);
                    log.info(
                            "tracking_event_processed correlationId={} eventId={} trackingCode={} status={}",
                            event.correlationId(),
                            event.eventId(),
                            event.trackingCode(),
                            event.status()
                    );
                } else {
                    log.info(
                            "tracking_event_ignored_duplicate correlationId={} eventId={} trackingCode={}",
                            event.correlationId(),
                            event.eventId(),
                            event.trackingCode()
                    );
                }
            }
        } catch (JsonProcessingException ex) {
            log.error("tracking_event_invalid payload={}", safePayload(payload), ex);
            throw new IllegalArgumentException("Evento Kafka inválido", ex);
        }
    }

    @DltHandler
    public void processDltMessage(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic
    ) {
        log.error(
                "tracking_event_dead_letter topic={} payload={}",
                receivedTopic,
                safePayload(payload)
        );
    }

    private String safePayload(String payload) {
        if (payload == null) {
            return "null";
        }
        if (payload.length() <= MAX_LOG_PAYLOAD_LENGTH) {
            return payload;
        }
        return payload.substring(0, MAX_LOG_PAYLOAD_LENGTH) + "...";
    }
}
