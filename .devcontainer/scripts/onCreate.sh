#!/bin/sh

cd /workspace/lila-ws
sbt compile

cd /workspaces/lila
sbt bloopInstall
bloop compile lila

./ui/build
