#!/bin/bash

## build
#mvn -DskipTests=true clean package -P dev

# 必须在当前目录下执行脚本
# 临时目录
#host_name=root@121.36.36.155
#tmp_dir=/tmp/tmp-devops-$(openssl rand -hex 8)
#service_name=charge_pile_machine
#
## SCP 发布包
#echo "start deploy to server path:${tmp_dir}"
#ssh ${host_name} "mkdir -p ${tmp_dir}/target/"
#
## 发布包
#scp -r ./.env                                                       ${host_name}:$tmp_dir
#scp -r ./docker-compose.yml                                         ${host_name}:$tmp_dir
#
#
#ssh -tt ${host_name} << remotessh
#cd ${tmp_dir}
#docker-compose -p ${service_name} up -d --force-recreate
#exit
#remotessh
#echo "end"