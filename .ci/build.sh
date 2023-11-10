#!/bin/bash

# 必须在当前目录下执行脚本
# 环境
profile=prod
## build
mvn -DskipTests=true clean package -P ${profile} -f ../pom.xml

# >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
# build 服务器
host_name=root@120.46.55.96
identity_file=~/.ssh/kube-prod-key.pem
ssh_port=1022

#host_name=root@192.168.10.14
#identity_file=~/.ssh/id_rsa
#ssh_port=22

# <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

# 服务名字
service_name=charge_pile_machine
# 容器镜像版本
container_tag=1.0.1


tmp_dir=/tmp/tmp-devops-$(openssl rand -hex 8)
# SCP 发布包
echo "start deploy to server path:${tmp_dir}"
ssh -i ${identity_file} -p ${ssh_port} ${host_name} "mkdir -p ${tmp_dir}/target/"

# 发布包
scp -i ${identity_file} -P ${ssh_port} -r ./.env                                                       ${host_name}:${tmp_dir}
scp -i ${identity_file} -P ${ssh_port} -r ./docker-compose.yml                                         ${host_name}:${tmp_dir}
scp -i ${identity_file} -P ${ssh_port} -r ../charge-pile-machine-server/Dockerfile                     ${host_name}:${tmp_dir}
scp -i ${identity_file} -P ${ssh_port} -r ../charge-pile-machine-server/docker-entrypoint.sh           ${host_name}:${tmp_dir}
scp -i ${identity_file} -P ${ssh_port} -r ../charge-pile-machine-server/target/application.jar         ${host_name}:${tmp_dir}/target/


ssh -i ${identity_file} -p ${ssh_port} -tt ${host_name} << remotessh
cd ${tmp_dir}
docker buildx build -t ${service_name}:latest .
docker tag ${service_name}:latest swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}
docker push swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}
exit
remotessh
echo "end"