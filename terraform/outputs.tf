# ── VPC ───────────────────────────────────────────────────────────────────────
output "vpc_id" {
  description = "ID du VPC CloudOps"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "IDs des subnets publics"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs des subnets prives (microservices)"
  value       = module.vpc.private_subnet_ids
}

output "database_subnet_ids" {
  description = "IDs des subnets BDD"
  value       = module.vpc.database_subnet_ids
}

# ── Security Groups ───────────────────────────────────────────────────────────
output "alb_sg_id" {
  description = "Security Group Load Balancer"
  value       = module.security_groups.alb_sg_id
}

output "eks_nodes_sg_id" {
  description = "Security Group noeuds EKS"
  value       = module.security_groups.eks_nodes_sg_id
}

output "rds_sg_id" {
  description = "Security Group RDS"
  value       = module.security_groups.rds_sg_id
}

# ── IAM ───────────────────────────────────────────────────────────────────────
output "eks_node_role_arn" {
  description = "ARN IAM Role noeuds EKS"
  value       = module.iam.eks_node_role_arn
}

output "eks_cluster_role_arn" {
  description = "ARN IAM Role cluster EKS"
  value       = module.iam.eks_cluster_role_arn
}

# ── RDS ───────────────────────────────────────────────────────────────────────
output "rds_incidents_endpoint" {
  description = "Endpoint RDS incidents"
  value       = module.rds.incidents_endpoint
  sensitive   = true
}

output "rds_keycloak_endpoint" {
  description = "Endpoint RDS Keycloak"
  value       = module.rds.keycloak_endpoint
  sensitive   = true
}

output "rds_incidents_secret_arn" {
  description = "ARN Secret Manager — credentials incidents DB"
  value       = module.rds.incidents_secret_arn
}

output "rds_keycloak_secret_arn" {
  description = "ARN Secret Manager — credentials keycloak DB"
  value       = module.rds.keycloak_secret_arn
}