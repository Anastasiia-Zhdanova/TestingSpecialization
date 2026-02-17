# 🏋️ Gym Management REST API (Spring Boot)

## 📝 Project Overview
# Gym CRM Main Service
The core microservice of the Gym CRM System, responsible for user management, training scheduling, and authentication. This service orchestrates the system's business logic and communicates asynchronously with the Workload Service.

## 🏗 Microservices Architecture
This service is part of a distributed system:

* ***Gym Main Service (this one):*** Processes core logic and training creation.
* ***Discovery Server:*** Eureka-based service registry.
* ***Trainer Workload Service:*** Handles trainer statistics via MongoDB.
* ***Message Broker:*** ActiveMQ Artemis for asynchronous communication.

## 🚀 Messaging Features
* ***Asynchronous Processing:*** Replaced legacy REST/OpenFeign calls with JMS messages to improve performance and reliability.
* ***ActiveMQ Artemis Integration:*** Fully managed by Spring Boot Starter Artemis with Jakarta EE support.
* ***JSON Serialization:*** Reliable data exchange using MappingJackson2MessageConverter with custom type ID mappings to handle cross-service DTO package differences.

## 🛠 Tech Stack
* Java: 19
* Spring Boot: 3.2.5
* Database: PostgreSQL
* Messaging: ActiveMQ Artemis (Starter)
* Security: Spring Security + JWT (Externalized Configuration)
* Monitoring: Spring Boot Actuator

