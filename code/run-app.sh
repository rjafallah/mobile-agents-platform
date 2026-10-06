#!/bin/bash
# Compile la plateforme puis lance l'app-node avec un jar dans src/apps
# Usage: ./run-app.sh 2000 hello.jar Hello

PORT=$1
JAR_NAME=$2
MAIN_CLASS=$3

if [ -z "$PORT" ] || [ -z "$JAR_NAME" ] || [ -z "$MAIN_CLASS" ]; then
  echo "Usage: ./run-app.sh <port> <jarName> <MainClass>"
  echo "Ex:    ./run-app.sh 2000 hello.jar Hello"
  exit 1
fi

javac -d bin src/plateforme/*.java

java -cp bin AgentServer $PORT src/apps/$JAR_NAME $MAIN_CLASS
