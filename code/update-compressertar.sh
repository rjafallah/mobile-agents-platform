#!/bin/bash

javac -d bin src/plateforme/*.java

javac -cp bin -d bin \
  AgentUtilisés/Compresser/CompresserAgent.java

jar cf src/apps/compresser.jar \
  -C bin CompresserAgent.class

echo "[OK] src/apps/compresser.jar updated"
