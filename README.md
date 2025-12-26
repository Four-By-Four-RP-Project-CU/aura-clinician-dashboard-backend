# AURA Delivery & Explainability Clinician Backend

Spring Boot service for a plugin-based clinician dashboard delivery layer. Designed for Chronic Urticaria (CU) now, with a disease-module architecture for future extensions.

## Requirements
- Java 17
- Maven 3.9+

## Run
```bash
mvn spring-boot:run
```
Service runs on `http://localhost:8081`.

## Authentication
HTTP Basic auth with role-based access.
- `clinician` / `clinician123` (ROLE_CLINICIAN)

## Endpoints
- `GET /api/v1/cases`
- `GET /api/v1/dashboard/{caseId}?diseaseType=CU`
- `POST /api/v1/feedback` (clinician review action)
- `GET /api/v1/audit/{caseId}`
