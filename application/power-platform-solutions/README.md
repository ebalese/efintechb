# Power Platform — Bank Application Repository

This repository contains the source code for the Bank Power Platform solutions, managed using the modern PAC CLI YAML source control format. Components live at the root level and are referenced by solution manifests.

---

## Repository Structure

```
.
├── CanvasApps/                          # Power Apps canvas applications
│   ├── BankCore_MainApp/
│   ├── BankCustomerPortal_App/
│   └── BankInternalDashboard/
│
├── Flows/                               # Power Automate cloud flows
│   ├── BankCore_ApprovalFlow/
│   ├── BankCore_NotificationFlow/
│   ├── BankCustomerPortal_OnboardingFlow/
│   └── BankShared_SendEmailFlow/
│
├── ConnectionReferences/                # Connector bindings (no credentials)
├── EnvironmentVariables/                # Variable schemas (no values)
├── CopilotStudio/                       # Copilot Studio agents
├── PowerPages/                          # Power Pages portals
│   └── BankCustomerPortal_Site/
├── entities/                            # Dataverse table definitions
│
├── solutions/                           # Solution manifests — references only
│   ├── BankShared/                      # Shared utilities — deploy first
│   ├── BankCore/                        # Core banking — deploy second
│   └── BankCustomerPortal/              # Customer portal — deploy third
│
├── config/                              # Deployment settings per environment
│   ├── dev/deployment-settings.json
│   ├── test/deployment-settings.json
│   ├── uat/deployment-settings.json
│   └── prod/deployment-settings.json
│
├── artifacts/                           # Build output — not committed to git
└── scripts/
    └── deploy-solution.ps1              # Manual deployment helper
```

---

## Key Concepts

### Component-first structure

All components (Canvas Apps, Flows, Entities, etc.) live at the **root level**, not inside solution folders. Solution folders under `solutions/` contain **manifests only** — they declare which root-level components belong to each solution.

This mirrors how a Helm `Chart.yaml` references templates without physically containing them.

```
solutions/BankCore/solutioncomponents.yml   ← lists which components belong here
CanvasApps/BankCore_MainApp/                ← actual component source lives here
Flows/BankCore_ApprovalFlow/                ← actual component source lives here
```

### Naming convention

All components are prefixed with their owning solution:

| Prefix | Solution |
|---|---|
| `BankShared_` | BankShared — shared utilities |
| `BankCore_` | BankCore — core banking logic |
| `BankCustomerPortal_` | BankCustomerPortal — customer-facing |

---

## Solutions

### Dependency order

Solutions must be deployed in the following order. Each solution depends on the one above it.

```
BankShared  →  BankCore  →  BankCustomerPortal
```

Never deploy out of order. If `BankShared` deployment fails, stop — do not proceed to `BankCore`.

### BankShared

Shared utilities consumed by all other solutions. Contains shared flows, connection references, and environment variable schemas.

- `BankShared_SendEmailFlow` — centralised email notification flow
- Connection references for SharePoint and Dataverse
- Shared environment variable definitions

### BankCore

Core banking logic. Depends on `BankShared`.

- `BankCore_MainApp` — primary internal canvas app
- `BankInternalDashboard` — internal reporting canvas app
- `BankCore_ApprovalFlow` — approval chain automation
- `BankCore_NotificationFlow` — notification routing

### BankCustomerPortal

Customer-facing layer. Depends on `BankCore`.

- `BankCustomerPortal_App` — customer-facing canvas app
- `BankCustomerPortal_OnboardingFlow` — client onboarding automation
- `BankCustomerPortal_Site` — Power Pages portal

---

## Environment Configuration

Each environment has its own deployment settings file under `config/`. These files define environment variable **values** and connection reference **bindings**. They contain no secrets.

```
config/
├── dev/deployment-settings.json      # Dev values — relaxed thresholds, dev endpoints
├── test/deployment-settings.json     # Test values — test endpoints, QA email group
├── uat/deployment-settings.json      # UAT values — near-production config
└── prod/deployment-settings.json     # Prod values — live endpoints, ops email group
```

### What changes per environment

| Setting | Dev | Test | UAT | Prod |
|---|---|---|---|---|
| API base URL | dev endpoint | test endpoint | uat endpoint | prod endpoint |
| Approval threshold | CHF 1,000 | CHF 5,000 | CHF 50,000 | CHF 50,000 |
| Notification email | dev team | QA team | business team | operations team |
| Feature flags | all enabled | all enabled | selected | stable only |
| Solution type | Unmanaged | Managed | Managed | Managed |

### What never goes in config files

- API keys or passwords → Azure Key Vault only
- Connection credentials → injected by pipeline via connection reference binding
- Client secrets → pipeline variables / Key Vault references

---

## Deployment

### Prerequisites

