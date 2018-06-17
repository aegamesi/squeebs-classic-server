#!/bin/sh

docker create \
       --name=squeebs \
       -v /mnt/dokku-persistent/squeebs:/db \
       -e PGID=1001 -e PUID=1001 -e DB_PATH=/db \
       -p 12564:12564 -p 12566:12566 \
       elipsitz/squeebs-classic-server