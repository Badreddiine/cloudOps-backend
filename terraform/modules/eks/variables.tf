variable "project_name"          { type = string }
variable "environment"           { type = string }
variable "vpc_id"                { type = string }
variable "private_subnet_ids"    { type = list(string) }
variable "eks_cluster_sg_id"     { type = string }
variable "eks_nodes_sg_id"       { type = string }
variable "eks_cluster_role_arn"  { type = string }
variable "eks_node_role_arn"     { type = string }
variable "kubernetes_version"    { type = string; default = "1.28" }
variable "node_instance_types"   { type = list(string); default = ["t3.medium"] }
variable "node_min_size"         { type = number; default = 2 }
variable "node_max_size"         { type = number; default = 5 }
variable "node_desired_size"     { type = number; default = 2 }
variable "node_disk_size"        { type = number; default = 50 }