# License
This source is made available under the GNU Affero General Public License, full terms at https://www.gnu.org/licenses/agpl-3.0.en.html.

# Apperta Physical Health Application Server
This server provides a middleware component that works between an openEHR server/demographics server and  simple mobile or web applications designed for patients or health care professionals.
The server provides one part of a personal health record (PHR) based architecture in which a patient user is responsible for creating and managing their health record and can give access to registered health care professionals (HCP) on an individual basis. 
This framework is designed with the purpose of simplifying the access to the openEHR data for an application within the PHR design but enabling the client application full access to the details of each openEHR construct. 

The key design aim of the server is that a user can login for a session and make requests to query or create/update EHR compositions or demographics details. For a patient, the requests will always refer to themselves, and for a HCP the requests will only be applied if the patient has granted access in advance.
The server enforces security and permission based access. The server is built to use OAuth based authentication. A simple internal OAuth implementation is included for demonstration, but it can also be adapted to use an external OAuth server.
The server also provides some basic templating features which enable an "app" to be registered which can manage the complex composition and aql path strings associated with openEHR documents and queries. The templates enable a client API in which simple identifiers can be used by the client application, which are replaced with the open paths before being passed on to the openEHR api. This is especially useful for storing complex aql queries within the middleware.

The server creates an interface to the communications layer that connects to openEHR instances or demographics servers, which are referred to internally as "connectors". This design is intended to simplify the process of connecting with different flavours of openEHR server while maintaining a consistent interface for client apps. New connectors can be added under the impls folder and the required connector can be configured at runtime via the spring application.properties.
The default connectors included within the project can connect to a Marand openEhr server and a Marand demographics API which is intended for demonstration purposes only.

The framework is build upon Spring Boot, which provides a minimal source approach to constructing Spring Java based server applications. It uses a number of dependencies including FlyWay for database management and Lombok for automantic generation of getters and setters for POJOs.
It has Swagger configured to document all of the client API endpoints.
The server is configured by default to have all debug logging switched on, it is intended for prototyping and demonstration purposes.

# Starting the Service

## Via Docker Compose

Using the docker compose configuration is recommended because it provides a fully work demo of the system including all services it depends upon (e.g. Postgres, Mailhog etc.)

First build the Apperta docker container using the 'docker' Maven profile.

```
mvn clean install -Pdocker
```

Optionally, skip tests to make the build quicker

```
mvn clean install -Pdocker -DskipTests
```

then use the runCompose.sh script to start docker compose

```
./runCompose.sh
```

* Mail Hog (for checking mail) is available on http://localhost:8025
* Wire Mock (pre configured to mock the backend) has an admin UI on http://localhost:8090/__admin/
* Adminer (for querying the database) is available on http://localhost:8081. To query the database:
 * host: db
 * database: postgres
 * username: postgres
 * password: mysecretpassword

Note that the containers will be destroyed everytime you run runCompose.sh. If you want to retain them (e.g. retain the database), remove the 'rm' command from the shell script.

## Via Maven

To run via Maven use either runAppertaH2Embedded.sh or runAppetaPostgres.sh, depending on which database you want Apperta to use.

If you need a postgres instance, use runPostgres.sh

# Using the API

## Healthcheck

The API will listen on port HTTP 8080. The health check URL should return a 200 if all is well with a status of 'UP'

```curl -v http://localhost:8080/actuator/health```

## User Creation and Authentication

First, retrieve an oauth token using the client credentials grant

```
curl --verbose \
     --data grant_type=client_credentials \
     my-trusted-client:secret@localhost:8080/oauth/token  
```

This will return a JWT Token

Now register a user that can be used to retrieve an OAuth token, using the previously sent JWT token e.g.

```
curl  --verbose \
      --header "Content-Type: application/json" \
      --header "Authorization: Bearer <client_access_token>" \
      --request POST \
      --data '{"username":"testUser1","password":"testPassword1","role":"PATIENT","emailAddress":"test@here.com"}' \
      http://localhost:8080/api/user/register
```

Now retrieve an oauth token for the newly created using the password grant

```
curl --verbose \
     --data grant_type=password \
     --data username=testUser1 \
     --data password=testPassword1 \
     my-trusted-client:secret@localhost:8080/oauth/token
```

You can now call secured endpoints using the user access token e.g.

```
curl --verbose \
     --header "Authorization: Bearer <user_access_token>" \
     http://localhost:8080/api/device/remind 
```

The HCP Profile be used by a HCP user. Using an oauth token for a different user type will fail with a 403 error

```
curl --verbose \
     --header "Authorization: Bearer <user_access_token>" \
     http://localhost:8080/api/user/hcp/profile
```

## Password Reset

To trigger a Reset Email

```
curl --verbose \
     --header "Authorization: Bearer <client_access_token>" \
     --header "Content-Type: application/json" \
     --data '{"username":"testUser1"}' \
     http://localhost:8080/api/user/passwordResetTokenRequest
```

The reset token is sent in an email (see Mailhog). To reset the password either use the UI linked in the email or use the reset API...

Validate a token using