## ⚙️ Configuration & Security
To enhance security, sensitive data like the JWT Secret Key has been externalized from the source code.
### Environment Variables
You can override default settings using environment variables:
* JWT_SECRET: Secret key for HMAC-SHA signatures (min. 32 chars).
* ARTEMIS_URL: URL of the ActiveMQ broker (default: tcp://localhost:61616).

## 🚀 Getting Started
1. Prerequisites
    * ***Docker:*** To run the message broker.
    * ***PostgreSQL:*** Database gym_db should be available on port 5432.
    * ***Eureka Server:*** Must be running on port 8761.

2. Run ActiveMQ Broker
   ```Bash
   docker run -d --name artemis -p 61616:61616 -p 8161:8161 -e ARTEMIS_USER=admin -e ARTEMIS_PASSWORD=admin apache/activemq-artemis:latest-alpine
   ```
3. Run the Application
   ```Bash
   mvn spring-boot:run
   ```
## 🔌 API Guide
### Authentication
```POST /api/v1/auth/login```

* Exchange credentials for a JWT.
* The secret is managed via ***application.yml*** and injected into ***JwtService***.

### Training Management
```POST /api/v1/trainings```

* ***Input:*** Training details (Trainee, Trainer, Date, Duration).
* ***Process:*** 1. Saves training to PostgreSQL. 2. Sends a TrainerWorkloadRequest to trainer-workload-queue.
* ***Response:*** 200 OK (Async acknowledgment).

## 🛡 Fault Tolerance
* ***Decoupling:*** If the Workload Service is down, messages stay safely in the Artemis queue until the service recovers.
* ***Transaction ID (TID):*** All logs include a unique TID to trace a single request across the entire microservice landscape.

## 📝 Project Overview

This project is an enterprise-ready, multi-layered Java application built with **Spring Boot 3** and **Hibernate/JPA**. It provides a comprehensive RESTful API for managing Trainees, Trainers, and Training sessions.

The system is designed for high availability and observability, featuring:

* **Security:** Spring Security with **Redis** for stateful session management. Stateless authentication using **JWT (JSON Web Tokens)** with **Brute Force Protection** (account locking).
* **Observability:** Integrated **Spring Boot Actuator** for health monitoring and **Prometheus** metrics via Micrometer for performance tracking.
* **Configuration:** Full support for multi-environment deployments using **Spring Profiles** (`local`, `dev`, `stg`, `prod`).
* **AOP:** Custom Aspect for request tracing (Transaction ID/MDC logging).
* **Data Layer:** JPA/EntityManager-based DAOs (migrated from manual `HibernateUtil`) for clean and efficient data access.

---

### Key Architectural Pillars:
* ***Asynchronous Messaging:*** Integrated ActiveMQ Artemis to handle inter-service communication. The system utilizes a fire-and-forget pattern for updating trainer workloads, ensuring the core service remains highly responsive.
* ***Security:*** Secured by Spring Security with stateless JWT (JSON Web Tokens) authentication. It includes Brute Force Protection with account locking mechanisms. Critical security credentials (like JWT secrets) are externalized for production safety.
* ***Polyglot Persistence: * PostgreSQL:*** Used by the Main Service for complex relational data and transactional integrity. 
  * MongoDB: Used by the Workload Service for high-performance aggregation and storage of trainer statistics.

* ***Observability & Tracing: * Spring Boot Actuator*** for real-time health monitoring.

* ***Prometheus*** integration for performance metrics.

* ***Custom AOP Logging:*** Implements a Transaction ID (TID) tracing system that tracks requests across microservice boundaries via JMS headers.

* ***Infrastructure: * Service Discovery:*** Eureka Server for dynamic registration.

* ***Containerization:*** Full support for running infrastructure (ActiveMQ) via Docker.

* ***Engineering Excellence:*** Adheres to SOLID principles, utilizing Design Patterns (Builder, Factory, Proxy) and ensuring high test coverage with FIRST-compliant Unit and Integration tests.
___

## 🏗️ Project Structure & Key Components

This project follows a strict layered architecture to ensure separation of concerns.

| Layer                    | Key Components (Files) | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|:-------------------------| :--- |:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| *API (Controllers)**                      | `AuthenticationController`, `TraineeController`, `TrainerController`, `TrainingController` | REST endpoints for user lifecycle and training scheduling.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **Messaging**        | `JmsConfig`, `TrainingService` | Handles asynchronous message production to ActiveMQ Artemis.                                                                                                                        |
| **Business Logic**             | `AuthService`, `TraineeService`, `TrainerService`, `TrainingService` | Core business rules, credential generation, and transaction management.                                                                                                                                                                                                                                                                                                                                                                                                                |
| **Security**    | `WebSecurityConfig`, `JwtService`, `JwtAuthenticationFilter` | Externalized JWT security and stateless authentication.                                                                                                                                                                                                                             |
| **Data Access**       | `UserDAO`, `TraineeDAO`, `TrainerDAO`, `TrainingDAO` | JPA/EntityManager-based access to PostgreSQL.                                                                                                                                                                         |
| **Persistence** | `User`, `Trainee`, `Trainer`, `Training`, `TrainingType` | Relational entities with JPA mappings.                                                                                                                                                                                                                                                  |

---

## 🚀 Running the Application & Profiles

### Prerequisites

1.  **Java Development Kit (JDK) 19+**
2.  **Maven 3.6+**
3.  **PostgreSQL Server** (Required for `dev/stg/prod` profiles)
4.  **Redis Server** (Required for session management, e.g., `localhost:6379` for local)

### Build and Package

First, build the executable JAR file:
```bash
mvn clean install
```
### Running with Profiles

This application uses Spring Profiles to manage configurations across different environments. You activate a profile using the `--spring.profiles.active=...` argument.

|Profile   |  File | Database                | ddl-auto  |  Purpose |
|---|---|-------------------------|---|---|
| local  | application-local.yml  | H2 In-Memory            | create-drop  |  Local development & testing. |
|  dev |  application-dev.yml | PostgreSQL (Dev DB)     | update  | Development integration environment.  |
| stg  |  application-stg.yml | PostgreSQL (Staging DB) | validate  |  Pre-production testing. |
| prod  |  application-prod.yml | PostgreSQL (Prod DB)    | validate  | Live production environment.  |

#### 1. How to run (`local` profile)

The `local` profile is designed to run "out-of-the-box" using an H2 in-memory database. It automatically loads `data.sql`.
```bash
# Run using the local profile
java -jar target/gym-management-rest-1.0-SNAPSHOT.jar --spring.profiles.active=local
```

#### 2. How to run (`dev`, `stg`, `prod` profiles)

These profiles require an external PostgreSQL database and a password passed as an Environment Variable (to avoid storing secrets in code).

Example: Launching the `stg` (Staging) Environment

##### 1.Set the Environment Variable (macOS/Linux):
```bash
export STG_DB_PASSWORD=your_secure_staging_password
```
(For Windows, use: `set STG_DB_PASSWORD=your_secure_staging_password`)

##### 2.Run the Application with the `stg` profile active:
```bash
java -jar target/gym-management-rest-1.0-SNAPSHOT.jar --spring.profiles.active=stg
```

The application will start, read the database password from the `STG_DB_PASSWORD` variable, and connect to the Staging PostgreSQL database.

### 📊 Observability (Actuator & Metrics)
This project uses **Spring Boot Actuator** and **Micrometer** to provide deep insights into the application's health and performance.

#### 1. Spring Boot Actuator
   Actuator provides production-ready endpoints for monitoring.

1) **How it's implemented:** Enabled by adding the `spring-boot-starter-actuator` dependency in pom.xml.

2) How it's configured: Endpoints are exposed in `application.yml` via `management.endpoints.web.exposure.include: "*" `.

Key Endpoint: `GET /actuator/health`

#### 2. Custom Health Indicators
   I have implemented two custom health indicators that integrate with the `/actuator/health` endpoint:

##### 1. `TrainingTypeInitialLoadHealthIndicator`

