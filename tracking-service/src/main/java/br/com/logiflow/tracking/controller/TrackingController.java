package br.com.logiflow.tracking.controller;

import br.com.logiflow.tracking.dto.*;
import br.com.logiflow.tracking.kafka.TrackingEventPublisher;
import br.com.logiflow.tracking.service.TrackingQueryService;
import br.com.logiflow.tracking.service.TrackingSseService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    private final TrackingEventPublisher publisher;
    private final TrackingQueryService queryService;
    private final TrackingSseService sseService;

    public TrackingController(
            TrackingEventPublisher publisher,
            TrackingQueryService queryService,
            TrackingSseService sseService
    ) {
        this.publisher = publisher;
        this.queryService = queryService;
        this.sseService = sseService;
    }

    @PostMapping("/events")
    public ResponseEntity<TrackingAcceptedResponse> publishEvent(
            @Valid @RequestBody TrackingEventRequest request,
            @RequestHeader(value = CORRELATION_ID_HEADER, required = false) String correlationId
    ) {
        String resolvedCorrelationId = resolveCorrelationId(correlationId);
        TrackingEventMessage message = TrackingEventMessage.from(request, resolvedCorrelationId);
        publisher.publish(message);

        return ResponseEntity.accepted()
                .header(CORRELATION_ID_HEADER, resolvedCorrelationId)
                .body(new TrackingAcceptedResponse(
                        request.eventId(),
                        resolvedCorrelationId,
                        request.trackingCode(),
                        "ACCEPTED",
                        "Evento enviado para processamento assíncrono"
                ));
    }

    @GetMapping("/{trackingCode}")
    public TrackingResponse track(@PathVariable String trackingCode) {
        return queryService.findByTrackingCode(trackingCode);
    }

    @GetMapping(path = "/{trackingCode}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String trackingCode) {
        return sseService.subscribe(trackingCode);
    }

    private String resolveCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId.trim();
    }
}
