# ── Tache 4.01 : VPC ──────────────────────────────────────────────────────────
module "vpc" {
  source = "./modules/vpc"

  project_name          = var.project_name
  environment           = var.environment
  vpc_cidr              = var.vpc_cidr
  public_subnet_cidrs   = var.public_subnet_cidrs
  private_subnet_cidrs  = var.private_subnet_cidrs
  database_subnet_cidrs = var.database_subnet_cidrs
  availability_zones    = var.availability_zones
}

# ── Tache 4.01 : Security Groups ─────────────────────────────────────────────
module "security_groups" {
  source = "./modules/security-groups"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.vpc.vpc_id
  vpc_cidr     = var.vpc_cidr
}

# ── Tache 4.01 : IAM Roles ───────────────────────────────────────────────────
module "iam" {
  source = "./modules/iam"

  project_name = var.project_name
  environment  = var.environment
}

# ── Tache 4.02 : RDS PostgreSQL Multi-AZ ────────────────────────────────────
module "rds" {
  source = "./modules/rds"

  project_name              = var.project_name
  environment               = var.environment
  instance_class            = var.rds_instance_class
  allocated_storage         = var.rds_allocated_storage
  max_allocated_storage     = var.rds_max_allocated_storage
  incidents_db_name         = var.rds_incidents_db_name
  keycloak_db_name          = var.rds_keycloak_db_name
  master_username           = var.rds_master_username
  database_subnet_ids       = module.vpc.database_subnet_ids
  rds_security_group_id     = module.security_groups.rds_sg_id
  db_subnet_group_name      = module.vpc.db_subnet_group_name
}
# ── Tache 4.04 : MSK Kafka + ElastiCache Redis ────────────────────────────────
module "msk_redis" {
  source = "./modules/msk-redis"

  project_name       = var.project_name
  environment        = var.environment
  vpc_id             = module.vpc.vpc_id
  private_subnet_ids = module.vpc.private_subnet_ids
  kafka_sg_id        = module.security_groups.kafka_sg_id
  redis_sg_id        = module.security_groups.redis_sg_id
}