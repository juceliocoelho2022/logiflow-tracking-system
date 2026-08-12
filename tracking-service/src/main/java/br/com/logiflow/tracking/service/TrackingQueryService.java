package br.com.logiflow.tracking.service;

import br.com.logiflow.tracking.dto.TrackingResponse;
import br.com.logiflow.tracking.dto.TrackingTimelineItem;
import br.com.logiflow.tracking.entity.TrackingEvent;
import br.com.logiflow.tracking.repository.TrackingEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrackingQueryService {

    private final TrackingEventRepository repository;

    public TrackingQueryService(TrackingEventRepository repository) {
        this.repository = repository;
    }

    public TrackingResponse findByTrackingCode(String trackingCode) {
        List<TrackingEvent> events = repository.findByTrackingCodeOrderByOccurredAtAsc(trackingCode);

        if (events.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Código de rastreamento não encontrado: " + trackingCode);
        }

        TrackingEvent current = events.getLast();
        List<TrackingTimelineItem> timeline = events.stream()
                .map(event -> new TrackingTimelineItem(
                        event.getEventId(),
                        event.getStatus(),
                        event.getCity(),
                        event.getState(),
                        event.getDescription(),
                        event.getOccurredAt()
                ))
                .toList();

        return new TrackingResponse(
                current.getTrackingCode(),
                current.getOrderId(),
                current.getStatus(),
                current.getCity(),
                current.getState(),
                current.getOccurredAt(),
                timeline
        );
    }
}
