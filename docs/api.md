# /api/aclGroup/create
Create a new ACL group. Note: permissions parameter is not yet implemented.

## Request

```xml
<createAclGroupRequest>
  <aclGroups>
    <aclGroup>
      <id/>
      <aclId>1</aclId>
      <groupId>2</groupId>
      <permissions/>
    </aclGroup>
    <aclGroup>
      <id/>
      <aclId>1</aclId>
      <groupId>3</groupId>
      <permissions/>
    </aclGroup>
  </aclGroups>
</createAclGroupRequest>

```


## Response

```xml
<aclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</aclGroup>

```
```xml
<aclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</aclGroup>

```


---

# /api/aclGroup/delete


## Request

```xml
<deleteAclGroupRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>5</id>
    <id>78</id>
  </ids>
</deleteAclGroupRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/aclGroup/list


## Request

```xml
<listAclGroupRequest>
  <type>FULL</type>
</listAclGroupRequest>

```


## Response

```xml
<aclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</aclGroup>

```
```xml
<aclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</aclGroup>

```


---

# /api/aclGroup/listByGroupOrAcl


## Request

```xml
<aclGroupListRequest>
  <id>1</id>
  <idType>GROUP</idType>
</aclGroupListRequest>

```
```xml
<aclGroupListRequest>
  <id>2</id>
  <idType>ACL</idType>
</aclGroupListRequest>

```


## Response

```xml
<aclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</aclGroup>

```
```xml
<aclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</aclGroup>

```


---

# /api/aclGroup/update
This replaces the existing permissions of the given AclGroup with the ones you send with the request.
It does not otherwise change the AclGroup (as it only contains of a group and acl reference - to change those, create a new AclGroup.


## Request

```xml
<updateAclGroupRequest>
  <aclGroups/>
</updateAclGroupRequest>

```


## Response

```xml
<aclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</aclGroup>

```
```xml
<aclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</aclGroup>

```


---

# /api/acl/aclInfo


## Request

```xml
<aclInfoRequest>
  <aclId/>
  <name/>
</aclInfoRequest>

```


## Response

```xml
<cinnamon>
  <acls/>
</cinnamon>

```


---

# /api/acl/create


## Request

```xml
<createAclRequest>
  <acls>
    <acl>
      <id/>
      <name>default acl</name>
    </acl>
    <acl>
      <id/>
      <name>reviewers</name>
    </acl>
    <acl>
      <id/>
      <name>authors</name>
    </acl>
  </acls>
</createAclRequest>

```


## Response

```xml
<cinnamon>
  <acls/>
</cinnamon>

```


---

# /api/acl/delete


## Request

```xml
<deleteAclRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteAclRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/acl/getUserAcls


## Request

```xml
<idRequest>
  <id/>
</idRequest>

```


## Response

```xml
<cinnamon>
  <acls/>
</cinnamon>

```


---

# /api/acl/list


## Request

```xml
<listAclRequest>
  <type>FULL</type>
</listAclRequest>

```


## Response

```xml
<cinnamon>
  <acls/>
</cinnamon>

```


---

# /api/acl/update


## Request

```xml
<updateAclRequest>
  <acls>
    <acl>
      <id>1</id>
      <name>updated-name</name>
    </acl>
  </acls>
</updateAclRequest>

```


## Response

```xml
<cinnamon>
  <acls/>
</cinnamon>

```


---

# /cinnamon/connect
Connect to the cinnamon server by sending a form-encoded username and password.

Example call:

    TICKET=$(curl --silent --show-error -X POST "http://localhost:9090/cinnamon/connect?user=admin&password=admin&format=text")

If you add the "format=text" parameter, you will receive a plain/text response with just the session ticket.
Otherwise, you will get:

    <connection><ticket>72ca5288-c802-4da7-9315-6881f5e593b5</ticket></connection>

The ticket is a session id that must be sent with all other requests to the server,
in the request header field "ticket".


---

# /cinnamon/disconnect
Disconnect from the cinnamon server by invalidating the session ticket.


---

# /cinnamon/info
Retrieve the server version and build number.


---

WARNING: sun.reflect.Reflection.getCallerClass is not supported. This will impact performance.
# /api/changeTrigger/create


## Request

```xml
<createChangeTriggerRequest>
  <changeTriggers>
    <changeTrigger>
      <id>1</id>
      <name>triggerThumbnailGenerator</name>
      <controller>osd</controller>
      <action>setContent</action>
      <active>true</active>
      <preTrigger>false</preTrigger>
      <postTrigger>true</postTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>100</ranking>
      <config>&lt;config>&lt;url>http://localhost:64888/createThumbnail&lt;/url>&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
</createChangeTriggerRequest>

```


