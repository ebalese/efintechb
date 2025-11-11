# efintechb Application Guide

This repo contains three services and Helm charts used to deploy to Kubernetes via Argo CD.

- Services
  - `application/lbsite/StatisticsAPI`
  - `application/lbsite/DeviceRegistrationAPI`
  - `application/lbsite/SmartAPI`
- Helm charts
  - Umbrella: `infrastructure/helm/lbsite`
  - Service subcharts (vendored/packaged under umbrella `charts/`): `statisticsapi`, `deviceregapi`, `smartapi`
- Umbrella chart: `infrastructure/helm/lbsite`
- Argo CD apps
  - TST: `infrastructure/kubernetes/argocd/lbsite-tst-app.yaml`
  - PRD: `infrastructure/kubernetes/argocd/lbsite-prd-app.yaml`

## Local Development

- Java services: JDK 17
- Python service: Python 3.12 (uvicorn/fastapi)
- Build & test (from each service directory):

```bash
mvn -B test
mvn -B -DskipTests package
```

- Run locally (example StatisticsAPI):

```bash
cd application/lbsite/StatisticsAPI
mvn spring-boot:run
```

Environment variables are defined in Helm `values/*.yaml`; mirror locally as needed (JDBC URL, downstream URLs, etc.).

## Docker

- Build images locally (examples):

```bash
# StatisticsAPI
DOCKER_IMAGE=${DOCKER_HUB_USERNAME:-balese}/statistics-api:dev
docker build -t "$DOCKER_IMAGE" application/lbsite/StatisticsAPI

# DeviceRegistrationAPI
DOCKER_IMAGE=${DOCKER_HUB_USERNAME:-balese}/device-registration-api:dev
docker build -t "$DOCKER_IMAGE" application/lbsite/DeviceRegistrationAPI

# SmartAPI
DOCKER_IMAGE=${DOCKER_HUB_USERNAME:-balese}/smartapi:dev
docker build -t "$DOCKER_IMAGE" application/lbsite/SmartAPI
```

- Run containers locally (example):

```bash
docker run --rm -p 8080:8080 ${DOCKER_HUB_USERNAME:-balese}/statistics-api:dev
```

## Environments and Deployment

- TST (immutable by digest)
  - Values file: `infrastructure/helm/lbsite/values-tst.yaml`
  - CI publishes images (includes `latest`); the TST promoter resolves digests and updates `values-tst.yaml` with `image.tag: latest@<digest>`.
  - After merging, Argo CD syncs to deploy the exact digests.

- PRD (release tags)
  - Values file: `infrastructure/helm/lbsite/values-prd.yaml`
  - A Git tag `vX.Y.Z` triggers image builds with that tag and updates `values-prd.yaml` to use `image.tag: vX.Y.Z`.
  - After merging, Argo CD syncs to deploy the tagged images.

## CI/CD Overview

- Service CI workflows
  - `/.github/workflows/statistics-api-ci.yml`
  - `/.github/workflows/device-registration-api-ci.yml`
  - `/.github/workflows/smart-api-ci-tst.yml`
  - Triggers: push to `main`, pull_request (paths-limited). Tag pushes for PRD.
  - Publishes Docker images with `latest` and SHA tags.

- Promotion to TST
  - `/.github/workflows/promote-statistics-tst.yml`
  - `/.github/workflows/promote-device-registration-tst.yml`
  - `/.github/workflows/promote-smart-api-ci-tst.yml`
  - Updates `values-tst.yaml` with `latest@<digest>` for each service.

## Release Flow (PRD)

1) Ensure changes are on `main`.
2) Create a tag and push it:

```bash
git checkout main && git pull
git tag -a v0.1.4 -m "Release v0.1.4"
git push origin v0.1.4
```

3) Verify in GitHub Actions:
   - Service CI runs and pushes images `statistics-api:v0.1.4`, `device-registration-api:v0.1.4`.
   - A PR “Promote PRD to tag:v0.1.4” appears.
4) Merge the PR; Argo CD deploys PRD.

## Troubleshooting

- Images not tagged on Docker Hub after tag push
  - Ensure service CI `on.push.tags: ['v*']` is present and there is NO `on.push.paths` filter.
  - Check Docker Hub secrets: `DOCKER_HUB_USERNAME`, `DOCKER_HUB_ACCESS_TOKEN`.

- PRD promotion PR didn’t open
  - Confirm `/.github/workflows/promote-prd.yml` is on `main`.
  - Tag format must be `vX.Y.Z`.

- TST PR didn’t open
  - Ensure the originating workflow completed successfully and the SHA-tagged images exist.

- Argo CD didn’t update
  - Confirm the promotion PR was merged and the app is healthy/synced.
  - Verify `values-*.yaml` reflects the expected tag (PRD) or digest (TST).

## Useful Paths

- Services: `application/lbsite/*`
- Helm umbrella: `infrastructure/helm/lbsite/`
- Env values: `infrastructure/helm/lbsite/values-*.yaml`
- Argo CD apps: `infrastructure/kubernetes/argocd/`
- Workflows: `/.github/workflows/`

---
This README is a quick ops/dev guide. If you need deeper details (schema, endpoints, DB), consider adding service-level READMEs under each service directory.

## Compliance checks

The repo includes a simple compliance/security scan workflow: `/.github/workflows/compliance-framework.yml`.

- What it does
  - Builds Docker images locally for each service:
    - `application/lbsite/DeviceRegistrationAPI`
    - `application/lbsite/StatisticsAPI`
  - Runs Trivy vulnerability scans on those images.
  - Uploads results as SARIF to GitHub Security.
  - Fails the job on findings (Trivy `exit-code: '1'`).

- How to run
  - Trigger manually from GitHub Actions: “Compliance Framework” (workflow_dispatch).

- Where to see results
  - Repository `Security` tab → `Code scanning alerts`.
  - Open the workflow run → download/view the SARIF artifacts:
    - `trivy-results-device-registration-api.sarif`
    - `trivy-results-statistics-api.sarif`

- Interpreting failures
  - The job fails if vulnerabilities are detected. Review alerts in the Security tab.
  - To remediate:
    - Update base images (e.g., `FROM eclipse-temurin:17-jre` to a patched version).
    - Upgrade dependencies in Maven `pom.xml`.
    - Rebuild and re-run the scan.

- Mandatory in CI
  - The compliance scan is treated as a mandatory gate in CI. Pull requests must have a passing “Compliance Framework” check before they can be merged to `main`.
  - Enforce this via repository Branch protection rules by marking the “Compliance Framework” workflow as a required status check.
pwd