# Dynamic GraphQL Engine

This project contains a Spring Boot + Netflix DGS based runtime that implements the Dynamic GraphQL Service Framework outlined in the design document. The engine loads GraphQL schemas, validation rules, pipelines, and plugin descriptors dynamically from ZooKeeper while persisting JSON documents in MongoDB and publishing change events to Kafka.

## Features

* **Dynamic schema loading** – merges a built-in schema with SDL fragments stored in ZooKeeper and hot swaps the executable schema at runtime with watcher-driven refreshes.
* **Extensible logic pipelines** – declarative pipeline definitions trigger transactional primitives (validation, FK checks, persistence) and pluggable handlers during persist/update/read/cron phases, automatically reloading when ZooKeeper descriptors change.
* **Unified API surface** – exposes GraphQL, REST, and a gRPC façade that delegate to the same service layer and pipeline executor.
* **Custom scalars** – DateTime, Geo, and JSON scalars respect caller formatting preferences conveyed via HTTP headers.
* **Concurrency & persistence** – Mongo writes apply optimistic locking with automatic version increments, timestamps, and configurable connection settings.
* **Observability** – propagates request identifiers, exposes Micrometer metrics, and prepares hooks for structured logging and tracing.
* **Security context** – derives request security metadata from headers for downstream authorization integrations.

## Project Layout

```
src/main/java/com/example/dynamicgraphql
├── cdc/ChangeDataCapturePublisher.java
├── config/EngineConfiguration.java
├── context/... (request context and thread-local helpers)
├── graphql/... (GraphQL providers and data fetchers)
├── grpc/... (gRPC adapter)
├── observability/MetricsBinder.java
├── persistence/... (MongoDB + FK helpers)
├── pipeline/... (pipeline model, primitives, catalog, executor)
├── plugin/... (plugin SPI and loader)
├── scalars/... (custom GraphQL scalars)
├── security/SecurityContextFactory.java
├── service/DocumentMutationService.java
├── web/... (REST controllers and filters)
└── zk/... (ZooKeeper integrations)
```

## Running Locally

1. Ensure Java 25 is installed and ZooKeeper, MongoDB, and Kafka endpoints are available (defaults: `localhost:2181`, `localhost:27017`, `localhost:9092`). Override these via `engine.zookeeper.connection-string`, `engine.persistence.connection-string`, and `engine.kafka.bootstrap-servers` if needed.
2. Populate ZooKeeper nodes under `/dynamic/graphql/schema` (SDL files) and `/dynamic/graphql/pipelines` (JSON pipeline descriptors).
3. Build and run the service:

```bash
./mvnw spring-boot:run
```

4. Access GraphQL at `http://localhost:8080/graphql`, REST endpoints under `/api`, and expose gRPC by wiring the `DocumentGrpcService` into a server bootstrap.

## Testing

```bash
./mvnw test
```

The current test suite focuses on compilation; additional integration tests should be added to validate schema hot-reload, pipeline execution, and plugin lifecycle management.
