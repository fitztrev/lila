#!/bin/sh

sudo service nginx restart

sudo mongod --fork --dbpath /workspaces/mongodb-data --logpath /var/log/mongod.log

redis-server --daemonize yes
