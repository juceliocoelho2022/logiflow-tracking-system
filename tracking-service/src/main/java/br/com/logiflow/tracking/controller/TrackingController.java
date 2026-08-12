package br.com.logiflow.tracking.controller;

import br.com.logiflow.tracking.dto.*;
import br.com.logiflow.tracking.kafka.TrackingEventPublisher;
import br.com.logiflow.tracking.service.TrackingQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingEventPublisher publisher;
    private final TrackingQueryService queryService;

    public TrackingController(TrackingEventPublisher publisher, TrackingQueryService queryService) {
        this.publisher = publisher;
        this.queryService = queryService;
    }

    @PostMapping("/events")
    public ResponseEntity<TrackingAcceptedResponse> publishEvent(@Valid @RequestBody TrackingEventRequest request) {
        TrackingEventMessage message = TrackingEventMessage.from(request);
        publisher.publish(message);

        return ResponseEntity.accepted().body(new TrackingAcceptedResponse(
                request.eventId(),
                request.trackingCode(),
                "ACCEPTED",
                "Evento enviado para processamento assíncrono"
        ));
    }

    @GetMapping("/{trackingCode}")
    public TrackingResponse track(@PathVariable String trackingCode) {
        return queryService.findByTrackingCode(trackingCode);
    }
}
