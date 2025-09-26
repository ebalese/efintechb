# Infrastructure

This directory contains Kubernetes and Helm assets for deploying lbsite.

## Layout
- `helm/`
  - `lbsite/` — Umbrella Helm chart that deploys both services and PostgreSQL
  - `statisticsapi/` — Service chart (published to OCI)
  - `deviceregapi/` — Service chart (published to OCI)
- `kubernetes/argocd/`
  - `lbsite-tst-app.yaml` — Argo CD Application for TST
  - `lbsite-prd-app.yaml` — Argo CD Application for PRD

## Helm (local rendering)
From `infrastructure/helm/lbsite/`:

```bash
# Fetch subchart dependencies (OCI + Bitnami)
helm dependency build

# Render TST
helm template lbsite-tst . \
  --namespace lbsite-tst \
  --values values/tst.yaml

# Render PRD
helm template lbsite-prd . \
  --namespace lbsite-prd \
  --values values/prd.yaml
```

## Argo CD
Argo CD Applications point at `infrastructure/helm/lbsite/` and use env-specific value files. Sync from the Argo CD UI, or rely on auto-sync.

## Secrets bootstrap
Services read DB credentials via `secretKeyRef`. Create the Secrets once per namespace (see root `README.md` for commands). Do a rollout restart after creating/updating Secrets so pods pick up the values.

## Notes
- Service charts are versioned and published to Docker Hub OCI (`balese`). Update the umbrella `Chart.yaml` dependency versions when you publish new chart versions.
- For quick local testing, you can vendor packaged service charts under `helm/lbsite/charts/`.

## Branch protection
The `main` branch is protected. Pushes to `main` are blocked; changes land via pull requests with required checks (CI and compliance) before merge.
