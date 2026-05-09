output "kafka_bootstrap_brokers"     { value = aws_msk_cluster.main.bootstrap_brokers }
output "kafka_bootstrap_brokers_tls" { value = aws_msk_cluster.main.bootstrap_brokers_tls }
output "kafka_cluster_arn"           { value = aws_msk_cluster.main.arn }
output "redis_primary_endpoint"      { value = aws_elasticache_replication_group.main.primary_endpoint_address }
output "redis_port"                  { value = 6379 }