# Power Platform Environment Module
# This module creates a Power Platform environment with associated resources

terraform {
  required_providers {
    powerplatform = {
      source  = "microsoft/power-platform"
      version = "~> 1.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

# Create Power Platform environment
resource "powerplatform_environment" "environment" {
  display_name = var.display_name
  location     = var.location
  environment_type = var.environment_type
  
  dataverse = var.enable_dataverse ? {
    language_code     = var.language_code
    currency_code     = var.currency_code
    security_group_id = var.security_group_id
  } : null
}

# Create Azure AD group for environment access (if specified)
resource "azuread_group" "environment_access" {
  count = var.create_access_group ? 1 : 0

  display_name     = "${var.display_name} Access Group"
  description      = "Azure AD group for accessing ${var.display_name} Power Platform environment"
  security_enabled = true
  mail_enabled     = false
  owners           = [data.azuread_client_config.current.object_id]
}

# Data source for current client config
data "azuread_client_config" "current" {}
