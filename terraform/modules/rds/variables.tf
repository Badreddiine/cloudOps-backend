variable "project_name" {
  type = string
}

variable "environment" {
  type = string
}

variable "instance_class" {
  type = string
}

variable "allocated_storage" {
  type = number
}

variable "max_allocated_storage" {
  type = number
}

variable "incidents_db_name" {
  type = string
}

variable "keycloak_db_name" {
  type = string
}

variable "master_username" {
  type      = string
  sensitive = true
}

variable "database_subnet_ids" {
  type = list(string)
}

variable "rds_security_group_id" {
  type = string
}

variable "db_subnet_group_name" {
  type = string
}