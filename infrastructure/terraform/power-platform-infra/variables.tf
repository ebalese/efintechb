# Root Shared Variables

variable "location" {
  description = "Default Azure region for resources"
  type        = string
  default     = "eastus"
}

variable "tags" {
  description = "Default tags to apply to all resources"
  type        = map(string)
  default = {
    ManagedBy = "Terraform"
    Project   = "lbsite"
  }
}
