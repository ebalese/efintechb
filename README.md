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

## CI/CD Overview

This repository uses GitHub Actions for CI and Argo CD for CD.

### CI (build, test, images)

- **Triggers**
  - Per-service CI runs on pull requests and pushes (e.g., to `main`).
  - On tag push (`v*`), images are built and pushed with the tag value to Docker Hub.

- **Compliance checks**
  - Maven build with tests (can be skipped with `-DskipTests` in CI where needed).
  - Basic validation (formatting/linting can be expanded in future PRs).

- **Build & Image tagging**
  - Build JARs via Maven (`spring-boot-maven-plugin`).
  - Build Docker images with Buildx and push to Docker Hub.
  - Tags used:
    - On normal CI (push to branches): `${{ github.sha }}`.
    - On release/tag push: `${{ github.ref_name }}` (e.g., `v1.2.0`).

### CD (Argo CD)

- **Applications**
  - `lbsite-tst` and `lbsite-prd` Argo CD `Application` manifests in `infrastructure/kubernetes/argocd/` target the umbrella chart at `infrastructure/helm/lbsite/`.
  - Each environment specifies its value file via `helm.valueFiles`.

- **Automatic sync**
  - Both TST and PRD apps use automated sync with prune + self-heal. Once the Helm values in `main` change, Argo CD reconciles and deploys the new configuration.

### Environment Promotion

- **TST (digest pinned)**
  - Workflow: `.github/workflows/promote-tst.yml` (trigger: a successful CI workflow for either service).
  - Resolves Docker image digests for the triggering commit SHA and writes them to `infrastructure/helm/lbsite/values/tst.yaml` as `image.digest` (removes `image.tag`).
  - Opens a PR with the digest updates; merging it triggers Argo CD to deploy immutable digests.

- **PRD (tag-based)**
  - Workflow: `.github/workflows/promote-prd.yml` (trigger: push of a tag matching `v*`).
  - Updates `infrastructure/helm/lbsite/values/prd.yaml` to set both services' `image.tag` to `${{ github.ref_name }}` (e.g., `v1.2.0`).
  - Opens a PR; merging it triggers Argo CD to deploy those release tags.

### Required Secrets (GitHub Actions)

- `DOCKER_HUB_USERNAME` — your Docker Hub username or org (e.g., `balese`).
- `DOCKER_HUB_TOKEN` — Docker Hub access token for `docker/login-action`.

### Quick Test of PRD Promotion

1. Merge changes to `main`.
2. Create and push a tag from `main` (e.g., `v0.1.1`):
   ```bash
   git tag -a v0.1.1 -m "Release v0.1.1"
   git push origin v0.1.1
   ```
3. Ensure images are built and pushed with the same tag:
   - `${DOCKER_HUB_USERNAME}/statistics-api:v0.1.1`
   - `${DOCKER_HUB_USERNAME}/device-registration-api:v0.1.1`
4. The PRD promotion workflow opens a PR updating `values/prd.yaml` tags to `v0.1.1`.
5. Merge the PR; Argo CD syncs PRD and rolls out the tagged images.
