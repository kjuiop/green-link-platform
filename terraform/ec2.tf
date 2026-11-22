data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"] # Canonical

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*"]
  }
}

resource "aws_key_pair" "main" {
  key_name   = var.ec2_key_name
  public_key = file(var.ec2_public_key_path)
}

resource "aws_instance" "backend" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = "t3.small"
  key_name               = aws_key_pair.main.key_name
  vpc_security_group_ids = [aws_security_group.ec2.id]
  subnet_id              = tolist(data.aws_subnets.default.ids)[0]
  iam_instance_profile   = aws_iam_instance_profile.ec2_s3.name

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  user_data = <<-EOF
    #!/bin/bash
    apt-get update -y
    apt-get install -y ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    apt-get update -y
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ubuntu
  EOF

  tags = { Name = "${var.project_name}-backend" }

  # ami 가 most_recent 라 새 AMI 가 나오면 인스턴스가 교체(삭제)되므로 무시하고, 삭제 자체도 차단한다.
  lifecycle {
    prevent_destroy = true
    ignore_changes  = [ami, user_data]
  }
}

resource "aws_eip" "backend" {
  instance = aws_instance.backend.id
  domain   = "vpc"
  tags     = { Name = "${var.project_name}-backend-eip" }
}
