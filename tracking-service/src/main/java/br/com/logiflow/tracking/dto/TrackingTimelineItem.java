package br.com.logiflow.tracking.dto;

import br.com.logiflow.tracking.entity.TrackingStatus;

import java.time.Instant;
import java.util.UUID;

public record TrackingTimelineItem(
        UUID eventId,
        TrackingStatus status,
        String city,
        String state,
        String description,
        Instant occurredAt
) {
}
