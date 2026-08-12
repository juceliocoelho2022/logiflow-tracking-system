package br.com.logiflow.tracking.dto;

import br.com.logiflow.tracking.entity.TrackingStatus;

import java.time.Instant;
import java.util.UUID;

public record TrackingEventMessage(
        UUID eventId,
        String trackingCode,
        String orderId,
        TrackingStatus status,
        String city,
        String state,
        String description,
        Instant occurredAt
) {
    public static TrackingEventMessage from(TrackingEventRequest request) {
        return new TrackingEventMessage(
                request.eventId(),
                request.trackingCode(),
                request.orderId(),
                request.status(),
                request.city(),
                request.state(),
                request.description(),
                request.occurredAt()
        );
    }
}
