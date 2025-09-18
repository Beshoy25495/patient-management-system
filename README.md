# Patient Management System (Microservices Architecture)

A distributed, microservices-based **Patient Management System** designed for scalability, resilience, and observability.  
This system manages patient data, billing, notifications, and analytics, with asynchronous communication using **Kafka** and **gRPC**.  
It provides a foundation for healthcare providers to streamline workflows by organizing patient records, scheduling appointments, and integrating billing and notifications.

> **Status:** Beta – Initial patient CRUD and Kafka event-driven workflows implemented.

---

## 🚀 Features

- **Microservices Architecture**
  - Modular services: Patient, Billing, Notification, Analytics, Auth
  - Centralized API Gateway for routing and authentication
- **Event-Driven Design**
  - Kafka for asynchronous communication between services
  - Generic Protobuf-based events `<T>` for flexibility
- **Rollback / Saga Pattern (Planned)**
  - Distributed transaction handling for patient creation and other critical workflows
- **Observability with ELK Stack (Planned)**
  - Centralized logging and monitoring using Elasticsearch, Logstash, and Kibana
- **Authentication & Authorization**
  - Dedicated Auth service with JWT support

---

## 🗂 Project Structure
