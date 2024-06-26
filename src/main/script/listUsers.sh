#!/bin/bash

#cd ../../../
#java -jar target/cinnamon-server.jar --config default-config.xml &

PORT=8080
TICKET=$(curl --silent --show-error -X POST "http://localhost:${PORT}/cinnamon/connect" --data "<connectionRequest><username>admin</username><password>admin</password><format>text</format></connectionRequest>")

curl -X POST --silent --show-error --header "ticket: ${TICKET}" --header "Content-type: application/xml" --data "<listUserAccountRequest><type>FULL</type></listUserAccountRequest>" "http://localhost:${PORT}/api/user/list"
echo;