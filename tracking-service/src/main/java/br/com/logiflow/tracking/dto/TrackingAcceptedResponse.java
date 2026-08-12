package br.com.logiflow.tracking.dto;

import java.util.UUID;

public record TrackingAcceptedResponse(
        UUID eventId,
        String trackingCode,
        String status,
        String message
) {
}