- [PAC CLI](https://aka.ms/PowerAppsCLI) installed
- Artifactory credentials configured in GitHub Secrets
- Power Platform service principal with appropriate permissions
- GitHub Environments configured for dev/test/uat/prod with approval gates

### Required GitHub Secrets

The following secrets must be configured in the GitHub repository:

#### Artifactory Secrets
- `ARTIFACTORY_URL` - Base URL of your Artifactory instance
- `ARTIFACTORY_USER` - Username for Artifactory authentication
- `ARTIFACTORY_PASS` - Password/token for Artifactory authentication
- `ARTIFACTORY_REPO` - Repository name in Artifactory

#### Power Platform Secrets
- `POWERPLATFORM_CLIENT_ID` - Service principal client ID
- `POWERPLATFORM_CLIENT_SECRET` - Service principal client secret
- `POWERPLATFORM_TENANT_ID` - Azure AD tenant ID
- `POWERPLATFORM_ENV_URL_dev` - Dev environment URL
- `POWERPLATFORM_ENV_URL_test` - Test environment URL
- `POWERPLATFORM_ENV_URL_uat` - UAT environment URL
- `POWERPLATFORM_ENV_URL_prod` - Prod environment URL

### Pipeline deployment (standard)

All deployments go through the pipeline. Direct manual imports to Test, UAT, or Prod are not permitted.

```
Developer builds in browser (Dev env)
              ↓
Trigger build.yml (manual or push to main)
              ↓
pac solution pack → Managed + Unmanaged zips
              ↓
Push to Artifactory
              ↓
Trigger deploy.yml (manual)
              ↓
Download from Artifactory
              ↓
pac solution import → Target Environment
```

### Workflows

#### build.yml

Triggers:
- **Automatic**: On push to `main` branch when solution files change
- **Manual**: Workflow dispatch with version parameter

Actions:
1. Packs all three solutions (BankShared, BankCore, BankCustomerPortal) as both managed and unmanaged
2. Uploads artifacts to GitHub Actions (retained 7 days for debugging)
3. Pushes all zips to Artifactory

```bash
# Manual build trigger
# Go to Actions → Build Power Platform Solutions → Run workflow
# Specify version (e.g., 1.0.0)
```

#### deploy.yml

Triggers:
- **Manual only**: Workflow dispatch with version and target_environment parameters

Actions:
1. Downloads specified version from Artifactory
2. Authenticates to target Power Platform environment
3. Deploys solutions in dependency order (BankShared → BankCore → BankCustomerPortal)
4. Uses GitHub Environments for approval gates on test/uat/prod

```bash
# Manual deployment trigger
# Go to Actions → Deploy Solutions to Environment → Run workflow
# Specify version (e.g., 1.0.0) and target environment (test/uat/prod)
```

### Environment Gates

GitHub Environments are configured with approval requirements:

| Environment | Approval Required | Approvers |
|---|---|---|
| dev | No | None |
| test | Yes | QA Team Lead |
| uat | Yes | Business Stakeholder |
| prod | Yes | Change Advisory Board (CAB) |

### Manual deployment (break-glass / dev only)

Use the helper script for manual dev environment deployments only. Never use this for Test, UAT, or Prod.

```powershell
# Deploy all three solutions to dev in correct order
.\scripts\deploy-solution.ps1 `
  -Environment dev `
  -SettingsFile config/dev/deployment-settings.json

# Deploy a single solution
.\scripts\deploy-solution.ps1 `
  -Environment dev `
  -SolutionName BankCore `
  -SettingsFile config/dev/deployment-settings.json
```

### Rollback

Rollback is performed by deploying a previous version from Artifactory via the deploy workflow. Never manually re-import a zip to Prod without CAB approval.

```bash
# Trigger rollback via deploy workflow
# Go to Actions → Deploy Solutions to Environment → Run workflow
# Specify previous version (e.g., 1.0.0) and target environment (prod)
# CAB approval will be required for prod rollback
```

---

## Local Development

### Pack a solution locally

```bash
# Authenticate
pac auth create --url https://yourdev.crm.dynamics.com

# Pack BankCore from source
pac solution pack \
  --folder ./solutions/BankCore \
  --zipfile ./artifacts/BankCore_local.zip \
  --packagetype Unmanaged

# Import to personal dev environment
pac solution import \
  --path ./artifacts/BankCore_local.zip \
  --settings-file ./config/dev/deployment-settings.json
```

### Export changes back to source

After making changes in the browser, export back to keep source in sync:

```bash
pac solution clone \
  --name BankCore \
  --outputDirectory ./solutions/BankCore \
  --processCanvasApps
```

Review the diff before committing. GUID noise in flow JSON is expected — focus on logic changes.

---

## Branch Strategy

```
main          →  source of truth — triggers build.yml on push
feature/*     →  developer branches — no pipeline trigger
```

PRs must target `main`. At least one reviewer required. Build runs automatically on merge to main.

Deployment is manual via workflow dispatch:
1. Build runs automatically on push to main
2. Deploy to dev/test/uat/prod via manual workflow dispatch with appropriate approvals

---

## Adding a New Component

1. Create the component in your personal Dev environment
2. Export using `pac solution clone` or `pac solution unpack`
3. Confirm the component appears in the correct root folder (`CanvasApps/`, `Flows/`, etc.)
4. Add the component reference to `solutions/<SolutionName>/solutioncomponents.yml`
5. Follow the naming convention — prefix with `BankCore_`, `BankShared_`, or `BankCustomerPortal_`
6. Open a PR — pipeline linter runs automatically

---

## Platform Team

Infrastructure (environments, DLP policies, connection reference IDs, service connections) is managed separately in the platform repository:

```
power-platform-infra/    # Platform team repo — Terraform + Ansible
```

For new environment requests, gateway issues, DLP policy changes, or connection reference provisioning, raise a request with the platform team. Do not attempt to modify infrastructure from this repository.

---

## Contact

| Role | Responsibility |
|---|---|
| Platform Team | Environments, DLP, connections, pipelines |
| App Team Lead | Solution architecture, naming conventions, deploy order |
| Change Manager | CAB approvals for Prod deployments |