## Response

```xml
<cinnamon>
  <changeTriggers/>
</cinnamon>

```


---

# /api/changeTrigger/delete


## Request

```xml
<deleteChangeTriggerRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteChangeTriggerRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/changeTrigger/list


## Request

```xml
<listChangeTriggerRequest>
  <type>FULL</type>
</listChangeTriggerRequest>

```


## Response

```xml
<cinnamon>
  <changeTriggers/>
</cinnamon>

```


---

# /api/changeTrigger/update


## Request

```xml
<updateChangeTriggerRequest>
  <changeTriggers>
    <changeTrigger>
      <id>1</id>
      <name>triggerThumbnailGenerator</name>
      <controller>osd</controller>
      <action>setContent</action>
      <active>true</active>
      <preTrigger>false</preTrigger>
      <postTrigger>true</postTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>100</ranking>
      <config>&lt;config>&lt;url>http://localhost:64888/createThumbnail&lt;/url>&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
</updateChangeTriggerRequest>

```


## Response

```xml
<cinnamon>
  <changeTriggers/>
</cinnamon>

```


---

# /api/configEntry/create
Create a new config entry

## Request

```xml
<createConfigEntryRequest>
  <configEntries>
    <configEntry>
      <id/>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</createConfigEntryRequest>

```
```xml
<createConfigEntryRequest>
  <configEntries>
    <configEntry>
      <id/>
      <name>render-server-password</name>
      <config>xxx</config>
      <publicVisibility>false</publicVisibility>
    </configEntry>
  </configEntries>
</createConfigEntryRequest>

```


## Response

```xml
<cinnamon>
  <configEntries>
    <configEntry>
      <id>1</id>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</cinnamon>

```


---

# /api/configEntry/delete
Delete a list of config entries

## Request

```xml
<DeleteConfigEntryRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteConfigEntryRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/configEntry/get
Retrieve a config entries by names or ids

## Request

```xml
<configEntryRequest>
  <ids>
    <id>123</id>
  </ids>
</configEntryRequest>

```
```xml
<configEntryRequest>
  <ids>
    <id>456</id>
  </ids>
</configEntryRequest>

```


## Response

```xml
<cinnamon>
  <configEntries>
    <configEntry>
      <id>1</id>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</cinnamon>

```


---

# /api/configEntry/list
List all config entries the current user is allowed to see (superuser: all, normal users: only those with public visibility)

## Request

```xml
<listConfigEntryRequest>
  <type>FULL</type>
</listConfigEntryRequest>

```


## Response

```xml
<cinnamon>
  <configEntries>
    <configEntry>
      <id>1</id>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</cinnamon>

```


---

# /api/configEntry/update
Update a list of config entries

## Request

```xml
<createConfigEntryRequest>
  <configEntries>
    <configEntry>
      <id>321</id>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</createConfigEntryRequest>

```
```xml
<createConfigEntryRequest>
  <configEntries>
    <configEntry>
      <id>444</id>
      <name>render-server-password</name>
      <config>xxx</config>
      <publicVisibility>false</publicVisibility>
    </configEntry>
  </configEntries>
</createConfigEntryRequest>

```


## Response

```xml
<cinnamon>
  <configEntries>
    <configEntry>
      <id>1</id>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</cinnamon>

```


---

# /api/config/listAllConfigurations
List of all objects the client may want to cache, for example users, object types, groups, permissions, languages etc.


## Request

```xml
<listConfigRequest>
  <type>FULL</type>
</listConfigRequest>

```


## Response

```xml
<cinnamon>
  <acls/>
  <folderTypes/>
  <formats/>
  <groups/>
  <indexItems/>
  <languages/>
  <lifecycles/>
  <metasetTypes/>
  <objectTypes/>
  <permissions/>
  <relationTypes/>
  <uiLanguages/>
  <users/>
  <providerClasses/>
</cinnamon>

```


---

# /api/config/urlMappings
List URL Mappings

## Request

```xml
<listUrlMappingInfoRequest>
  <type>FULL</type>
</listUrlMappingInfoRequest>

```


## Response

```xml
<cinnamon>
  <urlMappings>
    <urlMapping>
      <controller>test</controller>
      <action>echo</action>
      <path>/api/test/echo</path>
      <description>return the posted input xml</description>
    </urlMapping>
  </urlMappings>
</cinnamon>

```


---

# /api/folderType/create
Create a new folder type

## Request

