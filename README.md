# Squeebs Classic Server

### Dokku Deployment
* `dokku apps:create squeebs`
* `dokku domains:add squeebs squeebs.aegamesi.com`
* `dokku storage:mount squeebs /mnt/dokku-persistent/squeebs:/db`
* `dokku config:set squeebs DB_PATH=/db`
