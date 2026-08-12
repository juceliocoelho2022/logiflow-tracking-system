package br.com.logiflow.tracking.service;

import br.com.logiflow.tracking.dto.TrackingResponse;
import br.com.logiflow.tracking.entity.TrackingEvent;
import br.com.logiflow.tracking.entity.TrackingStatus;
import br.com.logiflow.tracking.repository.TrackingEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingQueryServiceTest {

    @Mock
    private TrackingEventRepository repository;

    @InjectMocks
    private TrackingQueryService service;

    @Test
    void shouldReturnCurrentStatusAndHistory() {
        String code = "LF2026000145BR";
        TrackingEvent created = new TrackingEvent(
                UUID.randomUUID(), code, "PED-145", TrackingStatus.PEDIDO_CRIADO,
                "São Paulo", "SP", "Pedido criado", Instant.parse("2026-08-12T10:00:00Z"));
        TrackingEvent transit = new TrackingEvent(
                UUID.randomUUID(), code, "PED-145", TrackingStatus.EM_TRANSPORTE,
                "Guarulhos", "SP", "Em transferência", Instant.parse("2026-08-12T12:00:00Z"));

        when(repository.findByTrackingCodeOrderByOccurredAtAsc(code))
                .thenReturn(List.of(created, transit));

        TrackingResponse response = service.findByTrackingCode(code);

        assertEquals(TrackingStatus.EM_TRANSPORTE, response.currentStatus());
        assertEquals(2, response.history().size());
        assertEquals("Guarulhos", response.currentCity());
    }

    @Test
    void shouldReturn404WhenTrackingCodeDoesNotExist() {
        when(repository.findByTrackingCodeOrderByOccurredAtAsc("NAO-EXISTE"))
                .thenReturn(List.of());

        assertThrows(ResponseStatusException.class,
                () -> service.findByTrackingCode("NAO-EXISTE"));
    }
}
