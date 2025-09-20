# DeviceRegistrationAPI

Service responsible for registering devices for users.

## Overview
This service exposes a single endpoint to persist a device registration event into PostgreSQL. It validates the incoming `deviceType` (allowed: `IOS`, `ANDROID`, `WATCH`, `TV`) and stores the record in table `device_registration`.

## Endpoints
- `POST /Device/register`
  - Input JSON:
    ```json
    {
      "userKey": "string",
      "deviceType": "IOS|ANDROID|WATCH|TV"
    }
    ```
  - Success response (HTTP 200):
    ```json
    { "statusCode": 200 }
    ```
  - Error response (HTTP 400):
    ```json
    { "statusCode": 400 }
    ```

## Validation & Behavior
- `deviceType` is validated against the enum `IOS`, `ANDROID`, `WATCH`, `TV` (case-insensitive). It is normalized to uppercase before persistence.
- The payload is validated with Bean Validation annotations (non-blank, size limits).

## Persistence
- Table: `device_registration`
  - Columns:
    - `id` BIGSERIAL PK
    - `user_key` VARCHAR(120) NOT NULL
    - `device_type` VARCHAR(32) NOT NULL (uppercased)
    - `created_at` TIMESTAMPTZ NOT NULL
- JPA `ddl-auto: update` is enabled by default to manage the schema automatically in non-production setups.

## Configuration
Configuration is provided via Spring Boot `application.yml` with environment variable overrides:

- `server.port` (default `8081`)
- `DB_URL` (default `jdbc:postgresql://localhost:5432/lbsite`)
- `DB_USERNAME` (default `postgres`)
- `DB_PASSWORD` (default `postgres`)

## Run locally (Maven)
1. Ensure PostgreSQL is running and accessible.
2. From this directory, run:
   ```bash
   mvn spring-boot:run
   ```

## Run with Docker
Build image:
```bash
docker build -t device-registration-api:local .
```

Run container (Windows host Postgres example):
```bash
docker run --rm \
  -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/lbsite \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  device-registration-api:local
```

## Examples
Register a device:
```bash
curl -i -X POST http://localhost:8081/Device/register \
  -H "Content-Type: application/json" \
  -d '{"userKey":"user-123","deviceType":"iOS"}'
```

Expected (HTTP 200):
```json
{ "statusCode": 200 }
```

Invalid device type example (HTTP 400):
```json
{ "statusCode": 400 }
```

