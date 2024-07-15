# demo steps
##setup
export PATH=~/tanzu-workshops/app\ advisor/vmware-app-analyzer-upgrades/cli/build/native/nativeCompile:$PATH
sdk use java 17-open

## pull build-config - get dependencies 
advisor build-config get

## publish analysis to dashboard
advisor build-config publish -u http://localhost:8080

## get upgrade plan
advisor upgrade-plan get -u http://localhost:8080

## upgrade and push to PR
export GIT_TOKEN_FOR_PRS=<git-personal-access-token>
advisor upgrade-plan apply --push  -u http://localhost:8080  --token=$GIT_TOKEN_FOR_PRS



-------------------------------

# commands to reset repository
git status .

git stash push

git pull --force

git stash drop

mvn clean

# create a new repository
git remote add aa-demo git@github.com:jlafata/spring-petclinic-aa-demo.git
git remote remove origina

# in docker container

run    `ifconfig | grep "inet "`
and get an ip address for your local workstation,  in my case today it's : 192.168.86.17
this is because you can't use localhost within docker

docker run -it --rm -v $PWD:/source  advisor-cli build-config get

docker run -it --rm -v $PWD:/source  advisor-cli build-config publish --url=http://192.168.86.17:8080 

docker run -it --rm -v $PWD:/source advisor-cli upgrade-plan get --url=http://192.168.86.17:8080

docker run -it --rm -v $PWD:/source advisor-cli upgrade-plan apply --url=http://192.168.86.17:8080



