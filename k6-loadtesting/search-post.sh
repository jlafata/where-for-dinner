#!/bin/bash
SEARCH_URL=https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/addSearch
echo $SEARCH_URL
#SEARCH_QUERY="searchName=test&zipCode=64106&radius=50&diningName=Bristol,Chophouse&startTime=02/17/2024, 06:02 PM&endtime=02/19/2024, 06:02 PM"
#echo $SEARCH_QUERY

#SEARCH_JSON='{"name":"test28","postalCode":64106,"radius":50,"diningNames":"Bristol","startTime":1708202940000,"endTime":1708980540000,"continousSearch":true,"sendResultsTo":"xx"}'
SEARCH_JSON=$(cat search-post.json)
echo $SEARCH_JSON

#curl -X POST $SEARCH_URL  \
#   -H "Content-Type: application/x-www-form-urlencoded" \
#   -k -d \"$SEARCH_QUERY\"

curl -X PUT https://where-for-dinner-search.apps.h2o-2-22522.h2o.vmware.com/search \
   -H "Content-Type:application/json" -H 'Accept:application/json'\
   -k -d "$SEARCH_JSON"

#DiningSearch.js:28
#       GET https://where-for-dinner-api-gateway.apps.h2o-2-22522.h2o.vmware.com/api/availability/availability/No%20Searched%20Submitted