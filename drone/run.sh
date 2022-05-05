#!/bin/bash
docker build -t app .
docker run --device=/dev/ttyAMA0:com4 app