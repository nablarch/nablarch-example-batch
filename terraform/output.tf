output "performance_bastion_instance_id" {
  value = aws_instance.performance_batch_bastion_instance.id
}

output "performance_ap_instance_id" {
  value = aws_instance.performance_batch_ap_instance.id
}

output "rds_endpoint" {
  value = aws_db_instance.performance_batch_db_instance.endpoint
}