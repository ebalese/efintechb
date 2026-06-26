# Foundation Applications Module
# This module manages initial solutions and other foundational resources for Power Platform

terraform {
  required_providers {
    powerplatform = {
      source  = "microsoft/power-platform"
      version = "~> 1.0"
    }
  }
}

# Deploy Power Platform solutions
resource "powerplatform_solution" "solutions" {
  for_each = var.solutions

  environment_name = var.environment_name
  solution_name    = each.value.name
  solution_path    = each.value.path
  version          = each.value.version
}

# Create custom connectors if specified
resource "powerplatform_connector" "connectors" {
  for_each = var.custom_connectors

  environment_name = var.environment_name
  display_name     = each.value.display_name
  description      = each.value.description
  connector_path   = each.value.path
}

# Create Power Apps if specified
resource "powerplatform_app" "apps" {
  for_each = var.power_apps

  environment_name = var.environment_name
  display_name     = each.value.display_name
  app_type         = each.value.app_type
  app_path         = each.value.path
}