```xml
<createFolderTypeRequest>
  <folderTypes>
    <folderType>
      <id/>
      <name>source</name>
    </folderType>
  </folderTypes>
</createFolderTypeRequest>

```
```xml
<createFolderTypeRequest>
  <folderTypes>
    <folderType>
      <id/>
      <name>temp</name>
    </folderType>
    <folderType>
      <id/>
      <name>bin</name>
    </folderType>
  </folderTypes>
</createFolderTypeRequest>

```


## Response

```xml
<cinnamon>
  <folderTypes/>
</cinnamon>

```


---

# /api/folderType/delete
Delete a folder type

## Request

```xml
<deleteFolderTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteFolderTypeRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/folderType/list
List all folder types

## Request

```xml
<listFolderTypeRequest>
  <type>FULL</type>
</listFolderTypeRequest>

```


## Response

```xml
<cinnamon>
  <folderTypes/>
</cinnamon>

```


---

# /api/folderType/update


## Request

```xml
<updateFolderTypeRequest>
  <folderTypes/>
</updateFolderTypeRequest>

```


## Response

```xml
<cinnamon>
  <folderTypes/>
</cinnamon>

```


---

# /api/folder/create
Create a new folder.

## Request

```xml
<createFolderRequest>
  <folders>
    <folder>
      <id/>
      <name>images</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>4</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary>&lt;description>contains images&lt;/description>&lt;/summary></summary>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T01:21:00+0000</created>
    </folder>
    <folder>
      <id/>
      <name>archive</name>
      <aclId>2</aclId>
      <ownerId>2</ownerId>
      <parentId>2</parentId>
      <typeId>2</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary/></summary>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T01:21:00+0000</created>
    </folder>
  </folders>
</createFolderRequest>

```


## Response



---

# /api/folder/createMeta


## Request

```xml
<createMetaRequest>
  <metasets>
    <metaset>
      <id/>
      <objectId>32</objectId>
      <typeId>3</typeId>
      <content>&lt;xml>some meta&lt;/xml></content>
    </metaset>
  </metasets>
</createMetaRequest>

```
```xml
<createMetaRequest>
  <metasets>
    <metaset>
      <id/>
      <objectId>40</objectId>
      <typeId>10</typeId>
      <content>&lt;meta>metadata&lt;/meta></content>
    </metaset>
  </metasets>
</createMetaRequest>

```


## Response

```xml
<cinnamon>
  <metasets/>
</cinnamon>

```


---

# /api/folder/delete


## Request

```xml
<deleteFolderRequest>
  <deleteRecursively>true</deleteRecursively>
  <deleteContent>false</deleteContent>
  <ids>
    <id>1</id>
    <id>2</id>
    <id>3</id>
  </ids>
</deleteFolderRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/folder/deleteAllMetas
Delete all metasets linked to the given Folder ids. Parameter ignoreNotFound is not used.

## Request

```xml
<deleteAllMetasRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>14</id>
    <id>15</id>
  </ids>
</deleteAllMetasRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/folder/deleteMeta
Delete the folder metasets with the given meta ids.

## Request

```xml
<deleteMetaRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>3</id>
    <id>5</id>
    <id>6</id>
  </ids>
</deleteMetaRequest>

```
```xml
<deleteMetaRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>1</id>
  </ids>
</deleteMetaRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/folder/getFolder
Fetch a single folder

## Request

```xml
<singleFolderRequest>
  <id>123</id>
  <includeSummary>true</includeSummary>
</singleFolderRequest>

```
```xml
<singleFolderRequest>
  <id>321</id>
  <includeSummary>false</includeSummary>
</singleFolderRequest>

```


## Response

```xml
<cinnamon>
  <folders/>
</cinnamon>

```


---

# /api/folder/getFolders


## Request

```xml
<folderRequest>
  <includeSummary>false</includeSummary>
  <ids/>
</folderRequest>

```


## Response

```xml
<cinnamon>
  <folders/>
</cinnamon>

```


---

# /api/folder/getFolderByPath


## Request

```xml
<folderPathRequest>
  <path>/home/creation/some-sub-folder</path>
  <includeSummary>true</includeSummary>
</folderPathRequest>

```


## Response

```xml
<cinnamon>
  <folders/>
</cinnamon>

```


---

# /api/folder/getMeta


## Request

```xml
<metaRequest>
  <id>3</id>
  <typeIds>
    <typeId>12</typeId>
    <typeId>13</typeId>
  </typeIds>
</metaRequest>

```
```xml
<metaRequest>
  <id>1</id>
</metaRequest>

```


## Response

```xml
<cinnamon>
  <metasets/>
</cinnamon>

```


---

# /api/folder/getSubFolders


## Request

