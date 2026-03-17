# Gym CRM Microservices System

A distributed RESTful application for managing gym operations, trainees, trainers, and training sessions. This project has been migrated from a monolithic architecture to a **Microservices Architecture** using Spring Cloud.

## 🏗 Architecture

The system consists of the following microservices:

1.  **Discovery Server (Eureka):** Service registry for dynamic service discovery.
2.  **Trainer Workload Service:** Handles workload calculations and analytics using **MongoDB** (Embedded).
3.  **Gym Main Service:** The core application handling users, authentication, and trainings using **PostgreSQL**.
4.  **Message Broker (ActiveMQ Artemis):** Facilitates asynchronous communication between the Main Service and the **Workload Service**.

## 🚀 Messaging Implementation (Current Module)
In this module, direct REST communication between services was replaced with **asynchronous communication** using **ActiveMQ Artemis**.
* **Asynchronous Flow**: When a training session is created in the Main Service, it sends a JSON message to the trainer-workload-queue.
* **Decoupling**: The Main Service no longer waits for the Workload Service to finish processing.
* **Reliability**: The use of a message broker ensures that workload updates are processed even if the Workload Service is temporarily unavailable.

## 🛠 Tech Stack

* **Java:** 19+
* **Spring Boot:** 3.2.x
* **Spring Cloud:** 2023.x (Eureka, OpenFeign, Circuit Breaker/Resilience4j)
* **Messaging:** ActiveMQ Artemis (Starter Artemis)
* **Databases:**
    * PostgreSQL (Main Service)
    * MongoDB Embedded (Workload Service)
* **Security:** Spring Security + JWT (Shared Secret Key)
* **Build Tool:** Maven (Multi-module)
* **Testing:** JUnit 5, Mockito, Spring Boot Test, Awaitility, Cucumber

## ⚙️ Prerequisites

* **Docker:** Required to run the ActiveMQ Artemis broker locally.
* **PostgreSQL:** Installed and running locally on port 5432 with a database named gym_db.
* **Java JDK 19** or higher.

## 🚀 Getting Started

### 1. Run ActiveMQ Artemis (via Docker)
Start the message broker using the following command:
```Bash
docker run -d --name artemis -p 61616:61616 -p 8161:8161 -e ARTEMIS_USER=admin -e ARTEMIS_PASSWORD=admin apache/activemq-artemis:latest-alpine
```

### 2. Configuration
The system supports multiple environments via Spring profiles.
* **Native Mode:** Connects to the external broker (Docker).
* **Embedded Mode:** Used in integration tests to provide an In-Memory broker.

### 3. Running the Services (Order Matters!)
   You must start the services in the following order to ensure proper registration:

#### 1. Start Discovery Server: Run DiscoveryServerApplication (Port: 8761) Wait until it fully starts.

#### 2. Start Trainer Workload Service: Run WorkloadApplication (Port: 8081) Wait until it registers with Eureka.

#### 3. Start Gym Main Service: Run GymApplication (Port: 8080)

### 4. Verification
   Open the Eureka Dashboard to verify all services are UP: http://localhost:8761

You should see:

* GYM-MAIN-SERVICE

* TRAINER-WORKLOAD-SERVICE

## 🔌 API Usage
The main entry point is the Gym Main Service on port 8080.

Authentication Flow
The system uses JWT. You must obtain a token first.

#### 1. Login: POST /api/v1/auth/login

```
JSON
{
"username": "trainer.user",
"password": "password123"
}
```
Response: Copy the token.

#### Core Endpoints
#### 2. Create Training (Triggers Workload Update): 
POST /api/v1/trainings 
Header: Authorization: Bearer <your_token>
```
JSON
{
"traineeUsername": "trainee.user",
"trainerUsername": "trainer.user",
"trainingName": "Cardio",
"trainingDate": "2026-06-01",
"trainingDuration": 60
}
```

#### 3. Update Trainee's Trainers: 
PUT /api/v1/trainees/{username}/trainers Header: 
Authorization: Bearer <your_token>
```
JSON
[ "trainer.user" ]
```

## 🧪 Testing
To run unit and integration tests across all modules:
```
Bash
mvn test
```
* Coverage: Includes Unit tests for Services, Mappers, and DTOs, plus Integration tests for Repositories and Controllers using H2 and Embedded Mongo.

## 🛡 Fault Tolerance
* The system implements Circuit Breaker (Resilience4j) on the Gym Main Service. If the Workload Service goes down:

* The Main Service will not crash.

* The transaction will complete locally.

* An error will be logged, and a fallback method will be triggered (e.g., for future retry via Message Queue).