# ── MSK Kafka ─────────────────────────────────────────────────────────────────
resource "aws_msk_configuration" "main" {
  name              = "${var.project_name}-${var.environment}-kafka-config"
  kafka_versions    = [var.kafka_version]
  description       = "Configuration MSK Kafka CloudOps"

  server_properties = <<-EOF
    auto.create.topics.enable=true
    default.replication.factor=2
    min.insync.replicas=1
    num.partitions=3
    log.retention.hours=168
    log.retention.bytes=1073741824
  EOF
}

resource "aws_msk_cluster" "main" {
  cluster_name           = "${var.project_name}-${var.environment}-kafka"
  kafka_version          = var.kafka_version
  number_of_broker_nodes = var.kafka_broker_count

  broker_node_group_info {
    instance_type   = var.kafka_instance_type
    client_subnets  = slice(var.private_subnet_ids, 0, var.kafka_broker_count)
    security_groups = [var.kafka_sg_id]

    storage_info {
      ebs_storage_info {
        volume_size = 100
      }
    }
  }

  configuration_info {
    arn      = aws_msk_configuration.main.arn
    revision = aws_msk_configuration.main.latest_revision
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS_PLAINTEXT"
      in_cluster    = true
    }
  }

  client_authentication {
    unauthenticated = true
  }

  logging_info {
    broker_logs {
      cloudwatch_logs {
        enabled   = true
        log_group = aws_cloudwatch_log_group.kafka.name
      }
    }
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-kafka"
  }
}

resource "aws_cloudwatch_log_group" "kafka" {
  name              = "/aws/msk/${var.project_name}-${var.environment}/broker"
  retention_in_days = 30
  tags              = { Name = "${var.project_name}-${var.environment}-kafka-logs" }
}

# ── Secrets Manager — bootstrap servers Kafka ─────────────────────────────────
resource "aws_secretsmanager_secret" "kafka" {
  name                    = "${var.project_name}/${var.environment}/kafka/bootstrap-servers"
  description             = "Bootstrap servers MSK Kafka"
  recovery_window_in_days = 7
  tags                    = { Name = "${var.project_name}-${var.environment}-kafka-secret" }
}

resource "aws_secretsmanager_secret_version" "kafka" {
  secret_id = aws_secretsmanager_secret.kafka.id
  secret_string = jsonencode({
    bootstrap_servers_plaintext = aws_msk_cluster.main.bootstrap_brokers
    bootstrap_servers_tls       = aws_msk_cluster.main.bootstrap_brokers_tls
  })
}

# ── ElastiCache Redis — Subnet Group ──────────────────────────────────────────
resource "aws_elasticache_subnet_group" "main" {
  name        = "${var.project_name}-${var.environment}-redis-subnet-group"
  subnet_ids  = var.private_subnet_ids
  description = "Subnet group Redis CloudOps"
  tags        = { Name = "${var.project_name}-${var.environment}-redis-subnet-group" }
}

# ── ElastiCache Redis — Replication Group (HA) ────────────────────────────────
resource "aws_elasticache_replication_group" "main" {
  replication_group_id = "${var.project_name}-${var.environment}-redis"
  description          = "Redis ElastiCache CloudOps — cache + rate limiting"

  node_type               = var.redis_node_type
  port                    = 6379
  parameter_group_name    = "default.redis7"
  engine_version          = "7.0"
  automatic_failover_enabled = var.redis_num_replicas > 0 ? true : false
  num_cache_clusters      = var.redis_num_replicas + 1

  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [var.redis_sg_id]

  at_rest_encryption_enabled = true
  transit_encryption_enabled = false  # microservices internes uniquement

  log_delivery_configuration {
    destination      = aws_cloudwatch_log_group.redis.name
    destination_type = "cloudwatch-logs"
    log_format       = "text"
    log_type         = "slow-log"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-redis"
  }
}

resource "aws_cloudwatch_log_group" "redis" {
  name              = "/aws/elasticache/${var.project_name}-${var.environment}/redis"
  retention_in_days = 14
  tags              = { Name = "${var.project_name}-${var.environment}-redis-logs" }
}

# ── Secrets Manager — endpoint Redis ──────────────────────────────────────────
resource "aws_secretsmanager_secret" "redis" {
  name                    = "${var.project_name}/${var.environment}/redis/endpoint"
  description             = "Endpoint Redis ElastiCache"
  recovery_window_in_days = 7
  tags                    = { Name = "${var.project_name}-${var.environment}-redis-secret" }
}

resource "aws_secretsmanager_secret_version" "redis" {
  secret_id = aws_secretsmanager_secret.redis.id
  secret_string = jsonencode({
    host = aws_elasticache_replication_group.main.primary_endpoint_address
    port = 6379
  })
}