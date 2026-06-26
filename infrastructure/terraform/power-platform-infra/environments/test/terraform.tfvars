# Test Environment Variables

display_name      = "lbsite-test"
location          = "eastus"
environment_type  = "Sandbox"
enable_dataverse  = true
language_code     = 1033
currency_code     = "USD"
purpose           = "Test environment for lbsite"
create_access_group = true

tags = {
  Environment = "Test"
  ManagedBy   = "Terraform"
  Project     = "lbsite"
}
