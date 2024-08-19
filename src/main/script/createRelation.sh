#!/bin/bash

PORT=8080
TICKET=$(curl --silent --show-error -X POST "http://localhost:${PORT}/cinnamon/connect" --data "<connectionRequest><username>admin</username><password>admin</password><format>text</format></connectionRequest>")
echo "${TICKET}"

curl -X POST --silent --show-error --header "ticket: ${TICKET}" --header "Content-type: application/xml" \
--data "<createRelationRequest><relations><relation><leftId>1</leftId><rightId>2</rightId>            <typeId>3</typeId>            <metadata>&lt;meta/></metadata></relation></relations></createRelationRequest>" \
"http://localhost:${PORT}/api/relation/create"

echo;