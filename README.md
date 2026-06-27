# BizKredit

BizKredit is a Spring Boot REST API application for managing SME business loans and working capital facilities.

The application supports key loan management activities such as user authentication, SME business onboarding, loan application processing, credit analysis, collateral management, facility disbursement, covenant monitoring, and notifications.

---

## Tech Stack

- Java 21
- Spring Boot 3.2.5
- Spring Data JPA
- Hibernate
- MySQL 8
- Spring Security
- JWT
- Maven
- Lombok
- JUnit 5
- Mockito
- Swagger / OpenAPI

---

## Features

- User registration and login
- JWT-based authentication
- SME business profile management
- Loan application management
- Financial and credit analysis
- Collateral management
- Facility and drawdown management
- Covenant monitoring
- Notifications and alerts
- Swagger API documentation
- Unit testing

---

## Architecture

The project follows a layered architecture:

Controller -> Service -> Repository -> Database

- Controller handles REST API requests
- Service contains business logic
- Repository communicates with the database
- Entity represents database tables
- DTO handles request and response data
- Security handles authentication and authorization
- Exception layer handles errors globally

---

## Prerequisites

Before running the project, make sure the following are installed:

- JDK 21
- MySQL 8
- Maven

---

## Database Configuration

Update the database details in:

src/main/resources/application.properties

Example:

spring.datasource.url=jdbc:mysql://localhost:3306/bizkredit_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
server.port=8081

---

## How to Run

Build the project:

mvn clean install

Run the application:

mvn spring-boot:run

The application will start at:

http://localhost:8081

---

## Swagger Documentation

After starting the application, Swagger UI can be accessed at:

http://localhost:8081/swagger-ui.html

---

## Testing

Run the test cases using:

mvn test

---

## Summary

BizKredit is a backend REST API application built using Spring Boot 3.2.5 for managing SME loan operations in a secure and structured way.