```xml
<singleFolderRequest>
  <id>123</id>
  <includeSummary>true</includeSummary>
</singleFolderRequest>

```
```xml
<singleFolderRequest>
  <id>321</id>
  <includeSummary>false</includeSummary>
</singleFolderRequest>

```


## Response

```xml
<cinnamon>
  <folders/>
</cinnamon>

```


---

# /api/folder/getSummaries


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>44</id>
    <id>5</id>
  </ids>
</idListRequest>

```


## Response

```xml
<cinnamon>
  <summaries/>
</cinnamon>

```


---

# /api/folder/setSummary
Add a summary to an object, for example a short description of this folder's content. Currently single-folder-API.

## Request

```xml
<setSummaryRequest>
  <id>45</id>
  <summary>&lt;xml>summary&lt;/xml></summary>
</setSummaryRequest>

```
```xml
<setSummaryRequest>
  <id>65</id>
  <summary>be careful when indexing non-xml summaries</summary>
</setSummaryRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/folder/update


## Request

```xml
<updateFolderRequest>
  <updateMetadataChanged>false</updateMetadataChanged>
  <folders>
    <folder>
      <id/>
      <name>new name</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>4</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary>update this&lt;/summary></summary>
      <hasSubfolders>false</hasSubfolders>
      <created>2023-10-27T14:36:32+0000</created>
    </folder>
  </folders>
</updateFolderRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/folder/updateMetaContent
Update the content of a given folder metaset

## Request

```xml
<updateMetaRequest>
  <metasets>
    <metaset>
      <id>123</id>
      <objectId>1</objectId>
      <typeId>2</typeId>
      <content>meta content update</content>
    </metaset>
  </metasets>
</updateMetaRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/format/create


## Request

```xml
<createFormatRequest>
  <formats/>
</createFormatRequest>

```


## Response

```xml
<cinnamon>
  <formats/>
</cinnamon>

```


---

# /api/format/delete


## Request

```xml
<deleteFormatRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteFormatRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/format/list


## Request

```xml
<listFormatRequest>
  <type>FULL</type>
</listFormatRequest>

```


## Response

```xml
<cinnamon>
  <formats/>
</cinnamon>

```


---

# /api/format/update


## Request

```xml
<updateFormatRequest>
  <formats/>
</updateFormatRequest>

```


## Response

```xml
<cinnamon>
  <formats/>
</cinnamon>

```


---

# /api/group/addUserToGroups


## Request

```xml
<addUserToGroupsRequest>
  <userId>33</userId>
  <groupIds>
    <groupId>1</groupId>
    <groupId>2</groupId>
    <groupId>3</groupId>
  </groupIds>
</addUserToGroupsRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/group/create


## Request

```xml
<createGroupRequest>
  <groups>
    <group>
      <id/>
      <name>authors</name>
      <parentId>1</parentId>
    </group>
    <group>
      <id/>
      <name>reviewers</name>
      <parentId>1</parentId>
    </group>
    <group>
      <id/>
      <name>admins</name>
      <parentId/>
    </group>
  </groups>
</createGroupRequest>

```


## Response

```xml
<cinnamon>
  <groups>
    <group>
      <id>1</id>
      <name>group with parent</name>
      <parentId>2</parentId>
    </group>
    <group>
      <id>2</id>
      <name>group without parent</name>
      <parentId/>
    </group>
  </groups>
</cinnamon>

```


---

# /api/group/delete


## Request

```xml
<deleteGroupRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <recursive>true</recursive>
  <ids>
    <id>4</id>
    <id>6</id>
    <id>7</id>
  </ids>
</deleteGroupRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/group/list


## Request

```xml
<listGroupRequest>
  <type>FULL</type>
</listGroupRequest>

```


## Response

```xml
<cinnamon>
  <groups>
    <group>
      <id>1</id>
      <name>group with parent</name>
      <parentId>2</parentId>
    </group>
    <group>
      <id>2</id>
      <name>group without parent</name>
      <parentId/>
    </group>
  </groups>
</cinnamon>

```


---

# /api/group/removeUserFromGroups


## Request

```xml
<removeUserFromGroupsRequest>
  <userId>33</userId>
  <groupIds>
    <groupId>1</groupId>
    <groupId>2</groupId>
    <groupId>3</groupId>
  </groupIds>
</removeUserFromGroupsRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/group/update


## Request

```xml
<updateGroupRequest>
  <groups>
    <group>
      <id>11</id>
      <name>updated-group-name</name>
      <parentId>2</parentId>
    </group>
  </groups>
</updateGroupRequest>

```


## Response

