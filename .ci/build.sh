#!/bin/bash

# 必须在当前目录下执行脚本
# 临时目录
host_name=root@192.168.10.14
tmp_dir=/tmp/tmp-devops-$(openssl rand -hex 8)
service_name=charge_pile_machine

# 容器镜像版本
container_tag=latest

# 环境
profile=prod

## build
mvn -DskipTests=true clean package -P ${profile} -f ../pom.xml

# SCP 发布包
echo "start deploy to server path:${tmp_dir}"
ssh ${host_name} "mkdir -p ${tmp_dir}/target/"

# 发布包
scp -r ./.env                                                       ${host_name}:${tmp_dir}
scp -r ./docker-compose.yml                                         ${host_name}:${tmp_dir}
scp -r ../charge-pile-machine-server/Dockerfile                     ${host_name}:${tmp_dir}
scp -r ../charge-pile-machine-server/docker-entrypoint.sh           ${host_name}:${tmp_dir}
scp -r ../charge-pile-machine-server/target/application.jar         ${host_name}:${tmp_dir}/target/


ssh -tt ${host_name} << remotessh
cd ${tmp_dir}
docker buildx build -t ${service_name}:${container_tag} .
docker tag ${service_name}:${container_tag} swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}
docker push swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}
exit
remotessh
echo "end"