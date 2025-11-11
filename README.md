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
- **CD**: Argo CD (GitOps)


## Folder structure
- `application/`
  - `lbsite/DeviceRegistrationAPI` (Java)
  - `lbsite/StatisticsAPI` (Java)
  - `lbsite/SmartAPI` (Python/FastAPI)
- `infrastructure/`
  - `helm/lbsite/` umbrella chart
    - `values-tst.yaml`, `values-prd.yaml`
    - `charts/` (service subcharts: `deviceregapi`, `statisticsapi`, `smartapi`)
  - `kubernetes/argocd/` Argo CD Applications (tst/prd)
- `.github/workflows/` CI/CD

## CI/CD
- Service CI:
  - Java services: `device-registration-api-ci.yml`, `statistics-api-ci.yml`
  - Python service: `smart-api-ci-tst.yml` (tests + build/push)
- Promotion to TST (GitOps via digest):
  - `promote-device-registration-tst.yml`
  - `promote-statistics-tst.yml`
  - `promote-smart-api-ci-tst.yml`
- Each promotion workflow updates `infrastructure/helm/lbsite/values-tst.yaml` with `image.tag: latest@<digest>` and pushes to `main`, which Argo CD syncs.

## Helm values
- TST: `infrastructure/helm/lbsite/values-tst.yaml`
- PRD: `infrastructure/helm/lbsite/values-prd.yaml`

## Argo CD
- TST app: `infrastructure/kubernetes/argocd/lbsite-tst-app.yaml`
- PRD app: `infrastructure/kubernetes/argocd/lbsite-prd-app.yaml`
Executable jars will be in `target/`:
- `statistics-api-0.0.1-SNAPSHOT.jar`
- `device-registration-api-0.0.1-SNAPSHOT.jar`

## Kubernetes deployment (umbrella chart)
Umbrella chart path: `infrastructure/helm/lbsite/`

- The chart declares dependencies:
  - `statisticsapi`, `deviceregapi`, and `smartapi` subcharts
  - `postgresql` (Bitnami) as a dependency
- The PostgreSQL chart is installed into the same namespace as the release, e.g. `lbsite-tst`.
- Service connection (tst):
  - JDBC URL: `jdbc:postgresql://lbsite-tst-postgresql:5432/lbsite`
  - Credentials are provided via Kubernetes Secrets (referenced by the charts); avoid committing real secrets to Git.

## Image tags and pulling in Kubernetes
- Use unique image tags and set `image.pullPolicy: Always` (configured in umbrella `values.yaml`) to ensure nodes pull updated images.
- Service CI publishes the following tags per build:
  - `latest`
  - `sha-<shortsha>`
  - `sha-<40charsha>`
  - `<40charsha>`
  - `vX.Y.Z` on tag pushes (release tags)


## CI/CD Overview

This repository uses GitHub Actions for CI and Argo CD for CD.

### CI (build, test, images)

- **Triggers**
  - Per-service CI runs on pull requests and pushes (e.g., to `main`).
  - On tag push (`v*`), images are built and pushed with the tag value to Docker Hub.

- **Compliance checks (mandatory)**
  - Trivy container vulnerability scans run via `/.github/workflows/compliance-framework.yml`.
  - Results are uploaded as SARIF to the Security tab (Code scanning alerts).
  - Make this a required status check in Branch protection so PRs must pass before merge.

- **Build & Image tagging**
  - Build JARs via Maven (`spring-boot-maven-plugin`).
  - Build Docker images with Buildx and push to Docker Hub.
  - Tags used on branch pushes: `latest`, `sha-<shortsha>`, `sha-<40charsha>`, `<40charsha>`.
  - Tags used on tag pushes: `vX.Y.Z` (plus the above sha variants also present for the commit).

### CD (Argo CD)

- **Applications**
  - `lbsite-tst` and `lbsite-prd` Argo CD `Application` manifests in `infrastructure/kubernetes/argocd/` target the umbrella chart at `infrastructure/helm/lbsite/`.
  - Each environment specifies its value file via `helm.valueFiles`.

- **Automatic sync**
  - Both TST and PRD apps use automated sync with prune + self-heal. Once the Helm values in `main` change, Argo CD reconciles and deploys the new configuration.

### Environment Promotion

- **TST (digest pinned)**
  - Workflows (per service):
    - `.github/workflows/promote-device-registration-tst.yml`
    - `.github/workflows/promote-statistics-tst.yml`
    - `.github/workflows/promote-smart-api-ci-tst.yml`
  - Each resolves the Docker image digest for `latest` and updates `infrastructure/helm/lbsite/values-tst.yaml` by setting `image.tag: latest@<digest>` for the respective service.
  - The change is pushed to `main` (no PR), and Argo CD syncs to deploy the pinned digest.

- **PRD (tag-based)**
  - Workflow: `.github/workflows/promote-prd.yml` (trigger: push of a tag matching `v*`).
  - Updates `infrastructure/helm/lbsite/values-prd.yaml` to set services' `image.tag` to `${{ github.ref_name }}` (e.g., `v1.2.0`).
  - Opens a PR; merging it triggers Argo CD to deploy those release tags.

### Secrets management

- Application containers read DB credentials from environment variables `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`, which are now sourced from Kubernetes Secrets via `valueFrom.secretKeyRef` in the service charts.
- Configuration:
  - `values-tst.yaml` and `values-prd.yaml` specify a Secret name per service (e.g., `statisticsapi.secret.name: lbsite-tst-db`).
  - DB URL (`SPRING_DATASOURCE_URL`) and non-sensitive configs remain in `values-*.yaml`.
- Bootstrap (one-time per environment):
  - TST:
    ```bash
    kubectl -n lbsite-tst create secret generic lbsite-tst-db \
      --from-literal=username=<db-username> \
      --from-literal=password=<db-password>
    ```
  - PRD:
    ```bash
    kubectl -n lbsite-prd create secret generic lbsite-prd-db \
      --from-literal=username=<db-username> \
      --from-literal=password=<db-password>
    ```

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
4. The PRD promotion workflow opens a PR updating `values-prd.yaml` tags to `v0.1.1`.
5. Merge the PR; Argo CD syncs PRD and rolls out the tagged images.
