variable "project_name"         { type = string }
variable "environment"          { type = string }
variable "vpc_id"               { type = string }
variable "private_subnet_ids"   { type = list(string) }
variable "kafka_sg_id"          { type = string }
variable "redis_sg_id"          { type = string }
variable "kafka_instance_type"  { type = string; default = "kafka.t3.small" }
variable "kafka_version"        { type = string; default = "3.6.0" }
variable "kafka_broker_count"   { type = number; default = 2 }
variable "redis_node_type"      { type = string; default = "cache.t3.micro" }
variable "redis_num_replicas"   { type = number; default = 1 }