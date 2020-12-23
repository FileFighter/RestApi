#!/usr/bin/env bash
DB_NAME="FileFighterDevDb"

echo ""
# Check if docker is running
if ! docker info >/dev/null 2>&1; then
  echo "[ERROR] Docker is not running!"
  exit 1
fi

if [[ $(docker ps -a --format "{{.Names}}" | grep $DB_NAME) ]]; then
  echo "Found container, starting it."
  docker start $DB_NAME
else
  echo "Found no container, creating and starting one."
  docker create -p 27017:27017 --name $DB_NAME mongo:latest
  docker start $DB_NAME
fi

echo ""