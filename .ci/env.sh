#!/bin/bash
set -e
# >>>>>>>>>>>>> build 服务器 >>>>>>>>>>>>>>>>>
#host_name=root@121.36.36.155
#identity_file=~/.ssh/kube-prod-key.pem
#ssh_port=1022
#--------------------------------------------
#host_name=root@221.176.140.236
#identity_file=~/.ssh/id_rsa
#ssh_port=22
# ---------------------------
host_name=root@120.46.55.96
identity_file=~/.ssh/kube-prod-key.pem
ssh_port=22
# <<<<<<<<<<<<< build 服务器 <<<<<<<<<<<<<<<<<
echo "─────────────────────────────────────────────────────"
echo host_name:${host_name}
echo identity_file:${identity_file}
echo ssh_port:${ssh_port}
echo "─────────────────────────────────────────────────────"