package br.com.logiflow.tracking.service;

import br.com.logiflow.tracking.dto.TrackingEventMessage;
import br.com.logiflow.tracking.entity.TrackingEvent;
import br.com.logiflow.tracking.repository.TrackingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackingCommandService {

    private static final Logger log = LoggerFactory.getLogger(TrackingCommandService.class);

    private final TrackingEventRepository repository;

    public TrackingCommandService(TrackingEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean register(TrackingEventMessage message) {
        if (repository.existsByEventId(message.eventId())) {
            log.info("Evento duplicado ignorado: {}", message.eventId());
            return false;
        }

        TrackingEvent entity = new TrackingEvent(
                message.eventId(),
                message.trackingCode(),
                message.orderId(),
                message.status(),
                message.city(),
                message.state(),
                message.description(),
                message.occurredAt()
        );

        try {
            repository.saveAndFlush(entity);
            log.info("Evento de rastreamento persistido: eventId={}, trackingCode={}, status={}",
                    message.eventId(), message.trackingCode(), message.status());
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.info("Evento já processado por outra execução: {}", message.eventId());
            return false;
        }
    }
}