1) Purpose: Ensures that the reference data (e.g., from `data.sql`) has been successfully loaded into the database, which is critical for creating Trainers.

2) Implementation: Injected `TrainingTypeDAO` and checks if `findAll()` returns an empty list.

3) Location: `com.company.gym.config.TrainingTypeInitialLoadHealthIndicator`

4) Status: `DOWN` if the `training_type` table is empty.

#### 2. Custom Metrics (Prometheus)
   I use **"Micrometer"** to define custom metrics, which are then exposed at the `/actuator/prometheus` endpoint for scraping.

##### 1. Metric: User Registrations (Counter)

1) Name: `app.user.registrations.total`

2) Purpose: Tracks the total number of new Trainee and Trainer profiles created.

3) Implementation: A `Counter` is initialized in the `AuthService` constructor (using `MeterRegistry`).

4) Location (Where): It is incremented (`via .increment()`) in the `assignUniqueUsernameAndPassword` method of `AuthService`.

##### 2. Metric: Training Creation Time (Timer)

1) Name: `app.training.creation.time`

2) Purpose: Measures the latency (duration) of the `createTraining` method, including database validation and insertion.

3) Implementation: A `Timer` is initialized in the `TrainingService` constructor (using `MeterRegistry`).

4) Location (Where): The entire business logic of the `createTraining` method is wrapped in a `Timer.record(...)` lambda block.

## 🗺️ API Usage and Documentation

### Swagger Documentation

The full interactive API documentation is available here for testing all **17 endpoints**:

* **URL:** `http://localhost:8080/swagger-ui.html`

### Key Endpoints

Authentication is handled via **Redis Sessions**. After a successful `POST /login`, a session cookie (`JSESSIONID`) must be included in all protected requests.

| Req.   | Description                          | Method   | Path                                                       | Auth Required |
|:-------|:-------------------------------------|:---------|:-----------------------------------------------------------| :--- |
| 1, 2   | **Registration** (Trainee/Trainer)   | `POST`   | `/api/v1/auth/{type}/register`                             | ❌ |
| 3      | **Login** (Establish Session)        | `POST`   | `/api/v1/auth/login`                                       | ❌ |
| 4      | **Change Password**                  | `PUT`    | `/api/v1/auth/change-password`                             | ✅ |
| 7      | **Delete Trainee** (Cascade)         | `DELETE` | `/api/v1/trainees/{username}`                              | ✅ |
| 14     | **Add Training**                     | `POST`   | `/api/v1/trainings`                                        | ✅ |
| 15, 16 | **Activate/Deactivate**              | `PATCH`  | `/api/v1/{type}s/{username}/status`                        | ✅ |
| 17     | **Get Training Types**               | `GET`    | `/api/v1/training-types`                                   | ❌ |
| 5,8    | **Get Profile** (Trainee/Trainer)    | `GET`    | `/api/v1/{type}s/{username}`                               | ✅ |
| 6,9    | **Update Profile** (Trainee/Trainer) | `PUT`    | `/api/v1/{type}s/{username}`                               | ✅ |
| 11     | **Update Profile** (Trainee/Trainer) | `PUT`    | `/api/v1/trainees/{username}/trainers `                    |	✅ |
| 10     | **Get Unassigned Trainers**          | `GET`    | `	/api/v1/trainees/{traineeUsername}/unassigned-trainers ` |	✅ |
| 12, 13 | **Get Trainings List**               | `GET`    | `	/api/v1/{type}s/{username}/trainings `                   |	✅ |
| 14     | **Add Training**                     | `POST`    | `	/api/v1/trainings `                   |	✅|

---

### 🛡️ Engineering Excellence
1. **Security**: Authentication relies on **Spring Security** and **BCrypt** hashing for password storage.

2. **Traceability (AOP)**: A unique **Transaction ID (TID)** is generated (via `LoggingAspect`) and logged for every REST request, enabling end-to-end tracing.

3. **Error Handling**: Custom exceptions are centrally managed by `GlobalExceptionHandler`, ensuring predictable and standardized HTTP status responses.

4. **Code Quality**: Adherence to SOLID principles, DTO validation via **Jakarta Validation**, and efficient object mapping using **MapStruct**.

## Task
### Spring Boot
**1. Based on the codebase created during the previous module, implement follow functionality:**

1) Convert existing application to be `Spring boot Application`.

2) Enable `actuator`. 
* Implement a few custom `health indicators`.
* Implement a few custom metrics using `Prometheus`.
3) Implement support for different environments (`local`, `dev`, `stg`, `prod`). Use Spring profiles.

### Notes:
1. Cover code with unit tests. Code should contain proper logging.
2. Pay attention that each environment - different db properties.
3. All functions except Create Trainer/Trainee profile. Should be executed only after Trainee/Trainer authentication (on this step should be checked
username and password matching).