# ── ALB ───────────────────────────────────────────────────────────────────────
resource "aws_security_group" "alb" {
  name        = "${var.project_name}-${var.environment}-alb-sg"
  description = "SG ALB - trafic HTTPS entrant depuis internet"
  vpc_id      = var.vpc_id
  tags        = { Name = "${var.project_name}-${var.environment}-alb-sg" }
}

resource "aws_security_group_rule" "alb_ingress_https" {
  type              = "ingress"
  description       = "HTTPS depuis internet"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.alb.id
}

resource "aws_security_group_rule" "alb_ingress_http" {
  type              = "ingress"
  description       = "HTTP redirection vers HTTPS"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.alb.id
}

resource "aws_security_group_rule" "alb_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = [var.vpc_cidr]
  security_group_id = aws_security_group.alb.id
}

# ── EKS Cluster Control Plane ─────────────────────────────────────────────────
resource "aws_security_group" "eks_cluster" {
  name        = "${var.project_name}-${var.environment}-eks-cluster-sg"
  description = "SG EKS Control Plane"
  vpc_id      = var.vpc_id
  tags        = { Name = "${var.project_name}-${var.environment}-eks-cluster-sg" }
}

resource "aws_security_group_rule" "eks_cluster_ingress_from_nodes" {
  type                     = "ingress"
  description              = "API Server depuis nodes"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_cluster.id
  source_security_group_id = aws_security_group.eks_nodes.id
}

resource "aws_security_group_rule" "eks_cluster_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.eks_cluster.id
}

# ── EKS Nodes ─────────────────────────────────────────────────────────────────
resource "aws_security_group" "eks_nodes" {
  name        = "${var.project_name}-${var.environment}-eks-nodes-sg"
  description = "SG noeuds EKS - microservices CloudOps"
  vpc_id      = var.vpc_id
  tags        = { Name = "${var.project_name}-${var.environment}-eks-nodes-sg" }
}

resource "aws_security_group_rule" "eks_nodes_ingress_self" {
  type              = "ingress"
  description       = "Communication inter-nodes"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  self              = true
  security_group_id = aws_security_group.eks_nodes.id
}

resource "aws_security_group_rule" "eks_nodes_ingress_alb_services" {
  type                     = "ingress"
  description              = "Trafic depuis ALB"
  from_port                = 8080
  to_port                  = 8084
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_nodes.id
  source_security_group_id = aws_security_group.alb.id
}

resource "aws_security_group_rule" "eks_nodes_ingress_alb_keycloak" {
  type                     = "ingress"
  description              = "Keycloak depuis ALB"
  from_port                = 8180
  to_port                  = 8180
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_nodes.id
  source_security_group_id = aws_security_group.alb.id
}

resource "aws_security_group_rule" "eks_nodes_ingress_from_cluster" {
  type                     = "ingress"
  description              = "EKS API Server vers nodes"
  from_port                = 1025
  to_port                  = 65535
  protocol                 = "tcp"
  security_group_id        = aws_security_group.eks_nodes.id
  source_security_group_id = aws_security_group.eks_cluster.id
}

resource "aws_security_group_rule" "eks_nodes_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.eks_nodes.id
}

# ── RDS PostgreSQL ────────────────────────────────────────────────────────────
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "SG RDS PostgreSQL - acces depuis EKS nodes uniquement"
  vpc_id      = var.vpc_id
  tags        = { Name = "${var.project_name}-${var.environment}-rds-sg" }
}

resource "aws_security_group_rule" "rds_ingress_from_nodes" {
  type                     = "ingress"
  description              = "PostgreSQL depuis EKS nodes"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.rds.id
  source_security_group_id = aws_security_group.eks_nodes.id
}

resource "aws_security_group_rule" "rds_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = [var.vpc_cidr]
  security_group_id = aws_security_group.rds.id
}

# ── Redis ElastiCache ─────────────────────────────────────────────────────────
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-${var.environment}-redis-sg"
  description = "SG Redis ElastiCache - acces depuis EKS nodes"
  vpc_id      = var.vpc_id
  tags        = { Name = "${var.project_name}-${var.environment}-redis-sg" }
}

resource "aws_security_group_rule" "redis_ingress_from_nodes" {
  type                     = "ingress"
  description              = "Redis depuis EKS nodes"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = aws_security_group.redis.id
  source_security_group_id = aws_security_group.eks_nodes.id
}

resource "aws_security_group_rule" "redis_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = [var.vpc_cidr]
  security_group_id = aws_security_group.redis.id
}

# ── Kafka MSK ─────────────────────────────────────────────────────────────────
resource "aws_security_group" "kafka" {
  name        = "${var.project_name}-${var.environment}-kafka-sg"
  description = "SG MSK Kafka - acces depuis EKS nodes"
  vpc_id      = var.vpc_id
  tags        = { Name = "${var.project_name}-${var.environment}-kafka-sg" }
}

resource "aws_security_group_rule" "kafka_ingress_broker" {
  type                     = "ingress"
  description              = "Kafka broker depuis EKS"
  from_port                = 9092
  to_port                  = 9092
  protocol                 = "tcp"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.eks_nodes.id
}

resource "aws_security_group_rule" "kafka_ingress_tls" {
  type                     = "ingress"
  description              = "Kafka TLS depuis EKS"
  from_port                = 9094
  to_port                  = 9094
  protocol                 = "tcp"
  security_group_id        = aws_security_group.kafka.id
  source_security_group_id = aws_security_group.eks_nodes.id
}

resource "aws_security_group_rule" "kafka_egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = [var.vpc_cidr]
  security_group_id = aws_security_group.kafka.id
}