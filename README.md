# Burim

Burim is a microservice-based e-commerce platform with integrated AI features for search, recommendations, and merchant analytics.

## Architecture

The system is designed as a set of independent microservices with database-per-service isolation:

- **API Gateway:** Entry point for clients, routing, and token validation.
- **Product Service:** Manages product catalog, categories, inventory, and reviews.
- **Cart Service:** Shopping cart management.
- **Order Service:** Order processing and status tracking.
- **AI Service:** Handles chat queries, Tool Calling, and RAG vector indexing.
- **Keycloak:** OAuth2 / OpenID Connect authorization server.

### Inter-Service Communication

- **Synchronous (REST):** Used for real-time user requests (browsing products, cart operations, direct AI chat).
- **Asynchronous (Apache Kafka):** Used as an event bus (`ProductUpdatedEvent`, `OrderCreatedEvent`) for background processing and updating the AI vector store without blocking main API responses.

![Burim Architecture](docs/architecture/architecture.png)

## AI Integration

- **Tool Calling (REST):** AI Service queries Product Service directly for live stock and pricing data.
- **RAG (Retrieval-Augmented Generation):** Semantic search using product descriptions, reviews, and documentation.
- **Event-Driven Indexing:** Product and order changes are consumed from Kafka topics and written to the vector store (`PGVector`).

## Tech Stack

### Implemented
- **Language:** Java 21
- **Framework:** Spring Boot 3
- **Data:** Spring Data JPA, Hibernate, Flyway
- **Database:** PostgreSQL
- **Environment:** Docker, Docker Compose
- **Build Tool:** Maven

### Planned
- **Gateway & Security:** Spring Cloud Gateway, Keycloak
- **Messaging & Cache:** Apache Kafka, Redis
- **AI Engine:** Spring AI, PGVector
- **Observability:** Prometheus, Grafana, Zipkin

## Use Cases

![Burim Use Case Diagram](docs/architecture/usecase.png)