```
curl --verbose \
     --header "Authorization: Bearer <client_access_token>" \
     --header "Content-Type: application/json" \
     --data '{"token":"7e5cc78f-0afc-4953-b819-b3c2203f08d1"}' \
     http://localhost:8080/api/user/passwordResetTokenVerify
```

Change a password using

```
curl --verbose \
     --header "Authorization: Bearer <client_access_token>" \
     --header "Content-Type: application/json" \
     --data '{"token":"761a62c8-9a77-4a9f-9ccc-34eaf55c5118", "password": "NewPassword"}' \
     http://localhost:8080/api/user/passwordReset
```

## Health Check

You can check the status of the application using

```
curl --verbose localhost:8080/actuator/health
```

If you want a more detailed breakdown of the health, authenticate to the end point using the client credentials grant e.g.

```
curl  --header "Authorization: Bearer <client_credentials_grant_token>" \\
      http://localhost:8080/actuator/health | json_pp
```

# Developer Notes

## API Password Management

JAsypt is used to encrypt passwords, using a single master password. This master password is then provided as an argument when running the application
 
To generate encrypted passwords, see http://www.jasypt.org/cli.html. Alternatively, if you're on a development machine you can call the jasypt JAR directly e.g.

```
java -cp ~/.m2/repository/org/jasypt/jasypt/1.9.2/jasypt-1.9.2.jar  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="secret" password=MasterJasyptPassword algorithm=PBEWithMD5AndDES
```
 
Encrypted passwords should be set in the application.properties surrounded by ENC(...). For example:

```
apperta.oauth.client.password                 = ENC(Lw++P+MfcmFlymWQpfVJNw==)
```

The master password is passed to the application using the 'jasypt.encryptor.password' property (see 'getting started' at the top of this page)

## Caching Backend Calls

* An example of calling a REST backend has been added via the /user/backendInfo API, including Unit and Integration tests
* Calls to this REST backend are cached using Spring Caching and Hazelcast
* Hazelcast supports distributed caches
* Wiremock is used to emulate the backend. 
 * Either start Wiremock using 'runWiremockBackend.sh', or use the Docker Compose configuration
 * You can check the wiremock configuring using http://localhost:8090/__admin/

Call the demo API method using

```
curl --verbose \
     --header "Authorization: Bearer b8d389ee-ed59-4544-8977-7f6e4da54a49" \
     http://localhost:8080/user/backendInfo?username=dave
```

## Intellij

If using Intellij, first install the Lombok Plugin and enable Annotation Processors. See this [Stack Overflow](https://stackoverflow.com/questions/9424364/cant-compile-project-when-im-using-lombok-under-intellij-idea) page for more information.

## Database Version Control

* Flyway is used to version control DDL. 
* Changes to the database should be added to /src/main/resources/db/migration. 
* Flyway will automatically apply DDL scripts by checking which have already been applied using the 'flyway_schema_history' database table.
* DDL for JPA entities is automatically generated when you run maven install (at a minimum, run 'mvn package -DskipTests'.). The generated DDL is available in /target/generated-resources. This will need to be selectively copied into a new sql file in db/migrations

## Swagger

* Swagger is automatically generated and can be viewed/downloaded via http://localhost/swagger-ui.html
 * swagger-ui can be used to make calls.
 * For calls other than /user/register, you'll need to retrieve an OAuth token
 * You can retrieve an OAuth token using Swagger UI. Note that the 'type' drop-down should be set to 'Request body', NOT 'Basic auth'. These options are badly named, as discussed in https://github.com/swagger-api/swagger-ui/issues/3227
* Rest Controllers and DTOs should be annotated with Spring Fox annotations to provide information about the operations and model
* See user/register for an example

## Lack of Entropy

If using docker containers after a restart it may take a while for the CMS Login page to appear. In this case you'll see something like the following in the log:

    2019-06-08 09:43:41.519  WARN [Apperta,a13864042d9b178a,a13864042d9b178a,false] 1 --- [http-nio-8080-exec-9] o.a.c.util.SessionIdGeneratorBase        : Creation of SecureRandom instance for session ID generation using [SHA1PRNG] took [51,859] milliseconds.

You can check the amount of entropy availabel by running this on the host container

    cat /proc/sys/kernel/random/entropy_avail
   
This value should be > 1000 when idle

To improve entropy available from /dev/random on docker host, we can use the harbur docker image. More information is available at https://github.com/harbur/docker-haveged   
   
This is included by default in the docker compose file.   
   
Some context is available here - https://stackoverflow.com/questions/26021181/not-enough-entropy-to-support-dev-random-in-docker-containers-running-in-boot2d   


## Reference

* OAuth Example from Spring Security - https://github.com/spring-projects/spring-security-oauth/blob/master/samples/oauth2/sparklr
* OAuth Spring Security Token Store Schema - https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql
* Spring Security Expressions - https://www.baeldung.com/spring-security-expressions
* JASypt Spring Boot Integration - https://www.baeldung.com/spring-boot-jasypt
* Docker & Spring Boot - https://spring.io/guides/gs/spring-boot-docker/