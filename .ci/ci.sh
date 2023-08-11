#!/bin/bash
#echo -n "Enter your name: "
#read name
#echo "Hello $name,welcome to my program"


# 必须在当前目录下执行脚本
# 临时目录
hmTmpDir=/tmp/tmp.$(openssl rand -hex 8)/hm-mchine

## build
#mvn -DskipTests=true clean package -P dev


# SCP 发布包
echo "start deploy to server path:$hmTmpDir"
ssh -p 22 root@221.176.140.236 "mkdir -p $hmTmpDir/target/"
scp -P 22 -r ./docker-compose.yml                                          root@221.176.140.236:$hmTmpDir
scp -P 22 -r ./.env                                                        root@221.176.140.236:$hmTmpDir
scp -P 22 -r ../charge-pile-machine-server/target/application.jar          root@221.176.140.236:$hmTmpDir/target/
scp -P 22 -r ../charge-pile-machine-server/Dockerfile                      root@221.176.140.236:$hmTmpDir
scp -P 22 -r ../charge-pile-machine-server/docker-entrypoint.sh            root@221.176.140.236:$hmTmpDir


ssh -p 22 -tt root@221.176.140.236 << remotessh
cd $hmTmpDir
docker build -t charge_pile_machine:latest .
docker-compose down
docker-compose up -d
exit
remotessh

echo "end"