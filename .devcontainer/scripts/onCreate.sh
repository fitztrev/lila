#!/bin/sh

cd /workspaces
git clone https://github.com/lichess-org/lila-ws.git
git clone https://github.com/lichess-org/lila-db-seed.git
git clone https://github.com/lichess-org/fishnet.git --recursive
git clone https://github.com/lichess-org/lila-fishnet.git
git clone https://github.com/lichess-org/pgn-viewer.git

## Setup initial database and seed test data (users, games, puzzles, etc)
mkdir -p /workspaces/mongodb-data
sudo mongod --fork --dbpath /workspaces/mongodb-data --logpath /var/log/mongod.log
mongo lichess /workspaces/lila/bin/mongodb/indexes.js
python3.9 /workspaces/lila-db-seed/spamdb/spamdb.py --drop all
sudo killall mongod

cd /workspaces/lila-ws
sbt compile

cd /workspaces/lila
sbt bloopInstall
bloop compile lila

./ui/build
