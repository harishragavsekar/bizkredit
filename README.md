# BizKredit — SME Business Loan & Working Capital Platform

BizKredit is a REST API-based backend platform designed for commercial banks, NBFCs, and fintech lenders to manage end-to-end SME loan origination, credit underwriting, collateral evaluation, and portfolio monitoring.

Built as part of the GenC IDE Java FSE - React (Stage 2 Plus) Program — Interim Evaluation Submission (Sprint 1 & Sprint 2).

---

## Team

| Member | Role | Module |
|---|---|---|
| Harish | Team Lead & Backend Developer | Identity & Access Management |
| Dileep | Backend Developer | SME Business Profile Management |
| Subhishka | Backend Developer | Loan Application & Document Management |
| Affrina | Backend Developer | Financial Analysis & Credit Underwriting |
| Harshat | Backend Developer | Collateral Management & Notifications |

---

## Modules Implemented

**Identity & Access Management** — Harish
User registration, role-based access control across 6 actor types, and complete audit logging.

**SME Business Profile Management** — Dileep
Business entity registration, KYC tracking, promoter management, and group company linkage.

**Loan Application & Document Management** — Subhishka
Loan application submission and tracking across 5 product types, document checklist management, verification workflow, and analyst assignment.

**Financial Analysis & Credit Underwriting** — Affrina
Multi-year financial statement entry with auto-computation of Current Ratio, Debt-Equity Ratio, and DSCR. Credit proposal creation with scorecard rating and underwriting decision workflow.

**Collateral Management & Notifications** — Harshat
Collateral asset registration, valuation tracking, revaluation cycle management, and in-app notification system.

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
| API Documentation | SpringDoc OpenAPI (Swagger UI) |

---

## API Summary

| Module | Base URL | Endpoints |
|---|---|---|
| IAM | /api/users | 5 |
| SME Business | /api/businesses | 8 |
| Loan Application | /api/applications | 9 |
| Financial Analysis | /api/financial | 9 |
| Collateral | /api/collateral | 7 |

Full API documentation available at `http://localhost:8080/swagger-ui.html`

---

## Unit Tests

| Module | Test Class | Tests |
|---|---|---|
| IAM | UserServiceTest | 4 |
| SME Business | SMEBusinessServiceTest | 6 |
| Loan Application | LoanApplicationServiceTest | 7 |
| Financial Analysis | FinancialAnalysisServiceTest | 7 |
| Collateral | CollateralServiceTest | 6 |

---

## Git Workflow

- `main` — stable base branch
- `develop` — integration branch, all features merged here via Pull Requests
- `feature/*` — individual feature branch per module

---

## Agile Practice

Sprint-based development following the DevLed model. Daily stand-ups, sprint planning, and retrospectives conducted throughout. Task tracking managed on Jira. All code reviewed via GitHub Pull Requests before merging to develop.

---

## Pending — Post Interim (Sprint 3 to 5)

- Facility Disbursement & Working Capital Management
- Covenant & Portfolio Monitoring
- Microservices architecture with Spring Cloud
- React frontend
- Docker containerization
- AWS deployment
- SonarQube code quality integration