# Squeebs Classic Server

This is the updated version of the Squeebs Classic Server that uses WebSockets.

### Dokku Deployment
* `dokku apps:create squeebs`
* `dokku domains:add squeebs squeebs.aegamesi.com`
* `dokku storage:mount squeebs /mnt/dokku-persistent/squeebs:/db`
* `dokku config:set squeebs DB_PATH=/db`
