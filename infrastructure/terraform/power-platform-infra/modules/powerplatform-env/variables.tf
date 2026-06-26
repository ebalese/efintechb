# Variables for Power Platform Environment Module

variable "display_name" {
  description = "Display name for the Power Platform environment"
  type        = string
}

variable "location" {
  description = "Azure region for the Power Platform environment"
  type        = string
}

variable "environment_type" {
  description = "Type of Power Platform environment (Production, Sandbox, Trial, etc.)"
  type        = string
  default     = "Sandbox"
}

variable "enable_dataverse" {
  description = "Whether to enable Dataverse database for the environment"
  type        = bool
  default     = true
}

variable "language_code" {
  description = "Language code for Dataverse (e.g., 1033 for English)"
  type        = number
  default     = 1033
}

variable "currency_code" {
  description = "Currency code for Dataverse (e.g., USD)"
  type        = string
  default     = "USD"
}

variable "security_group_id" {
  description = "Azure AD security group ID for Dataverse access"
  type        = string
  default     = null
}

variable "purpose" {
  description = "Purpose description for the environment"
  type        = string
  default     = "Managed by Terraform"
}

variable "enable_dynamics_365_apps" {
  description = "Whether to enable Dynamics 365 apps"
  type        = bool
  default     = false
}

variable "dynamics_365_app_names" {
  description = "List of Dynamics 365 app names to enable"
  type        = list(string)
  default     = []
}

variable "create_access_group" {
  description = "Whether to create an Azure AD group for environment access"
  type        = bool
  default     = false
}

variable "tags" {
  description = "Tags to apply to resources"
  type        = map(string)
  default     = {}
}