```xml
<cinnamon>
  <groups>
    <group>
      <id>1</id>
      <name>group with parent</name>
      <parentId>2</parentId>
    </group>
    <group>
      <id>2</id>
      <name>group without parent</name>
      <parentId/>
    </group>
  </groups>
</cinnamon>

```


---

# /api/indexItem/create


## Request

```xml
<createIndexItemRequest>
  <indexItems>
    <indexItem>
      <id/>
      <name>Titles</name>
      <fieldName>title</fieldName>
      <searchString>//title/text()</searchString>
      <searchCondition>true()</searchCondition>
      <multipleResults>true</multipleResults>
      <indexType>DEFAULT_INDEXER</indexType>
      <storeField>false</storeField>
    </indexItem>
  </indexItems>
</createIndexItemRequest>

```


## Response

```xml
<indexItem>
  <id>43</id>
  <name>Titles</name>
  <fieldName>title</fieldName>
  <searchString>//title/text()</searchString>
  <searchCondition>true()</searchCondition>
  <multipleResults>true</multipleResults>
  <indexType>DEFAULT_INDEXER</indexType>
  <storeField>false</storeField>
</indexItem>

```


---

# /api/indexItem/delete


## Request

```xml
<deleteIndexItemRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteIndexItemRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/indexItem/list


## Request

```xml
<listIndexItemRequest>
  <type>FULL</type>
</listIndexItemRequest>

```


## Response

```xml
<indexItem>
  <id>43</id>
  <name>Titles</name>
  <fieldName>title</fieldName>
  <searchString>//title/text()</searchString>
  <searchCondition>true()</searchCondition>
  <multipleResults>true</multipleResults>
  <indexType>DEFAULT_INDEXER</indexType>
  <storeField>false</storeField>
</indexItem>

```


---

# /api/indexItem/update


## Request

```xml
<updateIndexItemRequest>
  <indexItems/>
</updateIndexItemRequest>

```


## Response

```xml
<indexItem>
  <id>43</id>
  <name>Titles</name>
  <fieldName>title</fieldName>
  <searchString>//title/text()</searchString>
  <searchCondition>true()</searchCondition>
  <multipleResults>true</multipleResults>
  <indexType>DEFAULT_INDEXER</indexType>
  <storeField>false</storeField>
</indexItem>

```


---

# /api/index/info
Provides information on the status of the Lucene search index

## Request

```xml
<indexInfoRequest>
  <countDocuments>true</countDocuments>
</indexInfoRequest>

```


## Response

```xml
<indexInfoResponse>
  <documentsInIndex>100</documentsInIndex>
  <foldersInIndex>20</foldersInIndex>
  <failedJobCount>2</failedJobCount>
  <jobCount>41</jobCount>
</indexInfoResponse>

```


---

# /api/index/reindex
Rebuild the Lucene search index in parts or completely. When reindexing large numbers of documents, this will affect the system's performance.

## Request

```xml
<reindexRequest>
  <osdIds/>
  <folderIds/>
  <updateTikaMetaset>false</updateTikaMetaset>
</reindexRequest>

```
```xml
<reindexRequest>
  <osdIds>
    <osdIds>13</osdIds>
    <osdIds>23</osdIds>
  </osdIds>
  <folderIds>
    <folderIds>43</folderIds>
    <folderIds>2</folderIds>
  </folderIds>
  <updateTikaMetaset>false</updateTikaMetaset>
</reindexRequest>

```


## Response

```xml
<reindexResponse>
  <documentsToIndex>1000</documentsToIndex>
  <foldersToIndex>123</foldersToIndex>
</reindexResponse>

```


---

# /api/language/create


## Request

```xml
<createLanguageRequest>
  <languages>
    <language>
      <id/>
      <isoCode>en</isoCode>
    </language>
    <language>
      <id/>
      <isoCode>de</isoCode>
    </language>
    <language>
      <id/>
      <isoCode>fr</isoCode>
    </language>
  </languages>
</createLanguageRequest>

```


## Response

```xml
<cinnamon>
  <languages/>
</cinnamon>

```


---

# /api/language/delete


## Request

```xml
<deleteLanguageRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteLanguageRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/language/list


## Request

```xml
<listLanguageRequest>
  <type>FULL</type>
</listLanguageRequest>

```


## Response

```xml
<cinnamon>
  <languages/>
</cinnamon>

```


---

# /api/language/update


## Request

```xml
<updateLanguageRequest>
  <languages>
    <language>
      <id>53</id>
      <isoCode>new-isoCode-for-language</isoCode>
    </language>
  </languages>
</updateLanguageRequest>

```


## Response

```xml
<cinnamon>
  <languages/>
</cinnamon>

```


