# 📊 Trainer Workload Service (Gym CRM)

## 📝 Overview
The **Trainer Workload Service** is an independent microservice within the Gym CRM ecosystem. Its primary responsibility is to calculate, aggregate, and store the total training duration (workload) for each trainer on a monthly and yearly basis.

This service operates completely asynchronously. It listens for `TrainerWorkloadRequest` events from the message broker (ActiveMQ Artemis) and updates the NoSQL database (MongoDB) accordingly.

## 🛠 Tech Stack
* **Java:** 17 / 19
* **Framework:** Spring Boot 3.2.5
* **Database:** MongoDB (Spring Data MongoDB)
* **Messaging:** ActiveMQ Artemis (JMS Consumer)
* **Security:** Spring Security + JWT Validation
* **Testing:** JUnit 5, Mockito, Flapdoodle Embedded MongoDB, Awaitility

## 🚀 Key Features & Task Implementation

This service was developed to fulfill specific NoSQL and high-performance requirements:

### 1. NoSQL Data Modeling
Data is stored hierarchically in MongoDB to optimize read operations and aggregations.
* **Structure:** `TrainerWorkload` (Root) ➡️ `YearSummary` ➡️ `MonthSummary` (Duration).
* **Types:** `trainingDuration` is stored as a numerical value (`long`), and `isActive` as a `Boolean`.

### 2. Asynchronous Messaging & Error Handling
* Acts as a JMS Listener subscribing to `trainer-workload-queue`.
* Utilizes `MappingJackson2MessageConverter` with `_type` header mapping to seamlessly deserialize incoming DTOs from the Main Service without package conflicts.
* Implements robust **Error Handling**: Database failures trigger an explicit exception throw, signaling ActiveMQ to execute Redelivery or route the message to a Dead Letter Queue (DLQ) to prevent data loss.

### 3. Compound Indexing
To support future administrative queries, a **Compound Index** (`first_last_name_idx`) has been implemented on the `firstName` (1) and `lastName` (1) fields, optimizing read performance for trainer searches.

### 4. Two-Level Logging & Traceability
Implements a comprehensive, non-sensitive logging strategy utilizing **MDC (Mapped Diagnostic Context)**:
* **Transaction Level:** Implemented in the `TrainerWorkloadListener`. It extracts the `transactionId` from the JMS message (or generates a fallback UUID) to trace the start, completion, and overarching errors of a single message lifecycle.
* **Operation Level:** Implemented in the `TrainerWorkloadService`. Provides detailed `DEBUG` and `INFO` traces of specific database operations (e.g., adding hours, removing empty months, creating new profiles).

## 🧪 Testing & Quality Assurance
The service maintains a strict adherence to **FIRST principles** and Clean Code (SOLID, KISS, DRY):
* **Unit Tests:** Covers business logic (`TrainerWorkloadService`) and listener behavior (`TrainerWorkloadListener`) using Mockito.
* **Integration Tests:** The `WorkloadIntegrationTest` verifies the full end-to-end flow from the ActiveMQ template to the actual MongoDB save operation, utilizing **Awaitility** for asynchronous assertions.
* **Embedded DB:** Uses `de.flapdoodle.embed.mongo` to ensure tests are isolated, repeatable, and run without requiring external database infrastructure.
* **Coverage:** Achieves **>80%** line coverage (verified via Jacoco).

## ⚙️ How to Run

### Prerequisites
1. **ActiveMQ Artemis** must be running (e.g., via Docker on port `61616`).
2. **MongoDB** must be accessible (configured in `application.yml`).
3. **Eureka Server** (Discovery Service) should be running (if standalone mode is not forced).

### Run Application
```bash
mvn spring-boot:run
```

### 🗄️ NoSQL & Workload Service Features (MongoDB)

This service is built to fulfill specific NoSQL and high-performance requirements:

* **Document Schema:** Data is stored hierarchically in MongoDB (`TrainerWorkload` -> `YearSummary` -> `MonthSummary`) to optimize read operations and aggregations.
* **Compound Indexing:** A compound index (`first_last_name_idx`) is implemented on the `firstName` and `lastName` fields to ensure high-performance search capabilities, ready for future administrative query expansions.
* **Two-Level Logging:** * **Transaction Level:** Implemented at the JMS Listener level, tracking the start, completion, and overarching errors of a message processing cycle using MDC (`transactionId`).
  * **Operation Level:** Implemented at the Service layer, providing detailed `DEBUG`/`INFO` traces of specific database updates (e.g., adding duration, removing empty months).
* **Reliability & Error Handling:** The JMS Listener is designed with explicit error propagation. Any database failure during the update process throws an exception back to ActiveMQ, triggering the broker's Redelivery or Dead Letter Queue (DLQ) mechanisms to prevent data loss.