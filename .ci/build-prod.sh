#!/bin/bash
set -e
source ./env.sh
# 必须在当前目录下执行脚本

# 服务名字
service_name=charge_pile_machine
# 环境
profile=prod
# 容器镜像版本
container_tag=v231224

## build
# ──────────────────────────────────────────────────────────────────────────────────────────────────────────
# 服务器变量
host_name=$host_name
identity_file=$identity_file
ssh_port=$ssh_port
mvn -DskipTests=true clean package -P ${profile} -f ../pom.xml
# ──────────────────────────────────────────────────────────────────────────────────────────────────────────


# 发布包
cd ../charge-pile-machine-server

tmp_dir=/tmp/tmp-devops-$(openssl rand -hex 8)
echo "──────────────────────────────────────────────────────────────────────────────────────────────────────────"
echo "start deploy to server path:${tmp_dir} host_name:$host_name ssh_port:$ssh_port identity_file:$identity_file"
echo "swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}"
echo "──────────────────────────────────────────────────────────────────────────────────────────────────────────"
ssh -i "$identity_file" -p "$ssh_port" "$host_name" "mkdir -p $tmp_dir/target/"
scp -i "$identity_file" -P "$ssh_port" -r ./Dockerfile                                 "$host_name":"$tmp_dir"
scp -i "$identity_file" -P "$ssh_port" -r ./docker-entrypoint.sh                       "$host_name":"$tmp_dir"
scp -i "$identity_file" -P "$ssh_port" -r ./target/application.jar                     "$host_name":"$tmp_dir"/target/



# shellcheck disable=SC2087
ssh -i "$identity_file" -p "$ssh_port" -tt "$host_name" << EOF
cd ${tmp_dir}
docker buildx build -t ${service_name}:latest .
docker tag ${service_name}:latest swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}
docker push swr.cn-north-4.myhuaweicloud.com/huamar/${service_name}:${container_tag}
exit
EOF
echo "end"