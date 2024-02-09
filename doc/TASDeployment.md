# Tanzu Application Service (TAS) Deployment

This document describes a manual Where for Dinner TAS deployment.  

## Prerequisites

These instructions assume that you have the following installed locally:

* cf cli v7 or higher 
* jdk version 17,  I used `Using java version 17.0.9-librca`'
* node installed
* yarn installed

And a Tanzu Application Service Foundation with:
* Rabbit MQ tile installed
* Mysql Tile installed

Subsequent testing may investigate integrating with these tiles, currently  
* SSO for Tanzu Application Service (required if using the `Enable Security` option) - see the where-for-dinner-ui project
* Tanzu Spring Cloud Gateway (required if using the `Spring Cloud Gateway tile for Tanzu Application Service ` option)

## Quick Start

This section provides a fast track installation of the "simplest" configuration of the Hungry application using the application accelerator and the instructions immediately below.  A more thorough description of the configuration and installation scenarios are describes in subsequent sections of this page.  This section assumes you have already installed the application accelerator using the instructions at the top of the page.

from the where-for-dinner parent project
run the following
```code
mvn clean install
run create-services.sh
cd where-for-dinner-ui
yarn build
cf push
cd ../where-for-dinner-search-proc
cf push
cd ../where-for-dinner-search
cf push
cd ../where-for-dinner-notify
cf push
cd ../where-for-dinner-crawler-python
cf push
cd ../where-for-dinner-crawler
cf push
cd ../where-for-dinner-availability
cf push
cd ../where-for-dinner-api-gateway
vi src/main/resources/application.yaml
### update the where-for-dinner.tas-fqdn-apps-domain and save the changes
mvn clean package
cf push


### Monitor and Verify Installation

make sure all applications are running and all servies are successfully created

```code
cf a
Getting apps in org dev / space dev as admin...

name                              requested state   processes           routes
where-for-dinner-api-gateway      started           web:1/1, task:0/0   where-for-dinner-api-gateway.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-availability     started           web:1/1, task:0/0   where-for-dinner-availability.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-crawler          started           web:1/1, task:0/0   where-for-dinner-crawler.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-crawler-python   started           web:1/1             where-for-dinner-crawler-python.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-notify           started           web:1/1, task:0/0   where-for-dinner-notify.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-search           started           web:1/1, task:0/0   where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-search-proc      started           web:1/1, task:0/0   where-for-dinner-search-proc.apps.h2o-2-22522.h2o.vmware.com
where-for-dinner-ui               started           web:1/1             where-for-dinner-ui.apps.h2o-2-22522.h2o.vmware.com

```

```code
cf s
Getting service instances in org dev / space dev as admin...

name            offering     plan          bound apps                                                                                                      last operation     broker                   upgrade available
wfd-notify-q    p.rabbitmq   single-node   where-for-dinner-notify, where-for-dinner-search, where-for-dinner-search-proc, where-for-dinner-availability   create succeeded   rabbitmq-odb             no
wfd-search-db   p.mysql      db-small      where-for-dinner-search, where-for-dinner-search-proc, where-for-dinner-availability                            create succeeded   dedicated-mysql-broker   no
```

## open each of the following java spring boot applications and verify that the actuator health endpoint is health
### sample actuator health endpoint
https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/actuator/health

the result on a successful deployment should be 
```code
{
  "status": "UP"
}
```

## if you need it, you can get api-docs for the search api endpoint 
https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/v3/api-docs


## Uninstall


To remove the application from your cluster, navigate to the root directory of the unzipped accelerator file and run the following commands to delete all of the created resources:

```
cf delete where-for-dinner-api-gateway -f 
cf delete where-for-dinner-ui -f
cf delete where-for-dinner-search -f
cf delete where-for-dinner-search-proc -f
cf delete where-for-dinner-notify -f
cf delete where-for-dinner-availability -f
cf delete where-for-dinner-crawler -f
cf delete where-for-dinner-crawler-python -f

cf unbind-service where-for-dinner-notify wfd-notify-q
cf unbind-service where-for-dinner-search wfd-notify-q
cf unbind-service where-for-dinner-search-proc wfd-notify-q
cf unbind-service where-for-dinner-availability wfd-notify-q

cf unbind-service where-for-dinner-search wfd-search-db
cf unbind-service where-for-dinner-search-proc wfd-search-db
cf unbind-service where-for-dinner-availability wfd-search-db

cf delete-service wfd-notify-q
cf delete-service wfd-search-db

```
