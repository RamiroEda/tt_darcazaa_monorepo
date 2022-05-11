#!/bin/bash
echo HOST_IP=$(hostname -I | awk '{print $1}') >> .env
sudo cp /etc/wpa_supplicant/wpa_supplicant.conf ./mavlink_controller_server/wpa_supplicant.conf
sudo docker-compose --env-file ./.env up
