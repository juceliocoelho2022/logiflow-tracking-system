package br.com.logiflow.tracking.repository;

import br.com.logiflow.tracking.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, UUID> {

    boolean existsByEventId(UUID eventId);

    List<TrackingEvent> findByTrackingCodeOrderByOccurredAtAsc(String trackingCode);
}
