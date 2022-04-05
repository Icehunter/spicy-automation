#! /usr/bin/env bash
docker build -t ansible -f ./resources/ansible/Dockerfile ./resources/ansible
docker run -it -v $PWD/resources/ansible:/app ansible /bin/bash
