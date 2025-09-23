# efintechb
Monorepo for lbsite services and infrastructure.

## Stack
- **APIs**: `DeviceRegistrationAPI`, `StatisticsAPI` (Spring Boot 3, Java 17)
- **Database**: PostgreSQL (Bitnami Helm chart in Kubernetes; Docker image locally)
- **Packaging**: Spring Boot fat JARs via `spring-boot-maven-plugin` (`repackage`)
- **Containers**: Eclipse Temurin JRE 17 base images
- **Orchestration**:
  - Local: `docker-compose` in `application/lbsite/`
  - Kubernetes: Umbrella Helm chart `infrastructure/helm/lbsite/` (installs APIs + PostgreSQL in the same namespace)

## Local development (with PostgreSQL and debug)
From `application/lbsite/`:

```bash
docker compose up --build
```

This starts:
- `statisticsapi`: http://localhost:8082 (debug on 5005)
- `deviceregistrationapi`: http://localhost:8081 (debug on 5006)
- `postgres`: localhost:5432 (default database and credentials are defined in `application/lbsite/docker-compose.yml`)

Endpoints:
- StatisticsAPI
  - `GET /` -> service status
  - `GET /Log/auth/statistics?deviceType=IOS`
  - `POST /Log/auth`
- DeviceRegistrationAPI
  - `GET /` -> service status
  - `POST /Device/register`

## Building fat JARs
```bash
mvn clean package spring-boot:repackage -DskipTests
```
Executable jars will be in `target/`:
- `statistics-api-0.0.1-SNAPSHOT.jar`
- `device-registration-api-0.0.1-SNAPSHOT.jar`

## Kubernetes deployment (umbrella chart)
Umbrella chart path: `infrastructure/helm/lbsite/`

- The chart declares dependencies:
  - `statisticsapi` and `deviceregapi` subcharts
  - `postgresql` (Bitnami) as a dependency
- The PostgreSQL chart is installed into the same namespace as the release, e.g. `lbsite-tst`.
- Service connection (tst):
  - JDBC URL: `jdbc:postgresql://lbsite-tst-postgresql:5432/lbsite`
  - Credentials are configured via Helm values (do not commit real secrets).

Common commands:
```bash
# From infrastructure/helm/lbsite/
helm dependency update

# Install/upgrade TST
helm upgrade --install lbsite-tst . -n lbsite-tst -f values/tst.yaml

# Install/upgrade PRD
helm upgrade --install lbsite-prd . -n lbsite-prd -f values/prd.yaml
```

## Image tags and pulling in Kubernetes
- Use unique image tags and set `image.pullPolicy: Always` (configured in umbrella `values.yaml`) to ensure nodes pull updated images.

## Notes
- To debug in Kubernetes, port-forward the pod/deployment debug port (5005) and attach a Remote JVM Debugger.
- For production, enable persistence for PostgreSQL (PVCs) and manage credentials securely outside this README.
