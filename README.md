# BizKredit — SME Business Loan & Working Capital Platform

A Spring Boot REST API backend for managing SME business loans, from onboarding and credit underwriting through facility disbursement, collateral management, covenant monitoring, and notifications.

Built as part of the Cognizant GenC IDE Java FSE (React) Stage 2 Plus program.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Persistence | Spring Data JPA, Hibernate 6.4.4 |
| Database | MySQL 8 |
| Security | Spring Security, JWT (JJWT 0.12.3) |
| Boilerplate | Lombok |
| Logging | SLF4J |
| Testing | JUnit 5, Mockito, AssertJ |
| API Docs | SpringDoc OpenAPI / Swagger UI 2.5.0 |
| Build | Maven |

---

## Architecture

The application follows a standard layered architecture:

```
Controller  ->  Service  ->  Repository  ->  Database
   (REST)     (business)     (Spring Data)    (MySQL)
```

- **Controller layer** — exposes REST endpoints, validates input with `@Valid`
- **Service layer** — business logic, `@Transactional` boundaries
- **Repository layer** — Spring Data JPA interfaces
- **Entity layer** — JPA-mapped domain objects
- **DTO layer** — request/response records for auth
- **Security layer** — JWT filter, Spring Security configuration
- **Exception layer** — centralised handling via `@RestControllerAdvice`

---

## Modules

| Module | Area | Owner |
|--------|------|-------|
| 4.1 | Identity & Access Management + JWT Security | Harish (Lead) |
| 4.2 | SME Business Profile | Dileep |
| 4.3 | Loan Application | Dileep |
| 4.4 | Financial Analysis & Credit Underwriting | Subhishka |
| 4.5 | Collateral Management | Affrina |
| 4.6 | Facility Disbursement | Affrina |
| 4.7 | Covenant & Portfolio Monitoring | Harshat |
| 4.8 | Notifications & Alerts | Harshat |

---

## Security

All endpoints except authentication and Swagger require a valid JWT.

- `POST /api/auth/register` — register a user, returns a JWT
- `POST /api/auth/login` — authenticate, returns a JWT

Pass the token on subsequent requests:

```
Authorization: Bearer <token>
```

Passwords are hashed with BCrypt and never returned in API responses. Sessions are stateless.

---

## Getting Started

### Prerequisites

- JDK 21
- MySQL 8 running on `localhost:3306`
- Maven 3.9+

### Configuration

Update `src/main/resources/application.properties` with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bizkredit_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
server.port=8081
```

The database `bizkredit_db` is created automatically on first run.

### Build & Run

```bash
# Build and run tests
mvn clean install

# Run the application
mvn spring-boot:run
```

The application starts on `http://localhost:8081`.

### API Documentation

Once running, Swagger UI is available at:

```
http://localhost:8081/swagger-ui.html
```

Click **Authorize**, paste your JWT, and test any protected endpoint.

---

## Testing

Unit tests cover the service layer using JUnit 5 and Mockito, with AssertJ assertions.

```bash
mvn test
```

Tests follow the Arrange-Act-Assert pattern and mock all repository dependencies so they run without a database.

---

## API Overview

| Area | Base Path |
|------|-----------|
| Authentication | `/api/auth` |
| Users | `/api/users` |
| SME Businesses | `/api/businesses` |
| Loan Applications | `/api/applications` |
| Financial Analysis | `/api/financial` |
| Collateral | `/api/collateral` |
| Facilities & Drawdowns | `/api/facilities`, `/api/drawdowns` |
| Covenants & Monitoring | `/api/covenants` |
| Notifications | `/api/notifications` |

Full request/response schemas are documented in Swagger UI.

---

## Project Structure

```
src/main/java/com/bizkredit/
├── config/        Security, JWT, OpenAPI configuration
├── controller/    REST controllers
├── dto/           Request/response records
├── entity/        JPA entities
├── enums/         Domain enumerations
├── exception/     Global exception handling
├── repository/    Spring Data JPA repositories
└── service/       Business logic

src/test/java/com/bizkredit/
└── *ServiceTest   Unit tests for each service
```

---

## Branch Strategy

- `main` — stable releases
- `develop` — integration branch
- `feature/*` — per-module feature branches

Feature branches merge into `develop` via pull requests; `develop` merges into `main` at milestones.

---

## Team

| Member | Modules |
|--------|---------|
| Harish (Lead) | IAM, JWT Security |
| Dileep | SME Business, Loan Application |
| Subhishka | Financial Analysis & Underwriting |
| Affrina | Collateral, Facility Disbursement |
| Harshat | Covenant Monitoring, Notifications |
