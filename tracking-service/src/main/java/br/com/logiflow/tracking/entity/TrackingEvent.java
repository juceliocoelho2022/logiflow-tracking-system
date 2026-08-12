package br.com.logiflow.tracking.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tracking_event",
        indexes = {
                @Index(name = "idx_tracking_event_tracking_code", columnList = "tracking_code"),
                @Index(name = "idx_tracking_event_order_id", columnList = "order_id"),
                @Index(name = "idx_tracking_event_occurred_at", columnList = "occurred_at")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_tracking_event_event_id", columnNames = "event_id")
)
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "tracking_code", nullable = false, length = 60)
    private String trackingCode;

    @Column(name = "order_id", nullable = false, length = 60)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TrackingStatus status;

    @Column(length = 120)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(length = 500)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    protected TrackingEvent() {
    }

    public TrackingEvent(UUID eventId,
                         String trackingCode,
                         String orderId,
                         TrackingStatus status,
                         String city,
                         String state,
                         String description,
                         Instant occurredAt) {
        this.eventId = eventId;
        this.trackingCode = trackingCode;
        this.orderId = orderId;
        this.status = status;
        this.city = city;
        this.state = state;
        this.description = description;
        this.occurredAt = occurredAt;
        this.receivedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getTrackingCode() { return trackingCode; }
    public String getOrderId() { return orderId; }
    public TrackingStatus getStatus() { return status; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getDescription() { return description; }
    public Instant getOccurredAt() { return occurredAt; }
    public Instant getReceivedAt() { return receivedAt; }
}
