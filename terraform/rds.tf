resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = data.aws_subnets.default.ids
  tags       = { Name = "${var.project_name}-db-subnet-group" }
}

resource "aws_db_instance" "mysql" {
  identifier        = "${var.project_name}-mysql"
  engine            = "mysql"
  engine_version    = "8.4"
  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp2"

  db_name  = "greenlink"
  username = "admin"
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  publicly_accessible         = true
  allow_major_version_upgrade = true
  multi_az                    = false

  backup_retention_period = 7
  backup_window           = "18:00-19:00"

  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.project_name}-mysql-final"
  deletion_protection       = true

  tags = { Name = "${var.project_name}-mysql" }
}
