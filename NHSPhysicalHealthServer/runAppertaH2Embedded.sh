#!/bin/bash

mvn clean compile spring-boot:run \
-Djasypt.encryptor.password=MasterJasyptPassword \
-Dspring-boot.run.arguments="--logging.level.com.staircase13.apperta=DEBUG,--logging.level.org.springframework.boot.autoconfigure.security.oauth2=DEBUG,--logging.level.org.springframework.security.web.authentication=DEBUG" \
-Dspring.datasource.url=jdbc:h2:mem:testdb \
-Dspring.datasource.username=sa \
-Dspring.datasource.password= \
-Dspring.h2.console.enabled=true \
-Dspring.h2.console.path=/h2-console