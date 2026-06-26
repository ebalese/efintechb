# Power Platform — Infrastructure Repository

This repository manages the Power Platform infrastructure as code using Terraform. It provisions and governs all Power Platform environments, DLP policies, security roles, and supporting Azure resources. No environment is created manually through the Power Platform Admin Center.

---

## Repository Structure

```
infrastructure/terraform/power-platform-infra/
├── .github/
│   └── workflows/
│       └── provision.yml         #  Terraform deployment workflow
│
├── modules/
│   └── powerplatform-env/           # Reusable module — one complete PP environment
│       ├── main.tf                  # Resources: environment, Dataverse, DLP, roles
│       ├── variables.tf             # Input variables
│       └── outputs.tf               # Environment URL, ID, Dataverse URL
│
├── environments/                    # One folder per environment — calls the module
│   ├── dev/
│   │   ├── main.tf                  # Module call with dev-specific inputs
│   │   ├── backend.tf               # Dev remote state configuration
│   │   └── terraform.tfvars         # Dev variable values
│   ├── test/
│   │   ├── main.tf
│   │   ├── backend.tf
│   │   └── terraform.tfvars
│   ├── uat/
│   │   ├── main.tf
│   │   ├── backend.tf
│   │   └── terraform.tfvars
│   └── prod/
│       ├── main.tf
│       ├── backend.tf
│       └── terraform.tfvars
│
├── applications/
│   └── foundation/                  # Foundation solution environment bindings
│       ├── main.tf                  # Connection references, service connections
│       └── variables.tf
│
├── provider.tf                      # Provider configuration — powerplatform + azurerm
├── versions.tf                      # Provider version pins
├── variables.tf                     # Root variable declarations
└── terraform.tfvars.example         # Template — copy to each environment tfvars
```

---

## Key Concepts

### Module-per-concern, instance-per-environment

The `modules/powerplatform-env` module is called **once per environment** with different inputs. All environment-specific values live in `terraform.tfvars` — the module itself has no environment awareness.

```
environments/dev/main.tf    →  calls module with dev values
environments/test/main.tf   →  calls same module with test values
environments/uat/main.tf    →  calls same module with uat values
environments/prod/main.tf   →  calls same module with prod values
```

### Separate state per environment

Each environment folder has its own `backend.tf` pointing to a separate state file in Azure Blob Storage. This is intentional — destroying or corrupting dev state cannot affect prod.

```
pp-terraform-state/
├── dev.tfstate
├── test.tfstate
├── uat.tfstate
└── prod.tfstate
```

### Applications folder

The `applications/foundation/` folder provisions the bindings that allow the Foundation app solution to operate — specifically connection reference credentials and Azure DevOps service connections consumed by the application pipeline. It is deployed after environments are provisioned.

---

## Environments

| Environment | Type | DLP Tier | Managed Env | Solution Type |
|---|---|---|---|---|
| dev | Sandbox | Relaxed | No | Unmanaged |
| test | Sandbox | Standard | Yes | Managed |
| uat | Sandbox | Standard | Yes | Managed |
| prod | Production | Strict | Yes | Managed |

### DLP tiers

- **Relaxed (dev)** — approved connectors plus additional connectors for testing
- **Standard (test/uat)** — approved connectors only, mirrors prod restrictions
- **Strict (prod)** — approved connectors only, all others blocked, no exceptions

### Managed environments

Test, UAT, and Prod are provisioned as Managed Environments. This enforces solution checker on import and prevents makers from editing solutions directly. Only the pipeline service principal can import solutions to these environments.

---

## Prerequisites

- Terraform >= 1.9
- Azure CLI authenticated (`az login`)
- Power Platform service principal with Power Platform Admin role
- Access to Azure Blob Storage for remote state
- GitHub Secrets configured for Azure authentication (see Pipeline section below)

---

## Configuration

### 1. Copy the example tfvars

```bash
cp terraform.tfvars.example environments/dev/terraform.tfvars
```

Edit the copy with dev-specific values. Repeat for each environment.

### 2. Configure provider credentials

Credentials are never stored in tfvars. Set them as environment variables before running Terraform:

```bash
export ARM_CLIENT_ID="<service-principal-client-id>"
export ARM_CLIENT_SECRET="<service-principal-secret>"
export ARM_TENANT_ID="<tenant-id>"
export ARM_SUBSCRIPTION_ID="<subscription-id>"

export POWER_PLATFORM_CLIENT_ID="<pp-service-principal-client-id>"
export POWER_PLATFORM_CLIENT_SECRET="<pp-service-principal-secret>"
export POWER_PLATFORM_TENANT_ID="<tenant-id>"
```

In the pipeline these are sourced from GitHub Actions secrets or Azure DevOps variable groups — never hardcoded.

### 3. Backend configuration

Each environment `backend.tf` points to the correct state file:

```hcl
# environments/dev/backend.tf
terraform {
  backend "azurerm" {
    resource_group_name  = "rg-pp-terraform-state"
    storage_account_name = "ppterraformstate"
    container_name       = "tfstate"
    key                  = "dev.tfstate"
  }
}
```

