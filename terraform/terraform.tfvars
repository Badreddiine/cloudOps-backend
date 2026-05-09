aws_region   = "eu-west-1"
environment  = "staging"
project_name = "cloudops"

vpc_cidr              = "10.0.0.0/16"
public_subnet_cidrs   = ["10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"]
private_subnet_cidrs  = ["10.0.11.0/24", "10.0.12.0/24", "10.0.13.0/24"]
database_subnet_cidrs = ["10.0.21.0/24", "10.0.22.0/24", "10.0.23.0/24"]
availability_zones    = ["eu-west-1a", "eu-west-1b", "eu-west-1c"]

rds_instance_class        = "db.t3.medium"
rds_allocated_storage     = 20
rds_max_allocated_storage = 100
rds_incidents_db_name     = "incidents_db"
rds_keycloak_db_name      = "keycloak_db"
rds_master_username       = "cloudops_admin"