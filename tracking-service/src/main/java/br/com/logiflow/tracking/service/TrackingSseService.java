package br.com.logiflow.tracking.service;

import br.com.logiflow.tracking.dto.TrackingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TrackingSseService {

    private static final Logger log = LoggerFactory.getLogger(TrackingSseService.class);
    private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String trackingCode) {
        String normalizedCode = normalize(trackingCode);
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);

        emitters.computeIfAbsent(normalizedCode, ignored -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(normalizedCode, emitter));
        emitter.onTimeout(() -> removeEmitter(normalizedCode, emitter));
        emitter.onError(error -> removeEmitter(normalizedCode, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "trackingCode", normalizedCode,
                            "message", "Conexão em tempo real estabelecida"
                    )));
        } catch (IOException | IllegalStateException ex) {
            removeEmitter(normalizedCode, emitter);
            emitter.completeWithError(ex);
        }

        log.debug("Cliente SSE inscrito: trackingCode={}, total={}",
                normalizedCode, subscriberCount(normalizedCode));

        return emitter;
    }

    public void publish(String trackingCode, TrackingResponse response) {
        String normalizedCode = normalize(trackingCode);
        CopyOnWriteArrayList<SseEmitter> subscribers = emitters.get(normalizedCode);

        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : subscribers) {
            try {
                emitter.send(SseEmitter.event()
                        .name("tracking-update")
                        .id(response.lastUpdate().toString())
                        .data(response));
            } catch (IOException | IllegalStateException ex) {
                removeEmitter(normalizedCode, emitter);
            }
        }

        log.debug("Atualização SSE publicada: trackingCode={}, subscribers={}",
                normalizedCode, subscriberCount(normalizedCode));
    }

    private void removeEmitter(String trackingCode, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> subscribers = emitters.get(trackingCode);

        if (subscribers == null) {
            return;
        }

        subscribers.remove(emitter);

        if (subscribers.isEmpty()) {
            emitters.remove(trackingCode, subscribers);
        }
    }

    private int subscriberCount(String trackingCode) {
        CopyOnWriteArrayList<SseEmitter> subscribers = emitters.get(trackingCode);
        return subscribers == null ? 0 : subscribers.size();
    }

    private String normalize(String trackingCode) {
        return trackingCode.trim().toUpperCase();
    }
}
