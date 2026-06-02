# BizKredit — SME Business Loan Platform

## Tech Stack
- Java 17 + Spring Boot 3.2.5
- Spring Data JPA + Hibernate
- MySQL (local dev) | H2 (tests)
- Lombok + SLF4J
- JUnit 5 + Mockito
- Swagger UI at http://localhost:8080/swagger-ui.html

---

## Local Setup

1. Install MySQL and create the database (Spring auto-creates tables):
   ```sql
   CREATE DATABASE bizkredit_db;
   ```

2. Update `src/main/resources/application.properties`:
   ```
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. Run the app:
   ```bash
   mvn spring-boot:run
   ```

4. Run tests:
   ```bash
   mvn test
   ```

---

## Git Branching Strategy (team of 5)

```
main              ← stable, Harish merges here after review
└── develop       ← integration branch, everyone merges their feature here
    ├── feature/iam                  (Harish)
    ├── feature/sme-business-profile (Dileep)
    ├── feature/loan-application     (Subhishka)
    ├── feature/financial-analysis   (Affrina)
    └── feature/collateral           (Harshat)
```

### Workflow
```bash
# Each member starts from develop
git checkout develop
git pull origin develop
git checkout -b feature/your-module

# Work, commit often
git add .
git commit -m "feat: add LoanApplication entity and repository"

# Push and raise PR to develop
git push origin feature/your-module
```

### Commit message convention
```
feat: add X       → new feature
fix: correct Y    → bug fix
test: add tests   → test only changes
refactor: clean Z → no logic change
```

---

## Package Structure
```
com.bizkredit
├── BizKreditApplication.java
├── config/          ← (future: security config)
├── controller/      ← REST controllers
├── dto/             ← request/response objects + ApiResponse wrapper
├── entity/          ← JPA entities
├── enums/           ← Role, Status enums
├── exception/       ← custom exceptions + GlobalExceptionHandler
├── repository/      ← Spring Data JPA interfaces
└── service/         ← business logic
```

---

## Module Ownership
| Module | Team member | Package suffix suggestion |
|--------|-------------|--------------------------|
| IAM (User, AuditLog) | Harish | base package |
| SME Business Profile | Dileep | (same packages, different entity/service/controller files) |
| Loan Application & Docs | Subhishka | same |
| Financial Analysis & Credit | Affrina | same |
| Collateral + Notifications | Harshat | same |
| Facility + Covenant | — | Post-interim |
