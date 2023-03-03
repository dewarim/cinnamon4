#!/bin/bash

TICKET=$(curl --silent --show-error -X POST "http://localhost:9090/cinnamon/connect?user=admin&password=admin&format=text")

curl -X POST --silent --show-error --header "ticket: ${TICKET}" --header "Content-type: application/xml" --data "<listIndexItemRequest><type>FULL</type></listIndexItemRequest>" "http://localhost:9090/api/indexItem/list"
echo;