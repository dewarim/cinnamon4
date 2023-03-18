#!/bin/bash

TICKET=$(curl --silent --show-error -X POST "http://localhost:9090/cinnamon/connect?user=admin&password=admin&format=text")

echo "<createOsdRequest><name>test</name><parentId>1</parentId><ownerId>1</ownerId><aclId>1</aclId><typeId>1</typeId><formatId>1</formatId><languageId>1</languageId><lifecycleStateId/><metas/></createOsdRequest>" > /tmp/osd.xml
osd=$(curl --silent --show-error --header "ticket: ${TICKET}" -F "createOsdRequest=</tmp/osd.xml" "http://localhost:9090/api/osd/createOsd")
echo;
echo "created osd:"
echo "${osd}"
echo "sleep for 5"
echo;
sleep 5;

#echo " <BooleanQuery><Clause occurs='must'><PointRangeQuery fieldName='acl' lowerTerm='1' upperTerm='1' type='long'/></Clause></BooleanQuery>" > /tmp/booleanQuery.xml
#echo " <BooleanQuery><Clause occurs='must'><ExactPointQuery fieldName='id' value='26' type='long'/></Clause></BooleanQuery>" > /tmp/booleanQuery.xml
#echo " <BooleanQuery><Clause occurs='must'><TermQuery fieldName='name'>test</TermQuery></Clause></BooleanQuery>" > /tmp/booleanQuery.xml

# see: https://lucene.apache.org/core/9_4_2/core/org/apache/lucene/util/automaton/RegExp.html for explanation
#echo " <BooleanQuery><Clause occurs='must'><RegexQuery fieldName='name'>t..t</RegexQuery></Clause></BooleanQuery>" > /tmp/booleanQuery.xml
echo " <BooleanQuery><Clause occurs='must'><WildcardQuery fieldName='name'>t*t</WildcardQuery></Clause></BooleanQuery>" > /tmp/booleanQuery.xml
my_request=$(cat /tmp/booleanQuery.xml)
echo "search clause: ${my_request}"
echo;
encoded=$(sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g; s/"/\&quot;/g; s/'"'"'/\&#39;/g' < /tmp/booleanQuery.xml)

query="<searchIdsRequest>
                <searchType>OSD</searchType>
                <query>
                ${encoded}
                </query>
              </searchIdsRequest>"
echo "complete search request: ${query}"
echo;

curl -X POST --silent --show-error --header "ticket: ${TICKET}" --header "Content-type: application/xml" \
--data "${query}" "http://localhost:9090/api/search/objectIds"

echo;