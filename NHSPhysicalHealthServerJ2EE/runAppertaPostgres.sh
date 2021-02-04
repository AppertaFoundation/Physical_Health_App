#!/bin/bash

mvn clean compile spring-boot:run \
-Djasypt.encryptor.password=MasterJasyptPassword \
-Dspring-boot.run.arguments=--logging.level.com.staircase13.apperta=DEBUG \
-Dspring.datasource.url=jdbc:postgresql://localhost/postgres \
-Dspring.datasource.username=postgres \
-Dspring.datasource.password=mysecretpassword