---

# /api/lifecycleState/attachLifecycle
Only superusers may use forceChange parameter to attach any state without verification.

## Request

```xml
<attachLifecycleRequest>
  <osdId/>
  <lifecycleId/>
  <lifecycleStateId/>
  <forceChange>false</forceChange>
</attachLifecycleRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/lifecycleState/changeState


## Request

```xml
<changeLifecycleStateRequest>
  <osdId/>
  <stateId/>
</changeLifecycleStateRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/lifecycleState/create


## Request

```xml
<createLifecycleStateRequest>
  <lifecycleStates>
    <lifecycleState>
      <id/>
      <name>review-state</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.NopState</stateClass>
      <lifecycleId>1</lifecycleId>
      <lifecycleStateForCopyId/>
    </lifecycleState>
    <lifecycleState>
      <id/>
      <name>authoring-state</name>
      <config>&lt;config>&lt;properties>&lt;property>&lt;name>aclName&lt;/name>&lt;value>_default_acl&lt;/value>&lt;/property>&lt;/properties>&lt;nextStates>&lt;name>review-state&lt;/name>&lt;/nextStates>&lt;/config></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>2</lifecycleId>
      <lifecycleStateForCopyId>3</lifecycleStateForCopyId>
    </lifecycleState>
  </lifecycleStates>
</createLifecycleStateRequest>

```


## Response

```xml
<cinnamon>
  <lifecycleStates/>
</cinnamon>

```


---

# /api/lifecycleState/delete


## Request

```xml
<deleteLifecycleStateRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteLifecycleStateRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/lifecycleState/detachLifecycle


## Request

```xml
<idRequest>
  <id/>
</idRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/lifecycleState/get


## Request

```xml
<idRequest>
  <id/>
</idRequest>

```


## Response

```xml
<cinnamon>
  <lifecycleStates/>
</cinnamon>

```


---

# /api/lifecycleState/getNextStates


## Request

```xml
<idRequest>
  <id/>
</idRequest>

```


## Response

```xml
<cinnamon>
  <lifecycleStates/>
</cinnamon>

```


---

# /api/lifecycleState/list


## Request

```xml
<listLifecycleStateRequest>
  <type>FULL</type>
</listLifecycleStateRequest>

```


## Response

```xml
<cinnamon>
  <lifecycleStates/>
</cinnamon>

```


---

# /api/lifecycleState/update


## Request

```xml
<updateLifecycleStateRequest>
  <lifecycleStates>
    <lifecycleState>
      <id>232</id>
      <name>review-state-update</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>1</lifecycleId>
      <lifecycleStateForCopyId/>
    </lifecycleState>
  </lifecycleStates>
</updateLifecycleStateRequest>

```


## Response

```xml
<cinnamon>
  <lifecycleStates/>
</cinnamon>

```


---

# /api/lifecycle/create
Create lifecycles. Note: does not create lifecycle states, defaultStateId should be empty.

## Request

```xml
<createLifecycleRequest>
  <lifecycles>
    <lifecycle>
      <id/>
      <name>authoring</name>
      <defaultStateId/>
      <lifecycleStates/>
    </lifecycle>
    <lifecycle>
      <id/>
      <name>translation-lc</name>
      <defaultStateId/>
      <lifecycleStates/>
    </lifecycle>
  </lifecycles>
</createLifecycleRequest>

```


## Response

```xml
<cinnamon>
  <lifecycles/>
</cinnamon>

```


---

# /api/lifecycle/delete
Delete lifecycles 

## Request

```xml
<deleteLifecycleRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteLifecycleRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/lifecycle/get


## Request

```xml
<lifecycleRequest>
  <id/>
  <name/>
</lifecycleRequest>

```


## Response

```xml
<cinnamon>
  <lifecycles/>
</cinnamon>

```


---

# /api/lifecycle/list
List lifecycles 

## Request

```xml
<listLifecycleRequest>
  <type>FULL</type>
</listLifecycleRequest>

```


## Response

```xml
<cinnamon>
  <lifecycles/>
</cinnamon>

```


---

# /api/lifecycle/update
Update lifecycles. Note: does not update lifecycle states 

## Request

```xml
<updateLifecycleRequest>
  <lifecycles/>
</updateLifecycleRequest>

```


## Response

```xml
<cinnamon>
  <lifecycles/>
</cinnamon>

```


---

# /api/link/create


## Request

```xml
<createLinkRequest>
  <links/>
</createLinkRequest>

```


## Response

```xml
<link>
  <id>1</id>
  <type>OBJECT</type>
  <ownerId>2</ownerId>
  <aclId>3</aclId>
  <parentId>4</parentId>
  <folderId/>
  <objectId>123</objectId>
