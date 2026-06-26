# Production Environment Variables

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
