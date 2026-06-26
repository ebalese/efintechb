# UAT Environment Variables

display_name      = "lbsite-uat"
location          = "eastus"
environment_type  = "Sandbox"
enable_dataverse  = true
language_code     = 1033
currency_code     = "USD"
purpose           = "User Acceptance Testing environment for lbsite"
create_access_group = true

tags = {
  Environment = "UAT"
  ManagedBy   = "Terraform"
  Project     = "lbsite"
}
