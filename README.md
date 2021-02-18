# SpringBootDemo

springboot + mysql sample

google's jib maven plugin added for building and pushing application as docker image to registry

https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin
https://github.com/GoogleContainerTools/jib

The following is the command to build and push docker image,

mvn clean install jib:build -Djib.from.image=docker.io/scottyengineering/java11:latest -Djib.from.auth.username=${docker hub user name} -Djib.from.auth.password=${docker hub password} -Djib.to.image=docker.io/suresh918/springboot-demo:latest -Djib.to.auth.username=${docker hub user name} -Djib.to.auth.password=${docker hub password} 


the following is the command used in mychange project gitlab pipeline,
 mvn --settings $MAVEN_SETTINGS --no-transfer-progress clean compile jib:build -Djib.from.image=$ARTIFACTORY_REGISTRY_DOCKER/distroless/java:11 -Djib.from.auth.username=$ARTIFACTORY_USERNAME -Djib.from.auth.password=$ARTIFACTORY_TOKEN -Djib.to.image=$ARTIFACTORY_REGISTRY_DOCKER/$CI_PROJECT_NAME:$CI_COMMIT_REF_NAME -Djib.to.auth.username=$ARTIFACTORY_USERNAME -Djib.to.auth.password=$ARTIFACTORY_TOKEN -Djib.httpTimeout=120000



