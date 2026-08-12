# LogiFlow Tracking v1.1.0 — Realtime Dashboard with SSE

## Destaques

- Dashboard web em React + Vite;
- busca por código de rastreio;
- status atual, localização e timeline visual;
- atualização automática via Server-Sent Events (SSE);
- `EventSource` no frontend com reconexão automática;
- publicação de `tracking-update` após persistência do evento Kafka;
- idempotência preservada: eventos duplicados não disparam nova atualização;
- Backend CI com Java 21 + Maven;
- Frontend CI com Node.js 22 + Vite;
- screenshot oficial do fluxo até `ENTREGUE`.

## Fluxo validado

```text
POST /api/tracking/events
        ↓
Spring Boot
        ↓
Apache Kafka
        ↓
Tracking Consumer
        ↓
PostgreSQL
        ↓
TrackingSseService
        ↓
EventSource / React
        ↓
Timeline atualizada automaticamente
```

## Cenário demonstrado

```text
PEDIDO_CRIADO
      ↓
EM_TRANSPORTE
      ↓
SAIU_PARA_ENTREGA
      ↓
ENTREGUE
```

O dashboard recebeu o último estado sem nova busca manual e exibiu quatro eventos na timeline.

## Próximas etapas

- `correlationId`;
- retry controlado;
- Dead Letter Topic;
- logs estruturados;
- Testcontainers;
- observabilidade;
- integrações com outros serviços logísticos.
