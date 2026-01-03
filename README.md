# Chatly Backend - Spring Boot Real-Time Engine

This is the core messaging engine for the Chatly application. It handles authentication, WebSocket management via STOMP, and persistent storage of messages and chat metadata.

## 🚀 Features
* **Real-time Messaging**: Full-duplex communication using Spring WebSocket and STOMP.
* **JWT Authentication**: Secure stateless authentication for REST and WebSocket connections.
* **Presence Tracking**: Real-time online/offline status updates.
* **Media Support**: Integration for handling file and image uploads.
* **Relational Persistence**: Managed via Spring Data JPA.

## 🛠 Tech Stack
* **Java 17+**
* **Spring Boot 3.x**
* **Spring Security** (JWT)
* **Spring WebSocket** (STOMP + SockJS)
* **MySQL/PostgreSQL** (H2 for development)
* **Maven**

## 🏁 Getting Started

### Prerequisites
* JDK 17 or higher
* Maven 3.6+
* **Redis Server** (Running on port 6379)

### Configuration
1. Clone the repository.
2. Update `src/main/resources/application.properties` with your database credentials and JWT secret.
3. Run the application:
   ```bash
   mvn spring-boot:run


📡 API Endpoints (Brief)

    POST /api/auth/register - User registration

    POST /api/auth/login - Returns JWT token

    GET /api/chats - Fetch user's conversation list

    WS /ws - WebSocket handshake endpoint