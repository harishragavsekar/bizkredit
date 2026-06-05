# BizKredit — SME Business Loan & Working Capital Platform

BizKredit is a REST API-based backend platform for commercial banks, NBFCs, and fintech lenders to manage SME loan origination, credit underwriting, collateral evaluation, and portfolio monitoring.

Built as part of the GenC IDE Java FSE - React (Stage 2 Plus) Program.

---

## Team

| Member | Module |
|---|---|
| Harish (Lead) | 4.1 Identity & Access Management |
| Dileep | 4.2 SME Business Profile + 4.3 Loan Application |
| Subhishka | 4.4 Financial Analysis & Credit Underwriting |
| Affrina | 4.5 Collateral Management + 4.6 Facility Disbursement |
| Harshat | 4.7 Covenant & Portfolio Monitoring + 4.8 Notifications |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Build Tool | Maven |
| Utilities | Lombok, SLF4J |
| Testing | JUnit 5 + Mockito |
| API Docs | Swagger UI (SpringDoc OpenAPI) |

---

## Local Setup

1. Clone the repo and switch to develop:
```bash
git clone https://github.com/harishragavsekar/bizkredit.git
cd bizkredit
git switch develop
```

2. Update `src/main/resources/application.properties` with your MySQL credentials.

3. Run Maven build in STS:
```
Right-click project -> Run As -> Maven build... -> Goals: clean install -DskipTests
```

4. Run the app:
```
Right-click BizKreditApplication.java -> Run As -> Spring Boot App
```

5. Access Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

---

## Git Workflow

- `main` — stable base
- `develop` — integration branch, all features merged here via PRs
- `feature/*` — one branch per module

---

## Status

Work in progress — Interim Evaluation (Sprint 1 + Sprint 2)
