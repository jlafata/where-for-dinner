#cf cs p.gateway    standard  wfd-gateway
cf cs p.rabbitmq   single-node  wfd-notify-q
cf cs p.mysql      db-small    wfd-search-db
