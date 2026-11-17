# Burim

[![CI](https://github.com/godsGived/burim/actions/workflows/ci.yml/badge.svg)](https://github.com/godsGived/burim/actions/workflows/ci.yml)
![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-6DB33F)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

Burim is a microservice marketplace built to practice distributed backend patterns: REST, Kafka events, Redis, PostgreSQL, Keycloak, Testcontainers, MCP and tool calling.

> Current status: backend v1.0 and AI chat/MCP are implemented.

## Architecture

<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="docs/architecture/architecture-dark.svg">
    <img src="docs/architecture/architecture-light.png" alt="Burim architecture" width="100%">
  </picture>
</p>

<sub>Full-size: [light SVG](docs/architecture/architecture-light.svg) · [dark SVG](docs/architecture/architecture-dark.svg)</sub>

```mermaid
flowchart LR
  Client[Client] --> Gateway[API Gateway :8080]

  Gateway --> Product[Product :8081]
  Gateway --> Cart[Cart :8083]
  Gateway --> Order[Order :8082]
  Gateway --> Review[Review :8084]
  Gateway --> Notification[Notification :8086]
  Gateway --> AI[AI Service :8087]

  Order -->|internal REST| Cart
  Order -->|reserve stock| Product

  Review -.->|product-reviews-topic| Kafka[(Kafka)]
  Order -.->|order-created-topic| Kafka
  Order -.->|order-status-topic| Kafka
  Kafka -.->|rating update| Product
  Kafka -.->|notifications| Notification

  AI -->|MCP Streamable HTTP| MCP[MCP Server :8088]
  MCP -->|public REST| Product
  MCP -->|public REST| Review
  AI -->|OpenAI-compatible| DeepSeek[DeepSeek API]
  External[MCP clients: OpenCode / Cursor / Claude] -->|/mcp| MCP

  Cart --> Redis[(Redis)]
  Product --> PG[(PostgreSQL)]
  Order --> PG
  Review --> PG
  Notification --> PG
  Keycloak[Keycloak :8180] -.->|JWT| Product

  %% MCP is internal and is not routed through the gateway.
```

JWT is validated by each service, not by the gateway. Public reads are available for products, product reviews and single reviews.

## Services

| Service | Port | Responsibility | Storage |
|---|---:|---|---|
| api-gateway | 8080 | Routing, CORS, Springdoc aggregation | — |
| product-service | 8081 | Catalog, filters, pagination, stock reservations | PostgreSQL |
| cart-service | 8083 | Cart | Redis |
| order-service | 8082 | Checkout, idempotency, cancellation | PostgreSQL |
| review-service | 8084 | Reviews and review events | PostgreSQL |
| notification-service | 8086 | Kafka consumers and notifications | PostgreSQL |
| ai-service | 8087 | Chat API, Spring AI, MCP client | — |
| mcp-server | 8088 | Read-only MCP tools over Streamable HTTP | — |

## Core flows

### Checkout

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant G as Gateway
  participant O as Order
  participant Cart as Cart
  participant P as Product
  participant DB as order_db
  participant K as Kafka

  C->>G: POST /api/v1/orders/checkout
  G->>O: forward + Idempotency-Key
  O->>Cart: GET internal cart
  O->>P: reserve stock (operationId)
  O->>DB: save order
  O-)K: OrderCreatedEvent (after commit)
  O-->>C: 201 Created
```

### Review → rating

```mermaid
sequenceDiagram
  participant R as Review
  participant DB as review_db
  participant K as Kafka
  participant P as Product
  participant N as Notification

  R->>DB: save review
  R-)K: ReviewEvent
  K-)P: ReviewEvent
  P->>P: update rating + review count
  K-)N: ReviewEvent
  N->>N: create notification
```

### AI chat → MCP

```mermaid
sequenceDiagram
  participant C as Client
  participant AI as AI Service
  participant LLM as DeepSeek
  participant MCP as MCP Server
  participant P as Product
  participant R as Review

  C->>AI: POST /api/v1/ai/chat (JWT)
  AI->>LLM: prompt + tool schemas
  LLM-->>AI: tool_calls
  AI->>MCP: tools/call
  MCP->>P: public REST
  MCP->>R: public REST
  MCP-->>AI: tool result
  AI->>LLM: tool result
  LLM-->>AI: final answer
  AI-->>C: answer
```

## AI & MCP

- `ai-service` uses Spring AI 2.0.2 with an OpenAI-compatible DeepSeek model.
- `POST /api/v1/ai/chat` is stateless and requires JWT.
- `ai-service` is an MCP client; `mcp-server` is a separate MCP server.
- MCP transport: Streamable HTTP, endpoint `/mcp`.
- Read-only tools: `searchProducts`, `getProduct`, `getProductReviews`.
- `mcp-server` is internal/localhost-only and is not routed through the gateway.
- It can be attached to OpenCode, Cursor or Claude as an external MCP client.

## Quick start

```bash
export SPRING_AI_OPENAI_API_KEY=your_deepseek_key
docker compose up -d --build
```

```bash
curl http://localhost:8080/api/v1/products
```

Get a local Keycloak token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/burim/protocol/openid-connect/token \
  -d client_id=burim-client \
  -d grant_type=password \
  -d username=testuser \
  -d password=password | jq -r .access_token)
```

```bash
curl -s http://localhost:8080/api/v1/ai/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"Find a mechanical keyboard under 150 USD"}'
```

MCP check:

```bash
opencode mcp list
```

## API overview

| Endpoint | Auth | Notes |
|---|---|---|
| `GET /api/v1/products/**` | public | Catalog |
| `POST/PUT/DELETE /api/v1/products/**` | JWT | Product writes |
| `/api/v1/cart/**` | JWT | Cart |
| `/api/v1/orders/**` | JWT | Checkout requires `Idempotency-Key` |
| `GET /api/v1/reviews/products/{id}` | public | No `userId` in public DTO |
| `GET /api/v1/reviews/{id}` | public | No `userId` in public DTO |
| `GET /api/v1/reviews/my` | JWT | Current user's reviews |
| `POST/PUT/DELETE /api/v1/reviews/**` | JWT | Writes and ownership checks |
| `/api/v1/notifications/**` | JWT | Current user's notifications |
| `POST /api/v1/ai/chat` | JWT | AI chat + MCP tool calling |
| `POST /mcp` | internal | MCP Streamable HTTP, not via gateway |

## Testing & CI

- JUnit 5, MockMvc, WireMock.
- Testcontainers for PostgreSQL, Kafka and Redis.
- GitHub Actions matrix per service: `mvn -B clean verify`.

## Known limitations

- Kafka events are published after commit; if Kafka is unavailable, an event can be lost. A transactional outbox is not implemented.
- No TTL or reconciliation for stock reservations.
- No notification deduplication.
- No seller/buyer roles or ownership checks for product writes.
- Cart clear is best-effort; a repeated checkout with the same `Idempotency-Key` still returns `201`.
- Product deletion is hard delete; reviews are not checked against purchases.
- No observability stack or load tests.
- MCP server is unauthenticated and must stay internal/localhost-only.
- AI chat is stateless: no server-side chat history.

## License

No license is declared for this learning project.