---

## Usage

### Manual Terraform (break-glass / local development only)

For local development or break-glass scenarios, you can run Terraform manually. Always `cd` into the environment folder before running Terraform. Never run from the repo root.

```bash
cd environments/dev

terraform init
terraform plan -var-file="terraform.tfvars"
terraform apply -var-file="terraform.tfvars"
```

### Apply all environments in order (manual)

Environments must be applied in dependency order:

```bash
# 1. Dev first
cd environments/dev && terraform apply -var-file="terraform.tfvars" && cd ../..

# 2. Test
cd environments/test && terraform apply -var-file="terraform.tfvars" && cd ../..

# 3. UAT
cd environments/uat && terraform apply -var-file="terraform.tfvars" && cd ../..

# 4. Prod last
cd environments/prod && terraform apply -var-file="terraform.tfvars" && cd ../..

# 5. Application bindings after all environments are up
cd applications/foundation && terraform apply -var-file="terraform.tfvars" && cd ../..
```

### Apply via pipeline (standard)

All infrastructure changes should go through the pipeline. Manual apply is for break-glass scenarios only and requires a change ticket for UAT and Prod.

### Destroy an environment (non-prod only)

```bash
# Dev and Test only — never run destroy against UAT or Prod
cd environments/dev
terraform destroy -var-file="terraform.tfvars"
```

Prod and UAT destruction requires explicit platform team approval and a change ticket. The pipeline blocks `terraform destroy` on these environments.

---

## Pipeline

Infrastructure deployment is managed via the GitHub Actions workflow in `.github/workflows/provision.yml`.

### Workflow: provision.yml

Triggers:
- **Manual only**: Workflow dispatch with target_environment and action parameters

Actions:
1. Sets up Terraform 1.9.x
2. Runs `terraform init` with Azure backend
3. Runs `terraform validate`
4. Runs `terraform plan` or `terraform apply` based on action parameter
5. Uses GitHub Environments (infra-dev, infra-test, infra-uat, infra-prod) for approval gates

```bash
# Manual deployment trigger
# Go to Actions → Deploy Infrastructure (Terraform) → Run workflow
# Specify target environment (dev/test/uat/prod) and action (plan/apply)
```

### Required GitHub Secrets

The following secrets must be configured in the GitHub repository:

- `AZURE_CLIENT_ID` - Azure service principal client ID
- `AZURE_CLIENT_SECRET` - Azure service principal client secret
- `AZURE_TENANT_ID` - Azure AD tenant ID
- `AZURE_SUBSCRIPTION_ID` - Azure subscription ID

### Environment Gates

GitHub Environments are configured with approval requirements:

| Environment | Approval Required | Approvers |
|---|---|---|
| infra-dev | No | None |
| infra-test | Yes | Platform Team Lead |
| infra-uat | Yes | Platform Team Lead |
| infra-prod | Yes | Change Advisory Board (CAB) |

---

## Adding a New Environment

1. Create a new folder under `environments/`
2. Copy an existing environment as a template:
   ```bash
   cp -r environments/test environments/staging
   ```
3. Update `backend.tf` with the new state file key
4. Update `terraform.tfvars` with environment-specific values
5. Create a corresponding GitHub Environment (e.g., infra-staging) with appropriate approvers
6. Open a PR for review
7. After merge, trigger the workflow manually to plan and apply the new environment

---

## Module Reference — powerplatform-env

| Variable | Description | Example |
|---|---|---|
| `environment_name` | Display name in PP Admin Center | `"Foundation - Dev"` |
| `environment_type` | Sandbox or Production | `"Sandbox"` |
| `location` | Azure region | `"europe"` |
| `language_code` | Dataverse language | `"1033"` |
| `currency_code` | Dataverse currency | `"CHF"` |
| `security_group_id` | AAD group for access | `"<group-object-id>"` |
| `dlp_tier` | DLP policy tier | `"relaxed"` / `"standard"` / `"strict"` |
| `managed_environment` | Enable Managed Env features | `true` / `false` |

| Output | Description |
|---|---|
| `environment_url` | Power Platform environment URL |
| `environment_id` | Environment unique ID |
| `dataverse_url` | Dataverse OData endpoint URL |

---

## Important Rules

**Never provision environments manually through the Admin Center.** All environments must be created and modified via this repository. Manual changes will be detected by drift detection and flagged.

**Never commit secrets or credentials.** Provider credentials go in environment variables. Connection reference credentials go in Azure Key Vault. If a secret is accidentally committed, rotate it immediately and notify the security team.

**Never run `terraform destroy` on UAT or Prod** without an approved change ticket and platform team sign-off.

**Always plan before apply.** Review the plan output carefully before approving. Terraform changes to Power Platform environments can affect all solutions deployed to them.

---

## Platform Team

This repository is owned and maintained by the Platform Engineering team. Application teams consume the outputs (environment URLs, service connections) published by this pipeline — they do not modify this repository.

For access requests, new environment provisioning, DLP policy changes, or gateway issues, raise a request with the platform team.