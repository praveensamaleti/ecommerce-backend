# E-commerce Backend - Spring Boot REST API

This project provides a robust, production-ready backend for an e-commerce application, built with Java Spring Boot, Maven, and Spring Security with JWT.

## 🚀 Features

- **Authentication & Security**:
  - JWT-based stateless authentication.
  - Access and Refresh token mechanisms.
  - Role-based access control (`user`, `admin`).
- **Product Management**:
  - Full CRUD for products (Admin only).
  - Advanced filtering (query, category, price range).
  - Pagination support.
- **Order Processing**:
  - Secure order placement.
  - Automated stock management.
  - Tax and total calculation.
- **API Documentation**:
  - Interactive Swagger UI (OpenAPI 3.0).
- **Database**:
  - In-memory H2 database (easy for testing).

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.3**
- **Spring Security**
- **Spring Data JPA**
- **Maven**
- **H2 Database**
- **Lombok**
- **SpringDoc OpenAPI (Swagger)**

## 🚦 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker (optional)

### Running Locally

1. Clone the repository.
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. The API will be available at `http://localhost:8080/api`.

### Running with Docker

1. Build the image:
   ```bash
   docker build -t ecommerce-backend .
   ```
2. Run the container:
   ```bash
   docker run -p 8080:8080 ecommerce-backend
   ```

## 📖 API Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:mem:ecommercedb`
  - **User**: `sa`
  - **Password**: (leave empty)

## 🔑 Default Credentials

The application initializes with default users for testing:

| Role  | Email                | Password             |
|-------|----------------------|----------------------|
| Admin | `admin@example.com`  | `admin123`           |
| User  | `john@example.com`   | `securepassword123`  |

## 📂 Project Structure

- `com.ecommerce.backend.controller`: REST API endpoints.
- `com.ecommerce.backend.service`: Business logic and processing.
- `com.ecommerce.backend.repository`: Data access layer.
- `com.ecommerce.backend.entity`: JPA entities.
- `com.ecommerce.backend.dto`: Data Transfer Objects.
- `com.ecommerce.backend.security`: JWT and Security configuration.
- `com.ecommerce.backend.config`: General configuration and data initialization.
- `com.ecommerce.backend.enums`: Enums for Roles, Categories, and Order Status.

