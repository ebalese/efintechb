# Terraform Backend Configuration for UAT

terraform {
  backend "azurerm" {
    resource_group_name  = "terraform-state-rg"
    storage_account_name = "tfstatelbsite"
    container_name       = "terraform-state"
    key                  = "power-platform-uat.tfstate"
  }
}
