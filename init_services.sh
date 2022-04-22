#!/bin/bash
cd ./server
npm install && npx prisma migrate deploy
cd ../mavlink_controller_server
make install