</link>

```
```xml
<link>
  <id>1</id>
  <type>FOLDER</type>
  <ownerId>2</ownerId>
  <aclId>3</aclId>
  <parentId>4</parentId>
  <folderId>321</folderId>
  <objectId/>
</link>

```


---

# /api/link/delete


## Request

```xml
<deleteLinkRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteLinkRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/link/getLinksById


## Request

```xml
<getLinksRequest>
  <includeSummary>false</includeSummary>
  <ids/>
</getLinksRequest>

```


## Response

```xml
<cinnamon>
  <links/>
</cinnamon>

```


---

# /api/link/update


## Request

```xml
<updateLinkRequest>
  <links>
    <link>
      <id>1</id>
      <type>OBJECT</type>
      <ownerId>2</ownerId>
      <aclId>3</aclId>
      <parentId>4</parentId>
      <folderId>5</folderId>
      <objectId>6</objectId>
    </link>
  </links>
</updateLinkRequest>

```


## Response

```xml
<link>
  <id>1</id>
  <type>OBJECT</type>
  <ownerId>2</ownerId>
  <aclId>3</aclId>
  <parentId>4</parentId>
  <folderId/>
  <objectId>123</objectId>
</link>

```
```xml
<link>
  <id>1</id>
  <type>FOLDER</type>
  <ownerId>2</ownerId>
  <aclId>3</aclId>
  <parentId>4</parentId>
  <folderId>321</folderId>
  <objectId/>
</link>

```


---

# /api/metasetType/create


## Request

```xml
<createMetasetTypeRequest>
  <metasetTypes/>
</createMetasetTypeRequest>

```


## Response

```xml
<cinnamon>
  <metasetTypes/>
</cinnamon>

```


---

# /api/metasetType/delete


## Request

```xml
<deleteMetasetTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteMetasetTypeRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/metasetType/list


## Request

```xml
<listMetasetTypeRequest>
  <type>FULL</type>
</listMetasetTypeRequest>

```


## Response

```xml
<cinnamon>
  <metasetTypes/>
</cinnamon>

```


---

# /api/metasetType/update


## Request

```xml
<updateMetasetTypeRequest>
  <metasetTypes/>
</updateMetasetTypeRequest>

```


## Response

```xml
<cinnamon>
  <metasetTypes/>
</cinnamon>

```


---

# /api//


---

# /api/objectType/create


## Request

```xml
<createObjectTypeRequest>
  <objectTypes>
    <objectType>
      <id/>
      <name>default type</name>
    </objectType>
    <objectType>
      <id/>
      <name>other type</name>
    </objectType>
  </objectTypes>
</createObjectTypeRequest>

```


## Response

```xml
<cinnamon>
  <objectTypes/>
</cinnamon>

```


---

# /api/objectType/delete


## Request

```xml
<deleteObjectTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteObjectTypeRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/objectType/list


## Request

```xml
<listObjectTypeRequest>
  <type>FULL</type>
</listObjectTypeRequest>

```


## Response

```xml
<cinnamon>
  <objectTypes/>
</cinnamon>

```


---

# /api/objectType/update


## Request

```xml
<updateObjectTypeRequest>
  <objectTypes>
    <objectType>
      <id>123</id>
      <name>updated-object-type-name</name>
    </objectType>
  </objectTypes>
</updateObjectTypeRequest>

```


## Response

```xml
<cinnamon>
  <objectTypes/>
</cinnamon>

```


---

# /api/osd/copy


## Request

```xml
<copyOsdRequest>
  <targetFolderId>20</targetFolderId>
  <sourceIds>
    <sourceId>1</sourceId>
    <sourceId>2</sourceId>
    <sourceId>3</sourceId>
  </sourceIds>
  <metasetTypeIds>
    <metasetTypeId>13</metasetTypeId>
    <metasetTypeId>15</metasetTypeId>
    <metasetTypeId>2</metasetTypeId>
  </metasetTypeIds>
</copyOsdRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
  <references/>
  <relations/>
</cinnamon>

```


---

# /api/osd/createMeta


## Request

```xml
<createMetaRequest>
  <metasets>
    <metaset>
      <id/>
      <objectId>32</objectId>
      <typeId>3</typeId>
      <content>&lt;xml>some meta&lt;/xml></content>
    </metaset>
  </metasets>
</createMetaRequest>

```
```xml
<createMetaRequest>
  <metasets>
    <metaset>
      <id/>
      <objectId>40</objectId>
      <typeId>10</typeId>
      <content>&lt;meta>metadata&lt;/meta></content>
    </metaset>
  </metasets>
</createMetaRequest>

```


