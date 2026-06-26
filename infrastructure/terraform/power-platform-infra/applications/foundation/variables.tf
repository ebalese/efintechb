# Variables for Foundation Applications Module

variable "environment_name" {
  description = "Name of the Power Platform environment to deploy applications to"
  type        = string
}

variable "solutions" {
  description = "Map of Power Platform solutions to deploy"
  type = map(object({
    name    = string
    path    = string
    version = string
  }))
  default = {}
}

variable "custom_connectors" {
  description = "Map of custom connectors to create"
  type = map(object({
    display_name = string
    description  = string
    path         = string
  }))
  default = {}
}

variable "power_apps" {
  description = "Map of Power Apps to deploy"
  type = map(object({
    display_name = string
    app_type     = string
    path         = string
  }))
  default = {}
}
