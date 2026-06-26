# Terraform Provider Configuration

terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "~> 2.0"
    }
    powerplatform = {
      source  = "microsoft/power-platform"
      version = "~> 1.0"
    }
  }
}

provider "azurerm" {
  features {}
}

provider "azuread" {
  # Configuration will be loaded from environment variables or managed identity
}

provider "powerplatform" {
  # Configuration will be loaded from environment variables or managed identity
}
