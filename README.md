# Squeebs Classic Server

### Dokku Deployment
* `dokku apps:create squeebs`
* `dokku ps:scale squeebs server=1 web=0`
* `dokku domains:add squeebs squeebs.aegamesi.com`
* `dokku config:set squeebs DOKKU_SKIP_ALL_CHECKS=true DOKKU_SKIP_DEFAULT_CHECKS=true`
* `dokku proxy:ports-set squeebs http:80:12566`
* `dokku docker-options:add squeebs deploy -p 12564:12564`
* `dokku checks:skip squeebs server,web`