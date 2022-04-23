#!/bin/bash
cd ./server
npm run dev &

sleep 10

cd ../mavlink_controller_server
make run