## Response

```xml
<cinnamon>
  <metasets/>
</cinnamon>

```


---

# /api/osd/createOsd
Create a new OSD. Requires: this must be a multipart-mime request, with part "cinnamonRequest" and optional part "file" if this object
should contain data.


## Request

```xml
<createOsdRequest>
  <name>create OSD request must be sent via multipart-request</name>
  <parentId>1</parentId>
  <ownerId>23</ownerId>
  <aclId>44</aclId>
  <typeId>2</typeId>
  <formatId>3</formatId>
  <languageId>1</languageId>
  <lifecycleStateId/>
  <summary>&lt;summary>Optional fields: typeId, aclId, ownerId, formatId, languageId, summary&lt;/summary></summary>
  <metas/>
</createOsdRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
  <references/>
  <relations/>
</cinnamon>

```


---

# /api/osd/delete


## Request

```xml
<deleteOsdRequest>
  <deleteDescendants>false</deleteDescendants>
  <deleteAllVersions>false</deleteAllVersions>
  <ids/>
</deleteOsdRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/osd/deleteAllMetas
    Delete all metasets linked to the given OSD ids. Parameter ignoreNotFound is not used.


## Request

```xml
<deleteAllMetasRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>14</id>
    <id>15</id>
  </ids>
</deleteAllMetasRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/osd/deleteMeta
Delete the OSD metasets with the given meta ids.

## Request

```xml
<deleteMetaRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>3</id>
    <id>5</id>
    <id>6</id>
  </ids>
</deleteMetaRequest>

```
```xml
<deleteMetaRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <id>1</id>
  </ids>
</deleteMetaRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/osd/getContent
Returns an OSD's content according to it's format's content type.

## Request

```xml
<idRequest>
  <id/>
</idRequest>

```


## Response



---

# /api/osd/getMeta


## Request

```xml
<metaRequest>
  <id>3</id>
  <typeIds>
    <typeId>12</typeId>
    <typeId>13</typeId>
  </typeIds>
</metaRequest>

```
```xml
<metaRequest>
  <id>1</id>
</metaRequest>

```


## Response

```xml
<cinnamon>
  <metasets/>
</cinnamon>

```


---

# /api/osd/getObjectsByFolderId


## Request

```xml
<osdByFolderRequest>
  <includeSummary>false</includeSummary>
  <folderId>0</folderId>
  <linksAsOsd>true</linksAsOsd>
  <includeCustomMetadata>false</includeCustomMetadata>
  <versionPredicate>HEAD</versionPredicate>
</osdByFolderRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
  <references/>
  <relations/>
</cinnamon>

```


---

# /api/osd/getObjectsById


## Request

```xml
<osdRequest>
  <includeSummary>true</includeSummary>
  <includeCustomMetadata>true</includeCustomMetadata>
  <ids>
    <id>45</id>
    <id>23</id>
    <id>2</id>
  </ids>
</osdRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
  <references/>
  <relations/>
</cinnamon>

```


---

# /api/osd/getRelations


## Request

```xml
<getRelationsRequest>
  <ids>
    <ids>1</ids>
    <ids>32</ids>
    <ids>4</ids>
  </ids>
  <includeMetadata>false</includeMetadata>
</getRelationsRequest>

```


## Response

```xml
<relation>
  <id>399</id>
  <leftId>1</leftId>
  <rightId>4</rightId>
  <typeId>1</typeId>
  <metadata>&lt;generatedBy>PDF Renderer&lt;/generatedBy</metadata>
</relation>

```


---

# /api/osd/getSummaries


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>44</id>
    <id>5</id>
  </ids>
</idListRequest>

```


## Response

```xml
<cinnamon>
  <summaries/>
</cinnamon>

```


---

# /api/osd/lock


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>44</id>
    <id>5</id>
  </ids>
</idListRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/osd/setContent
Set an OSD's content. Requires a multipart-mime request, with part "cinnamonRequest" and part "file".


## Request

```xml
<setContentRequest>
  <id/>
  <formatId/>
</setContentRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/osd/setSummary
Add a summary to an object, for example a short description of the content.

## Request

```xml
<setSummaryRequest>
  <id>45</id>
  <summary>&lt;xml>summary&lt;/xml></summary>
</setSummaryRequest>

```
```xml
<setSummaryRequest>
  <id>65</id>
  <summary>be careful when indexing non-xml summaries</summary>
</setSummaryRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/osd/unlock


## Request

```xml
<idRequest>
  <id/>
</idRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

