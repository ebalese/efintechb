# StatisticsAPI

Service responsible for logging auth events and returning device registration statistics.

## Overview
This service exposes two endpoints:

1) `POST /Log/auth` — receives `{ userKey, deviceType }`, performs basic validation/normalization, calls DeviceRegistrationAPI `/Device/register`, and returns `{ statusCode, message }` where `message` is `success` on 200, otherwise `bad_request`.

2) `GET /Log/auth/statistics?deviceType=...` — validates the device type and queries the PostgreSQL database for the number of registered devices of that type. Returns `{ deviceType, count }` and uses `-1` for invalid input.

Allowed device types: `IOS`, `ANDROID`, `WATCH`, `TV` (case-insensitive).

## Endpoints
- `POST /Log/auth`
  - Input JSON:
    ```json
    {
      "userKey": "string",
      "deviceType": "IOS|ANDROID|WATCH|TV"
    }
    ```
  - Success (HTTP 200):
    ```json
    { "statusCode": 200, "message": "success" }
    ```
  - Error (HTTP 400):
    ```json
    { "statusCode": 400, "message": "bad_request" }
    ```

- `GET /Log/auth/statistics?deviceType=IOS`
  - Success (HTTP 200):
    ```json
    { "deviceType": "IOS", "count": 42 }
    ```
  - Invalid input (HTTP 400):
    ```json
    { "deviceType": "IOS", "count": -1 }
    ```

## Configuration
Configuration is provided via Spring Boot `application.yml` with environment variable overrides:

- `server.port` (default `8082`)
- `DB_URL` (default `jdbc:postgresql://localhost:5432/lbsite`)
- `DB_USERNAME` (default `postgres`)
- `DB_PASSWORD` (default `postgres`)
- `DEVICE_REGISTRATION_URL` (default `http://localhost:8081`)

## Run locally (Maven)
1. Ensure DeviceRegistrationAPI is running (default `http://localhost:8081`).
2. Ensure PostgreSQL is running and accessible.
3. From this directory, run:
   ```bash
   mvn spring-boot:run
   ```

## Run with Docker
Build image:
```bash
docker build -t statistics-api:local .
```

Run container (Windows host Postgres example):
```bash
docker run --rm \
  -p 8082:8082 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/lbsite \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e DEVICE_REGISTRATION_URL=http://host.docker.internal:8081 \
  statistics-api:local
```

## Examples
Log auth event (forwards to DeviceRegistrationAPI):
```bash
curl -i -X POST http://localhost:8082/Log/auth \
  -H "Content-Type: application/json" \
  -d '{"userKey":"user-123","deviceType":"iOS"}'
```

Expected (HTTP 200):
```json
{ "statusCode": 200, "message": "success" }
```

Get statistics:
```bash
curl -s "http://localhost:8082/Log/auth/statistics?deviceType=iOS"
```

Sample:
```json
{ "deviceType": "iOS", "count": 1 }
```

