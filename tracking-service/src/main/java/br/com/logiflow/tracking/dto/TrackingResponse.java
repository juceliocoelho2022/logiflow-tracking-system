package br.com.logiflow.tracking.dto;

import br.com.logiflow.tracking.entity.TrackingStatus;

import java.time.Instant;
import java.util.List;

public record TrackingResponse(
        String trackingCode,
        String orderId,
        TrackingStatus currentStatus,
        String currentCity,
        String currentState,
        Instant lastUpdate,
        List<TrackingTimelineItem> history
) {
}
