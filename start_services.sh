#!/bin/bash
cd ./server
npm run build && npm start &

sleep 10

cd ../mavlink_controller_server
make run