output "incidents_endpoint" {
  value     = aws_db_instance.incidents.address
  sensitive = true
}

output "keycloak_endpoint" {
  value     = aws_db_instance.keycloak.address
  sensitive = true
}

output "incidents_secret_arn" {
  value = aws_secretsmanager_secret.incidents_db.arn
}

output "keycloak_secret_arn" {
  value = aws_secretsmanager_secret.keycloak_db.arn
}