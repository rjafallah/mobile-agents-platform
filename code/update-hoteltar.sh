#!/bin/bash
# Recompile plateforme + appli Hotel, puis reconstruit src/apps/hotel.jar


# 1) Compiler la plateforme
javac -d bin src/plateforme/*.java

# 2) Compiler l'appli Hotel (3 fichiers)
javac -cp bin -d bin \
  AgentUtilisés/Hotel/HotelsService.java \
  AgentUtilisés/Hotel/PhoneBookService.java \
  AgentUtilisés/Hotel/HotelAgent.java

# 3) Construire le jar dans src/apps
jar cf src/apps/hotel.jar \
  -C bin HotelAgent.class \
  -C bin HotelsService.class \
  -C bin PhoneBookService.class

echo "[OK] src/apps/hotel.jar updated"
