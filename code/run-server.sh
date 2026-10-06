#!/bin/bash
# Compile la plateforme puis lance un node serveur
# Usage: ./run-server.sh 2001

PORT=$1
if [ -z "$PORT" ]; then
  echo "Usage: ./run-server.sh <port>"
  exit 1
fi

javac -d bin src/plateforme/*.java

java -cp bin AgentServer $PORT
