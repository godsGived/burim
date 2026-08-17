# Burim

> [!IMPORTANT]
> **Active Work in Progress / Target Architecture**  
> This project is currently in active development. The architecture diagrams and component specifications below represent the **target system design** being implemented incrementally.

[![CI Pipeline](https://github.com/godsGived/burim/actions/workflows/ci.yml/badge.svg)](https://github.com/godsGived/burim/actions/workflows/ci.yml)

---

Burim is a microservice-based e-commerce platform with AI-assisted
product discovery, comparison and seller tools.

## ✨ What makes Burim different?

- 🤖 AI-powered product comparison
- 💬 Questions about product reviews using RAG
- 🏷️ AI-assisted product listing for sellers
- 💰 AI-powered price recommendations
- 🔎 Tool Calling for live marketplace data
- 📚 RAG over reviews and marketplace documentation

## 🏗️ Architecture

![Architecture](docs/architecture/architecture.png)

Burim uses independent services with database-per-service isolation.

| Service | Responsibility |
|---|---|
| **API Gateway** | Entry point and request routing |
| **Product Service** | Products, categories, inventory management |
| **Review Service** | Product ratings, customer reviews, moderation, and event publishing |
| **Cart Service** | Shopping cart |
| **Order Service** | Orders and order status |
| **AI Service** | LLM integration, Tool Calling and RAG |

### Communication

- **REST** — synchronous requests requiring an immediate response
- **Kafka** — asynchronous domain events and background processing (e.g. review updates, vector indexation)
- **Redis** — cart state
- **PostgreSQL** — persistent service data
- **PGVector** — vector embeddings and semantic search

## 🤖 AI

The AI Service is built around Spring AI.

**Tool Calling**

AI can request live marketplace data through tools exposed by
the Product Service and Review Service.

**RAG**

Two knowledge sources are planned:

- product reviews
- marketplace documentation

This enables queries such as:

> "What do customers complain about most in this laptop?"

> "Compare these three products based on their reviews."

## 📐 Use Cases

![Use Case Diagram](docs/architecture/usecase.png)

## 🛠️ Tech Stack

**Backend**

Java 21 · Spring Boot · Spring Cloud · Spring Data JPA · Hibernate · Flyway

**Data & Messaging**

PostgreSQL · Redis · Apache Kafka · PGVector

**AI**

Spring AI · RAG · Function Calling

**Security & Observability**

Keycloak (OAuth2 / OpenID Connect / JWT) · Actuator · Prometheus · Grafana · Zipkin · Resilience4j

**Infrastructure & CI/CD**

Docker · Docker Compose · Testcontainers · GitHub Actions

## 🚧 Project Status

Currently implemented / in progress:
- 🔄 **Review Service** — Core CRUD, Flyway migrations, Keycloak JWT auth, and Testcontainers integration tests completed; **Kafka event producer** (events on review creation/updates for AI indexing and stats) in progress.
- 🔄 **Product Service** — In development.

The remaining services and AI capabilities will be added incrementally.