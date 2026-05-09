terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # Remote state S3 — a activer apres creation manuelle du bucket
  # backend "s3" {
  #   bucket         = "cloudops-terraform-state"
  #   key            = "cloudops/terraform.tfstate"
  #   region         = "eu-west-1"
  #   dynamodb_table = "cloudops-terraform-locks"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "CloudOps"
      Environment = var.environment
      ManagedBy   = "Terraform"
      Sprint      = "Sprint4"
    }
  }
}