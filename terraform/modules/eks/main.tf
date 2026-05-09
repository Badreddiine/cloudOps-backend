module "eks" {
  source = "./modules/eks"

  project_name         = var.project_name
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  private_subnet_ids   = module.vpc.private_subnet_ids
  eks_cluster_sg_id    = module.security_groups.eks_cluster_sg_id
  eks_nodes_sg_id      = module.security_groups.eks_nodes_sg_id
  eks_cluster_role_arn = module.iam.eks_cluster_role_arn
  eks_node_role_arn    = module.iam.eks_node_role_arn        # ← corrigé
  kubernetes_version   = var.eks_kubernetes_version
  node_instance_types  = var.eks_node_instance_types
  node_min_size        = var.eks_node_min_size
  node_max_size        = var.eks_node_max_size
  node_desired_size    = var.eks_node_desired_size
}