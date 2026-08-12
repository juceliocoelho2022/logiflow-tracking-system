# LogiFlow Tracking

Sistema de rastreamento logístico orientado a eventos, construído com Java 21, Spring Boot, Apache Kafka, PostgreSQL e Docker.

## Objetivo desta primeira versão

A V1 entrega o núcleo do domínio de rastreamento:

1. recebe um evento de status via REST;
2. publica o evento no Kafka (`tracking.events.v1`);
3. consome o evento de forma assíncrona;
4. aplica idempotência por `eventId`;
5. persiste o histórico no PostgreSQL;
6. disponibiliza a timeline pelo código de rastreamento.

## Arquitetura

```text
Cliente / outro microsserviço
          |
          | POST /api/tracking/events
          v
+-----------------------+
|   tracking-service    |
|      Spring Boot      |
+-----------+-----------+
            |
            | publish
            v
+-----------------------+
| Apache Kafka          |
| tracking.events.v1    |
+-----------+-----------+
            |
            | consume
            v
+-----------------------+
| tracking-service      |
| consumer + idempotência|
+-----------+-----------+
            |
            v
+-----------------------+
| PostgreSQL            |
| tracking_event        |
+-----------------------+
```

## Tecnologias

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Data JPA
- Spring for Apache Kafka
- PostgreSQL
- Flyway
- Docker / Docker Compose
- Spring Boot Actuator
- JUnit 5 + Mockito

## Estrutura

```text
logiflow-tracking-system/
├── docker-compose.yml
├── requests.http
├── README.md
└── tracking-service/
    ├── Dockerfile
    ├── pom.xml
    └── src/
        ├── main/java/br/com/logiflow/tracking/
        │   ├── config/
        │   ├── controller/
        │   ├── dto/
        │   ├── entity/
        │   ├── kafka/
        │   ├── repository/
        │   └── service/
        ├── main/resources/
        │   ├── application.yml
        │   └── db/migration/
        └── test/
```

## Como executar com Docker

Na raiz do projeto:

```bash
docker compose up --build
```

Serviços:

- Tracking API: `http://localhost:8090`
- Actuator Health: `http://localhost:8090/actuator/health`
- Kafka UI: `http://localhost:8091`
- PostgreSQL: `localhost:5435`
- Kafka: `localhost:9092`

## Como executar o serviço pela IDE

Suba somente a infraestrutura:

```bash
docker compose up -d postgres kafka kafka-ui
```

Depois abra `tracking-service` no IntelliJ IDEA e execute `TrackingServiceApplication`.

Também é possível usar Maven:

```bash
cd tracking-service
mvn spring-boot:run
```

## Teste rápido

### Publicar evento

```bash
curl -X POST http://localhost:8090/api/tracking/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId":"64f16422-54aa-4e83-8dc3-78edfd9da001",
    "trackingCode":"LF2026000145BR",
    "orderId":"PED-2026-000145",
    "status":"PEDIDO_CRIADO",
    "city":"São Paulo",
    "state":"SP",
    "description":"Pedido criado e aguardando processamento",
    "occurredAt":"2026-08-12T08:35:00Z"
  }'
```

### Consultar timeline

```bash
curl http://localhost:8090/api/tracking/LF2026000145BR
```

O arquivo `requests.http` contém requisições prontas para executar diretamente pelo IntelliJ IDEA.

## Status disponíveis

- `PEDIDO_CRIADO`
- `PAGAMENTO_APROVADO`
- `ESTOQUE_RESERVADO`
- `EM_SEPARACAO`
- `EXPEDIDO`
- `EM_TRANSPORTE`
- `SAIU_PARA_ENTREGA`
- `ENTREGUE`
- `ENTREGA_NAO_REALIZADA`
- `CANCELADO`

## Roadmap sugerido

### Sprint 1 — núcleo de tracking
- [x] Serviço Spring Boot
- [x] PostgreSQL + Flyway
- [x] Kafka producer/consumer
- [x] Timeline de eventos
- [x] Idempotência por eventId
- [x] Docker Compose
- [x] Teste unitário inicial

### Sprint 2 — integração logística
- [ ] Integrar `pedido-service`
- [ ] Integrar `estoque-service`
- [ ] Integrar `expedicao-service`
- [ ] Adicionar Dead Letter Topic
- [ ] Retry controlado
- [ ] Observabilidade e correlationId

### Sprint 3 — experiência do usuário
- [ ] Dashboard React
- [ ] Atualização em tempo real via SSE/WebSocket
- [ ] Mapa de movimentações
- [ ] Previsão de entrega
- [ ] Notificações

### Sprint 4 — arquitetura avançada
- [ ] Transactional Outbox
- [ ] Redis para consultas frequentes
- [ ] API Gateway
- [ ] Segurança
- [ ] Testcontainers
- [ ] CI/CD com GitHub Actions

## Decisão arquitetural importante

O endpoint de escrita retorna HTTP `202 Accepted`, pois o processamento definitivo é assíncrono: o evento entra no Kafka e o consumidor atualiza a timeline. Isso modela melhor um fluxo logístico distribuído do que persistir tudo diretamente no controller.
