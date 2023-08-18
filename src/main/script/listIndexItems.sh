#!/bin/bash

PORT=8080
TICKET=$(curl --silent --show-error -X POST "http://localhost:${PORT}/cinnamon/connect?user=admin&password=admin&format=text")

curl -X POST --silent --show-error --header "ticket: ${TICKET}" --header "Content-type: application/xml" --data "<listIndexItemRequest><type>FULL</type></listIndexItemRequest>" "http://localhost:${PORT}/api/indexItem/list"
echo;