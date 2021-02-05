#!/bin/bash

# view configuration on http://localhost:8090/__admin/
docker run -it --rm -p 8090:8080 -v $PWD/src/test/wiremock/backend:/config rodolpheche/wiremock --verbose --root-dir /config --global-response-templating



