#!/bin/sh

## Create config for lila
cp /workspaces/lila/conf/application.conf.default /workspaces/lila/conf/application.conf
tee -a /workspaces/lila/conf/application.conf <<EOF
net.site.name = "lila-codespace"
net.domain = "$CODESPACE_NAME-8080.preview.app.github.dev"
net.socket.domains = [ "$CODESPACE_NAME-8080.preview.app.github.dev" ]
net.base_url = "https://$CODESPACE_NAME-8080.preview.app.github.dev"
net.asset.base_url = "https://$CODESPACE_NAME-8080.preview.app.github.dev"
EOF

## Create config for lila-ws (websockets)
tee /workspaces/lila-ws-gitpod-application.conf <<EOF
include "application"
csrf.origin = "https://$CODESPACE_NAME-8080.preview.app.github.dev"
EOF

## Create config for fishnet clients
tee /workspaces/fishnet/fishnet.ini <<EOF
[fishnet]
cores=auto
systembacklog=long
userbacklog=short
EOF
