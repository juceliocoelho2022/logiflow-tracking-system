package br.com.logiflow.tracking.controller;

import br.com.logiflow.tracking.dto.TrackingAcceptedResponse;
import br.com.logiflow.tracking.dto.TrackingEventMessage;
import br.com.logiflow.tracking.dto.TrackingEventRequest;
import br.com.logiflow.tracking.entity.TrackingStatus;
import br.com.logiflow.tracking.kafka.TrackingEventPublisher;
import br.com.logiflow.tracking.service.TrackingQueryService;
import br.com.logiflow.tracking.service.TrackingSseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrackingControllerTest {

    @Mock
    private TrackingEventPublisher publisher;

    @Mock
    private TrackingQueryService queryService;

    @Mock
    private TrackingSseService sseService;

    @InjectMocks
    private TrackingController controller;

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() {
        TrackingEventRequest request = request();

        ResponseEntity<TrackingAcceptedResponse> response = controller.publishEvent(request, null);

        assertEquals(202, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().correlationId());
        assertFalse(response.getBody().correlationId().isBlank());
        assertEquals(response.getBody().correlationId(), response.getHeaders().getFirst("X-Correlation-Id"));

        ArgumentCaptor<TrackingEventMessage> captor = ArgumentCaptor.forClass(TrackingEventMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals(response.getBody().correlationId(), captor.getValue().correlationId());
    }

    @Test
    void shouldPropagateProvidedCorrelationId() {
        TrackingEventRequest request = request();
        String correlationId = "checkout-2026-000145";

        ResponseEntity<TrackingAcceptedResponse> response = controller.publishEvent(request, correlationId);

        assertNotNull(response.getBody());
        assertEquals(correlationId, response.getBody().correlationId());
        assertEquals(correlationId, response.getHeaders().getFirst("X-Correlation-Id"));

        ArgumentCaptor<TrackingEventMessage> captor = ArgumentCaptor.forClass(TrackingEventMessage.class);
        verify(publisher).publish(captor.capture());
        assertEquals(correlationId, captor.getValue().correlationId());
    }

    private TrackingEventRequest request() {
        return new TrackingEventRequest(
                UUID.randomUUID(),
                "LF2026000145BR",
                "PED-2026-000145",
                TrackingStatus.EM_TRANSPORTE,
                "Guarulhos",
                "SP",
                "Pedido em transporte",
                Instant.parse("2026-08-12T10:00:00Z")
        );
    }
}
