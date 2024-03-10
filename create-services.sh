#cf cs p.gateway    standard  wfd-gateway
#cf cs p.rabbitmq   single-node  wfd-notify-q
#cf cs p.mysql      db-small    wfd-search-db
cf cs p.rabbitmq   ha           wfd-notify-q
cf cs p.mysql      db-medium    wfd-search-db
cf cs p-redis      shared-vm    wfd-redis-cache
