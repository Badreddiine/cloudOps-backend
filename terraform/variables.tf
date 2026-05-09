# ── Generales ────────────────────────────────────────────────────────────────
variable "aws_region" {
  description = "Region AWS de deploiement"
  type        = string
  default     = "eu-west-1"
}

variable "environment" {
  description = "Environnement : dev / staging / prod"
  type        = string
  default     = "staging"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "L'environnement doit etre dev, staging ou prod."
  }
}

variable "project_name" {
  description = "Nom du projet"
  type        = string
  default     = "cloudops"
}

# ── VPC ───────────────────────────────────────────────────────────────────────
variable "vpc_cidr" {
  description = "CIDR block du VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDRs des subnets publics (1 par AZ)"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDRs des subnets prives (microservices)"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
}

variable "database_subnet_cidrs" {
  description = "CIDRs des subnets BDD (RDS isole)"
  type        = list(string)
  default     = ["10.0.21.0/24", "10.0.22.0/24", "10.0.23.0/24"]
}

variable "availability_zones" {
  description = "AZs a utiliser"
  type        = list(string)
  default     = ["eu-west-1a", "eu-west-1b", "eu-west-1c"]
}

# ── RDS ───────────────────────────────────────────────────────────────────────
variable "rds_instance_class" {
  description = "Type d'instance RDS"
  type        = string
  default     = "db.t3.medium"
}

variable "rds_allocated_storage" {
  description = "Stockage RDS en GB"
  type        = number
  default     = 20
}

variable "rds_max_allocated_storage" {
  description = "Stockage RDS max autoscaling en GB"
  type        = number
  default     = 100
}

variable "rds_incidents_db_name" {
  description = "Nom de la BDD incidents"
  type        = string
  default     = "incidents_db"
}

variable "rds_keycloak_db_name" {
  description = "Nom de la BDD Keycloak"
  type        = string
  default     = "keycloak_db"
}

variable "rds_master_username" {
  description = "Username maitre RDS"
  type        = string
  default     = "cloudops_admin"
  sensitive   = true
}