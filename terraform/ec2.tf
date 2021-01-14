locals {
  init_script_common = <<-SHELL_SCRIPT
    # Locale の設定
    localectl set-locale LANG=ja_JP.UTF-8
    # Timezone の設定
    timedatectl set-timezone Asia/Tokyo
    # 変数宣言
    USER_HOME=/home/ec2-user
    SHELL_SCRIPT

  init_script_deploy_ssh_key = <<-SHELL_SCRIPT
    echo '${file("./ssh-key/performance")}' > $${USER_HOME}/.ssh/id_rsa
    chown ec2-user:ec2-user $${USER_HOME}/.ssh/id_rsa
    chmod 600 $${USER_HOME}/.ssh/id_rsa
    SHELL_SCRIPT

  init_script_install_jdk = <<-SHELL_SCRIPT
    # Download JDK
    cd $${USER_HOME}
    wget https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk${var.jdk_version}-b01/OpenJDK8U-jdk_x64_linux_hotspot_${var.jdk_version}b01.tar.gz

    # 解凍
    tar -xvzf OpenJDK8U-jdk_x64_linux_hotspot_${var.jdk_version}b01.tar.gz
    chown -R ec2-user:ec2-user jdk${var.jdk_version}-b01

    # 環境変数設定
    echo 'export JAVA_HOME=$HOME/jdk${var.jdk_version}-b01' >> $${USER_HOME}/.bash_profile
    echo 'export PATH=$PATH:$JAVA_HOME/bin' >> $${USER_HOME}/.bash_profile
    SHELL_SCRIPT

  init_script_install_git = <<-SHELL_SCRIPT
    yum install -y git
    SHELL_SCRIPT

  init_script_install_maven = <<-SHELL_SCRIPT
    cd $${USER_HOME}
    wget https://archive.apache.org/dist/maven/maven-3/${var.maven_version}/binaries/apache-maven-${var.maven_version}-bin.tar.gz
    tar -zxvf apache-maven-${var.maven_version}-bin.tar.gz
    echo 'export PATH=$PATH:$HOME/apache-maven-${var.maven_version}/bin' >> $${USER_HOME}/.bash_profile
    mkdir $${USER_HOME}/.m2
    echo '${file("./config/settings.xml")}' > $${USER_HOME}/.m2/settings.xml
    chown -R ec2-user:ec2-user $${USER_HOME}/apache-maven-${var.maven_version}
    chown -R ec2-user:ec2-user $${USER_HOME}/.m2
    SHELL_SCRIPT
}


# Key Pair
resource "aws_key_pair" "performance_batch_key_pair" {
  key_name = "performance_batch_key_pair"
  public_key = file("./ssh-key/performance.pub")
  tags = {
    Name = "performance_batch_key_pair"
  }
}

# EC2 Instance
resource "aws_instance" "performance_batch_bastion_instance" {
  ami = "ami-01748a72bed07727c"
  instance_type = "t2.micro"
  key_name = aws_key_pair.performance_batch_key_pair.id
  subnet_id = aws_subnet.performance_batch_public_subnet.id
  vpc_security_group_ids = [aws_security_group.performance_batch_bastion_sg.id]
  tags = {
    Name = "performance_batch_bastion_instance"
  }
  user_data = <<-SHELL_SCRIPT
    #!/bin/bash
    ${local.init_script_common}
    ${local.init_script_deploy_ssh_key}
    SHELL_SCRIPT
}

resource "aws_instance" "performance_batch_ap_instance" {
  ami = "ami-01748a72bed07727c"
  instance_type = "m5.large"
  key_name = aws_key_pair.performance_batch_key_pair.id
  subnet_id = aws_subnet.performance_batch_private_subnet1.id
  vpc_security_group_ids = [aws_security_group.performance_batch_private_subnet_sg.id]
  private_ip = "172.17.2.10"
  tags = {
    Name = "performance_batch_ap_instance"
  }
  depends_on = [aws_db_instance.performance_batch_db_instance]
  user_data = <<-SHELL_SCRIPT
    #!/bin/bash
    ${local.init_script_common}
    ${local.init_script_install_jdk}
    ${local.init_script_install_git}
    ${local.init_script_install_maven}

    # Make log directory
    mkdir $${USER_HOME}/logs
    chown ec2-user:ec2-user $${USER_HOME}/logs

    # Setup Environment Variables
    cat <<EOF >> $${USER_HOME}/.bash_profile
    export DB_HOST=${aws_db_instance.performance_batch_db_instance.address}
    export NABLARCH_DB_URL=jdbc:postgresql://${aws_db_instance.performance_batch_db_instance.endpoint}/performance
    export AWS_REGION=ap-northeast-1
    export AWS_ACCESS_KEY_ID=${var.aws_access_key}
    export AWS_SECRET_ACCESS_KEY=${var.aws_secret_access_key}
    EOF

    su - ec2-user <<EOF
    # Clone nablarch-example-batch
    git clone https://github.com/nablarch/nablarch-example-batch.git
    cd nablarch-example-batch

    # build old batch application
    git checkout ${var.git_old_tag_name}
    mvn -DskipTests -Ddb.host=${aws_db_instance.performance_batch_db_instance.address} package
    unzip -d target/old_batch target/${var.old_assembly_final_name}.zip

    # build new batch application
    git checkout ${var.git_new_tag_name}
    mvn -DskipTests -Ddb.host=${aws_db_instance.performance_batch_db_instance.address} package
    unzip -d target/new_batch target/${var.new_assembly_final_name}.zip

    git checkout -b ${var.git_branch_name} origin/${var.git_branch_name}

    # download csv file
    cd ~/
    wget https://www.post.japanpost.jp/zipcode/dl/kogaki/zip/ken_all.zip
    unzip ken_all.zip
    # convert charset
    iconv -f sjis -t utf8 KEN_ALL.CSV > importZipCode_all.csv
    # copy csv file to input directory
    mkdir -p nablarch-example-batch/target/old_batch/work/input
    cp importZipCode_all.csv nablarch-example-batch/target/old_batch/work/input
    mkdir -p nablarch-example-batch/target/new_batch/work/input
    cp importZipCode_all.csv nablarch-example-batch/target/new_batch/work/input
    EOF

    SHELL_SCRIPT
}
