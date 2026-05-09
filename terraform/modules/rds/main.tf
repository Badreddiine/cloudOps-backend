# ── Mot de passe genere aleatoirement ────────────────────────────────────────
resource "random_password" "incidents_db" {
  length           = 24
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "random_password" "keycloak_db" {
  length           = 24
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# ── AWS Secrets Manager — credentials BDD ────────────────────────────────────
resource "aws_secretsmanager_secret" "incidents_db" {
  name                    = "${var.project_name}/${var.environment}/rds/incidents-db"
  description             = "Credentials RDS incidents_db"
  recovery_window_in_days = 7

  tags = { Name = "${var.project_name}-${var.environment}-incidents-db-secret" }
}

resource "aws_secretsmanager_secret_version" "incidents_db" {
  secret_id = aws_secretsmanager_secret.incidents_db.id
  secret_string = jsonencode({
    username = var.master_username
    password = random_password.incidents_db.result
    dbname   = var.incidents_db_name
    host     = aws_db_instance.incidents.address
    port     = 5432
    url      = "jdbc:postgresql://${aws_db_instance.incidents.address}:5432/${var.incidents_db_name}"
  })
}

resource "aws_secretsmanager_secret" "keycloak_db" {
  name                    = "${var.project_name}/${var.environment}/rds/keycloak-db"
  description             = "Credentials RDS keycloak_db"
  recovery_window_in_days = 7

  tags = { Name = "${var.project_name}-${var.environment}-keycloak-db-secret" }
}

resource "aws_secretsmanager_secret_version" "keycloak_db" {
  secret_id = aws_secretsmanager_secret.keycloak_db.id
  secret_string = jsonencode({
    username = var.master_username
    password = random_password.keycloak_db.result
    dbname   = var.keycloak_db_name
    host     = aws_db_instance.keycloak.address
    port     = 5432
    url      = "jdbc:postgresql://${aws_db_instance.keycloak.address}:5432/${var.keycloak_db_name}"
  })
}

# ── Parameter Group PostgreSQL optimise ───────────────────────────────────────
resource "aws_db_parameter_group" "postgres" {
  name        = "${var.project_name}-${var.environment}-pg15"
  family      = "postgres15"
  description = "Parametres PostgreSQL optimises pour CloudOps"

  parameter {
    name  = "log_connections"
    value = "1"
  }

  parameter {
    name  = "log_disconnections"
    value = "1"
  }

  parameter {
    name  = "log_min_duration_statement"
    value = "1000"   # Log requetes > 1 seconde
  }

  parameter {
    name  = "shared_preload_libraries"
    value = "pg_stat_statements"
  }

  tags = { Name = "${var.project_name}-${var.environment}-pg-params" }
}

# ── RDS incidents_db — Multi-AZ ──────────────────────────────────────────────
resource "aws_db_instance" "incidents" {
  identifier = "${var.project_name}-${var.environment}-incidents-db"

  # Moteur
  engine               = "postgres"
  engine_version       = "15.5"
  instance_class       = var.instance_class
  parameter_group_name = aws_db_parameter_group.postgres.name

  # Stockage
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true

  # BDD
  db_name  = var.incidents_db_name
  username = var.master_username
  password = random_password.incidents_db.result

  # Reseau
  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = [var.rds_security_group_id]
  publicly_accessible    = false

  # Haute disponibilite
  multi_az = true

  # Sauvegardes
  backup_retention_period   = 7
  backup_window             = "03:00-04:00"
  maintenance_window        = "Mon:04:00-Mon:05:00"
  delete_automated_backups  = false

  # Protection suppression
  deletion_protection       = var.environment == "prod" ? true : false
  skip_final_snapshot       = var.environment == "prod" ? false : true
  final_snapshot_identifier = var.environment == "prod" ? "${var.project_name}-${var.environment}-incidents-final" : null

  # Monitoring
  monitoring_interval          = 60
  monitoring_role_arn          = aws_iam_role.rds_monitoring.arn
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  performance_insights_enabled = true
  performance_insights_retention_period = 7

  tags = {
    Name    = "${var.project_name}-${var.environment}-incidents-db"
    Service = "incident-service"
  }
}

# ── RDS keycloak_db — Multi-AZ ───────────────────────────────────────────────
resource "aws_db_instance" "keycloak" {
  identifier = "${var.project_name}-${var.environment}-keycloak-db"

  engine               = "postgres"
  engine_version       = "15.5"
  instance_class       = var.instance_class
  parameter_group_name = aws_db_parameter_group.postgres.name

  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true

  db_name  = var.keycloak_db_name
  username = var.master_username
  password = random_password.keycloak_db.result

  db_subnet_group_name   = var.db_subnet_group_name
  vpc_security_group_ids = [var.rds_security_group_id]
  publicly_accessible    = false

  multi_az = true

  backup_retention_period  = 7
  backup_window            = "02:00-03:00"
  maintenance_window       = "Mon:03:00-Mon:04:00"
  delete_automated_backups = false

  deletion_protection       = var.environment == "prod" ? true : false
  skip_final_snapshot       = var.environment == "prod" ? false : true
  final_snapshot_identifier = var.environment == "prod" ? "${var.project_name}-${var.environment}-keycloak-final" : null

  monitoring_interval          = 60
  monitoring_role_arn          = aws_iam_role.rds_monitoring.arn
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]
  performance_insights_enabled = true
  performance_insights_retention_period = 7

  tags = {
    Name    = "${var.project_name}-${var.environment}-keycloak-db"
    Service = "keycloak"
  }
}

# ── IAM Role RDS Enhanced Monitoring ─────────────────────────────────────────
resource "aws_iam_role" "rds_monitoring" {
  name = "${var.project_name}-${var.environment}-rds-monitoring-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "monitoring.rds.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "rds_monitoring" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
  role       = aws_iam_role.rds_monitoring.name
}