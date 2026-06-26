# Production Environment Configuration

module "powerplatform_env" {
  source = "../../../modules/powerplatform-env"

  display_name      = "lbsite-prod"
  location          = "eastus"
  environment_type  = "Production"
  enable_dataverse  = true
  language_code     = 1033
  currency_code     = "USD"
  purpose           = "Production environment for lbsite"
  create_access_group = true

  tags = {
    Environment = "Production"
    ManagedBy   = "Terraform"
    Project     = "lbsite"
  }
}
