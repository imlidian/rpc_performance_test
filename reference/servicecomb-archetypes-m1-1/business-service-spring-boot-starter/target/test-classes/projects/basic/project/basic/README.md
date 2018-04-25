## Welcome to use ServiceComb Java Chassis
This project(module) is generate by *org.apache.servicecomb.archetypes:business-service-spring-boot-starter-archetype*, it use **spring-boot-starter-provider** to develop service endpoint.  

### More works can be done further:
1. Modify "HelloEndpoint", add your business service logic, or create some new endpoints to provide your services. More details can be found : http://servicecomb.incubator.apache.org/users/use-servicecomb-in-spring-boot/
2. Modify "microservice.yaml", change APPLICATION_ID, service_description.name, version, and service center address, endpoints publish address etc. More details can be found : http://servicecomb.incubator.apache.org/users/service-definition/

### Package your service
Under project(module) root folder, run 
```bash
mvn package
```
Then you can get executable jar in target/bin folder:   
- xxxxxx-{version}-exec.jar    
```bash
java -jar xxxxxx-{version}-exec.jar
```
*Notice: If you need to modify config setting in "microservice.yaml" like service center address but don't want repackage the executable jar, **you can direct place a new "microservice.yaml" file in same folder, then settings will be overridden.***