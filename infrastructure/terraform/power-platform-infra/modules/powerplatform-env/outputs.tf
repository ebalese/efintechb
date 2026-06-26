# Outputs for Power Platform Environment Module

output "environment_id" {
  description = "ID of the Power Platform environment"
  value       = powerplatform_environment.environment.id
}

output "environment_name" {
  description = "Name of the Power Platform environment"
  value       = powerplatform_environment.environment.name
}

output "environment_url" {
  description = "URL of the Power Platform environment"
  value       = powerplatform_environment.environment.linked_environment_metadata.0.instance_url
}

output "dataverse_enabled" {
  description = "Whether Dataverse is enabled"
  value       = var.enable_dataverse
}

output "access_group_id" {
  description = "ID of the Azure AD access group (if created)"
  value       = var.create_access_group ? azuread_group.environment_access[0].id : null
}

output "access_group_display_name" {
  description = "Display name of the Azure AD access group (if created)"
  value       = var.create_access_group ? azuread_group.environment_access[0].display_name : null
}
