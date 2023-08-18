#!/bin/bash

#java -jar target/cinnamon-server.jar --config debug-config.xml &

PORT=8080
TICKET=$(curl --silent --show-error -X POST "http://localhost:${PORT}/cinnamon/connect?user=admin&password=admin&format=text")

curl -X POST --silent --show-error --header "ticket: ${TICKET}" --header "Content-type: application/xml" --data "<listUserAccountRequest><type>FULL</type></listUserAccountRequest>" "http://localhost:${PORT}/api/user/list"
echo;