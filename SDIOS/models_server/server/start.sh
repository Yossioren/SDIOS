#!/usr/bin/env bash
if [ -z $1 ]
then
  echo "Usage: ./start.sh <port>"
  exit -1
fi

python3 -m flask run --reload --host 0.0.0.0 --port $1
