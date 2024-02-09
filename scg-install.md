# deployment instructions - secure branch -  Secured by Spring Cloud Gateway
* this implementation uses spring cloud gateway to add the x-frame-options to the http header
* this implementation requires Spring Cloud Gateway to be installed in your foundation 
### edit scg2.yaml as appropriate for your project, replace `{your.apps.domain}` with your apps.domain 

### convert yaml to json to pass to spring cloud gateway instance
```
yq scg2.yaml -o=json | jq -c > scg1.json
	
cf create-service p.gateway standard wfd-gateway -c scg1.json
``` 
or subsequently 
```
cf update-service wfd-gateway -c scg1.json
```

get url of gateway in the browswer and open the url with the specified prefix in the browser

http://<scg-url>/ext/hello
