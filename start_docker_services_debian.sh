#!/bin/bash
echo HOST_IP=$(hostname -I | awk '{print $1}') >> .env

sudo docker-compose --env-file ./.env up
