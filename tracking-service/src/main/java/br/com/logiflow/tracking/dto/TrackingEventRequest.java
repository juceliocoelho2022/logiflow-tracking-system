package br.com.logiflow.tracking.dto;

import br.com.logiflow.tracking.entity.TrackingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record TrackingEventRequest(
        @NotNull UUID eventId,
        @NotBlank @Size(max = 60) String trackingCode,
        @NotBlank @Size(max = 60) String orderId,
        @NotNull TrackingStatus status,
        @Size(max = 120) String city,
        @Size(max = 2) String state,
        @Size(max = 500) String description,
        @NotNull Instant occurredAt
) {
}
