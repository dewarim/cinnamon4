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
  <ids/>
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
  <id/>
  <idType/>
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


## Request

```xml
<updateAclGroupRequest>
  <aclGroups>
    <aclGroup>
      <id>1345</id>
      <aclId>54</aclId>
      <groupId>4</groupId>
      <permissions/>
    </aclGroup>
  </aclGroups>
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


---

# /cinnamon/disconnect
Disconnect from the cinnamon server by invalidating the session ticket.


---

# /cinnamon/info
Retrieve the server version and build number.


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
  <names/>
  <ids>
    <id>123</id>
  </ids>
</configEntryRequest>

```
```xml
<configEntryRequest>
  <names>
    <name>default-ui-settings</name>
  </names>
  <ids/>
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
  <metas>
    <metas>
      <id/>
      <objectId>32</objectId>
      <typeId>3</typeId>
      <content>&lt;xml>some meta&lt;/xml></content>
    </metas>
  </metas>
</createMetaRequest>

```
```xml
<createMetaRequest>
  <metas>
    <metas>
      <id/>
      <objectId>40</objectId>
      <typeId>10</typeId>
      <content>&lt;meta>metadata&lt;/meta></content>
    </metas>
  </metas>
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
  <ids>
    <ids>1</ids>
    <ids>2</ids>
    <ids>3</ids>
  </ids>
  <deleteRecursively>true</deleteRecursively>
  <deleteContent>false</deleteContent>
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
    <ids>14</ids>
    <ids>15</ids>
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
    <ids>3</ids>
    <ids>5</ids>
    <ids>6</ids>
  </ids>
</deleteMetaRequest>

```
```xml
<deleteMetaRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <ids>1</ids>
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
  <ids/>
  <includeSummary>false</includeSummary>
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
<idListRequest/>

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
  <id/>
  <parentId/>
  <name/>
  <ownerId/>
  <typeId/>
  <aclId/>
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
  <metas>
    <meta>
      <id>123</id>
      <objectId>1</objectId>
      <typeId>2</typeId>
      <content>meta content update</content>
    </meta>
  </metas>
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
  <ids/>
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
    <lifecycleStates>
      <id>232</id>
      <name>review-state-update</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>1</lifecycleId>
      <lifecycleStateForCopyId/>
    </lifecycleStates>
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
    <objectTypes>
      <id>123</id>
      <name>updated-object-type-name</name>
    </objectTypes>
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
  <metas>
    <metas>
      <id/>
      <objectId>32</objectId>
      <typeId>3</typeId>
      <content>&lt;xml>some meta&lt;/xml></content>
    </metas>
  </metas>
</createMetaRequest>

```
```xml
<createMetaRequest>
  <metas>
    <metas>
      <id/>
      <objectId>40</objectId>
      <typeId>10</typeId>
      <content>&lt;meta>metadata&lt;/meta></content>
    </metas>
  </metas>
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
Create a new OSD. Requires: this must be a multipart-mime request, with part "createOsdRequest" and optional part "file" if this object
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
  <ids/>
  <deleteDescendants>false</deleteDescendants>
  <deleteAllVersions>false</deleteAllVersions>
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
    <ids>14</ids>
    <ids>15</ids>
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
    <ids>3</ids>
    <ids>5</ids>
    <ids>6</ids>
  </ids>
</deleteMetaRequest>

```
```xml
<deleteMetaRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids>
    <ids>1</ids>
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
  <ids/>
  <includeSummary>false</includeSummary>
  <includeCustomMetadata>false</includeCustomMetadata>
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
<idListRequest/>

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

# /api/osd/setContent
Set an OSD's content. Requires a multipart-mime request, with part "setContentRequest" and part "file".


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

# /api/osd/update


## Request

```xml
<updateOsdRequest>
  <id/>
  <parentFolderId/>
  <name/>
  <ownerId/>
  <aclId/>
  <objectTypeId/>
  <languageId/>
</updateOsdRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/osd/updateMetaContent
Update the content of a given OSD metaset

## Request

```xml
<updateMetaRequest>
  <metas>
    <meta>
      <id>123</id>
      <objectId>1</objectId>
      <typeId>2</typeId>
      <content>meta content update</content>
    </meta>
  </metas>
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

# /api/osd/version
Create a new version of an OSD. Requires a multipart-mime request, with part "createNewVersionRequest" and optional
part "file", if the new version should contain data.


## Request

```xml
<createNewVersionRequest>
  <id/>
  <metaRequests/>
  <formatId/>
</createNewVersionRequest>

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

# /api/permission/changePermissions


## Request

```xml
<changePermissionsRequest>
  <aclGroupId/>
  <add/>
  <remove/>
</changePermissionsRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/permission/getUserPermissions


## Request

```xml
<userPermissionRequest>
  <userId/>
  <aclId/>
</userPermissionRequest>

```


## Response

```xml
<cinnamon>
  <permissions/>
</cinnamon>

```


---

# /api/permission/list


## Request

```xml
<listPermissionRequest>
  <type>FULL</type>
</listPermissionRequest>

```


## Response

```xml
<cinnamon>
  <permissions/>
</cinnamon>

```


---

# /api/relationType/create


## Request

```xml
<createRelationTypeRequest>
  <relationTypes>
    <relationType>
      <id/>
      <leftObjectProtected>true</leftObjectProtected>
      <rightObjectProtected>false</rightObjectProtected>
      <name>thumbnail-relation</name>
      <cloneOnRightCopy>true</cloneOnRightCopy>
      <cloneOnLeftCopy>false</cloneOnLeftCopy>
      <cloneOnLeftVersion>true</cloneOnLeftVersion>
      <cloneOnRightVersion>false</cloneOnRightVersion>
    </relationType>
  </relationTypes>
</createRelationTypeRequest>

```


## Response

```xml
<cinnamon>
  <relationTypes/>
</cinnamon>

```


---

# /api/relationType/delete


## Request

```xml
<deleteRelationTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteRelationTypeRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/relationType/list


## Request

```xml
<listRelationTypeRequest>
  <type>FULL</type>
</listRelationTypeRequest>

```


## Response

```xml
<cinnamon>
  <relationTypes/>
</cinnamon>

```


---

# /api/relationType/update


## Request

```xml
<updateRelationTypeRequest>
  <relationTypes>
    <relationType>
      <id/>
      <leftObjectProtected>true</leftObjectProtected>
      <rightObjectProtected>true</rightObjectProtected>
      <name>updated-type</name>
      <cloneOnRightCopy>true</cloneOnRightCopy>
      <cloneOnLeftCopy>true</cloneOnLeftCopy>
      <cloneOnLeftVersion>true</cloneOnLeftVersion>
      <cloneOnRightVersion>true</cloneOnRightVersion>
    </relationType>
  </relationTypes>
</updateRelationTypeRequest>

```


## Response

```xml
<cinnamon>
  <relationTypes/>
</cinnamon>

```


---

# /api/relation/create


## Request

```xml
<createRelationRequest>
  <relations>
    <relation>
      <id/>
      <leftId>1</leftId>
      <rightId>2</rightId>
      <typeId>3</typeId>
      <metadata>&lt;meta/></metadata>
    </relation>
  </relations>
</createRelationRequest>

```
```xml
<createRelationRequest>
  <relations>
    <relation>
      <id/>
      <leftId>2</leftId>
      <rightId>1</rightId>
      <typeId>10</typeId>
      <metadata>&lt;xml>test&lt;/xml></metadata>
    </relation>
  </relations>
</createRelationRequest>

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

# /api/relation/delete


## Request

```xml
<deleteRelationRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteRelationRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/relation/search
Search for relations matching all( with orMode=false) or some (with orMode=true) criteria. Fields are optional, but at least one field must contain criteria.

## Request

```xml
<searchRelationRequest>
  <leftIds>
    <leftIds>1</leftIds>
    <leftIds>2</leftIds>
    <leftIds>3</leftIds>
  </leftIds>
  <rightIds>
    <rightIds>4</rightIds>
    <rightIds>5</rightIds>
    <rightIds>6</rightIds>
  </rightIds>
  <relationTypeIds>
    <relationTypeIds>2</relationTypeIds>
  </relationTypeIds>
  <includeMetadata>true</includeMetadata>
  <orMode>true</orMode>
</searchRelationRequest>

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

# /api/search/objectIds
Search the Lucene index for objects (documents and folders) matching the given query
and return the ids of all objects found which are browsable for the current user.

Systemic fields contain metadata that will always be indexed.

## Systemic Folder Fields

Field of objects contain the id, so acl field will index the acl.id.

* folderpath
* acl
* id
* created
* name
* owner
* parent (id of parent folder, empty if root folder)
* summary
* type

## Systemic OSD Fields

* folderpath
* acl
* cmn_version
* content_changed
* content_size
* created (date)
* modified (date)
* creator
* modifier
* format
* language
* latest_branch
* latest_head
* locker (id of user who placed a lock on the object, if any)
* metadata_changed
* name
* owner
* parent (id of parent folder)
* predecessor (id of previous version, null if this is the first object in tree)
* root (id of the first object in this object's version tree)
* lifecycle_state
* summary
* type

### Fields only indexed for objects with content
* content_size
* format


## Request

```xml
<searchIdsRequest>
  <searchType>OSD</searchType>
  <query>&lt;BooleanQuery>&lt;Clause occurs='must'>&lt;TermQuery fieldName='name'>test&lt;/TermQuery>&lt;/Clause>&lt;/BooleanQuery></query>
</searchIdsRequest>

```
```xml
<searchIdsRequest>
  <searchType>FOLDER</searchType>
  <query>&lt;BooleanQuery>&lt;Clause occurs='must'>&lt;TermQuery fieldName='acl'>123&lt;/TermQuery>&lt;/Clause>&lt;/BooleanQuery></query>
</searchIdsRequest>

```
```xml
<searchIdsRequest>
  <searchType>ALL</searchType>
  <query>&lt;BooleanQuery>&lt;Clause occurs='must'>&lt;TermQuery fieldName='owner'>1337&lt;/TermQuery>&lt;/Clause>&lt;/BooleanQuery></query>
</searchIdsRequest>

```


## Response

```xml
<searchIdsResponse>
  <osdIds>
    <osdId>1</osdId>
    <osdId>32</osdId>
  </osdIds>
  <folderIds>
    <folderId>100</folderId>
    <folderId>200</folderId>
  </folderIds>
</searchIdsResponse>

```


---

# /static/
Returns a static file from the server (for example, a favicon.ico if one exists).

---

# /test/status200
Returns status code 200

---

# /test/status400
Returns status code 400

---

# /api/uiLanguage/create


## Request

```xml
<createUiLanguageRequest>
  <uiLanguages>
    <uiLanguage>
      <id/>
      <isoCode>en</isoCode>
    </uiLanguage>
    <uiLanguage>
      <id/>
      <isoCode>de</isoCode>
    </uiLanguage>
    <uiLanguage>
      <id/>
      <isoCode>fr</isoCode>
    </uiLanguage>
  </uiLanguages>
</createUiLanguageRequest>

```


## Response

```xml
<cinnamon>
  <uiLanguages/>
</cinnamon>

```


---

# /api/uiLanguage/delete


## Request

```xml
<deleteUiLanguageRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</deleteUiLanguageRequest>

```


## Response

```xml
<cinnamon>
  <success>false</success>
</cinnamon>

```


---

# /api/uiLanguage/list


## Request

```xml
<listUiLanguageRequest>
  <type>FULL</type>
</listUiLanguageRequest>

```


## Response

```xml
<cinnamon>
  <uiLanguages/>
</cinnamon>

```


---

# /api/uiLanguage/update


## Request

```xml
<updateUiLanguageRequest>
  <uiLanguages>
    <uiLanguage>
      <id>69</id>
      <isoCode>FR</isoCode>
    </uiLanguage>
    <uiLanguage>
      <id>96</id>
      <isoCode>GR</isoCode>
    </uiLanguage>
  </uiLanguages>
</updateUiLanguageRequest>

```


## Response

```xml
<cinnamon>
  <uiLanguages/>
</cinnamon>

```


---

# /api/user/create


## Request

```xml
<createUserAccountRequest>
  <userAccounts>
    <userAccount>
      <id/>
      <name>jane</name>
      <loginType>CINNAMON</loginType>
      <password>super-secret</password>
      <activated>true</activated>
      <locked>false</locked>
      <uiLanguageId>1</uiLanguageId>
      <fullname>Jane Doe</fullname>
      <email>jane@example.com</email>
      <changeTracking>false</changeTracking>
      <activateTriggers>true</activateTriggers>
      <passwordExpired>false</passwordExpired>
      <groupIds/>
      <config>&lt;config/></config>
    </userAccount>
  </userAccounts>
</createUserAccountRequest>

```


## Response

```xml
<UserAccountWrapper>
  <users>
    <user>
      <id/>
      <name>user-wrapper-example</name>
      <loginType>CINNAMON</loginType>
      <password>see-creta</password>
      <activated>true</activated>
      <locked>false</locked>
      <uiLanguageId>1</uiLanguageId>
      <fullname>U.W.Example</fullname>
      <email>user@example.com</email>
      <changeTracking>true</changeTracking>
      <activateTriggers>true</activateTriggers>
      <passwordExpired>false</passwordExpired>
      <groupIds/>
      <config>&lt;config/></config>
    </user>
  </users>
</UserAccountWrapper>

```


---

# /api/user/get


## Request

```xml
<getUserAccountRequest>
  <userId>1</userId>
  <username/>
</getUserAccountRequest>

```
```xml
<getUserAccountRequest>
  <userId/>
  <username>by-name</username>
</getUserAccountRequest>

```


## Response

```xml
<UserAccountWrapper>
  <users>
    <user>
      <id/>
      <name>user-wrapper-example</name>
      <loginType>CINNAMON</loginType>
      <password>see-creta</password>
      <activated>true</activated>
      <locked>false</locked>
      <uiLanguageId>1</uiLanguageId>
      <fullname>U.W.Example</fullname>
      <email>user@example.com</email>
      <changeTracking>true</changeTracking>
      <activateTriggers>true</activateTriggers>
      <passwordExpired>false</passwordExpired>
      <groupIds/>
      <config>&lt;config/></config>
    </user>
  </users>
</UserAccountWrapper>

```


---

# /api/user/list


## Request

```xml
<listUserAccountRequest>
  <type>FULL</type>
</listUserAccountRequest>

```


## Response

```xml
<UserAccountWrapper>
  <users>
    <user>
      <id/>
      <name>user-wrapper-example</name>
      <loginType>CINNAMON</loginType>
      <password>see-creta</password>
      <activated>true</activated>
      <locked>false</locked>
      <uiLanguageId>1</uiLanguageId>
      <fullname>U.W.Example</fullname>
      <email>user@example.com</email>
      <changeTracking>true</changeTracking>
      <activateTriggers>true</activateTriggers>
      <passwordExpired>false</passwordExpired>
      <groupIds/>
      <config>&lt;config/></config>
    </user>
  </users>
</UserAccountWrapper>

```


---

# /api/user/setConfig
Update a user's individual configuration

## Request

```xml
<setUserConfigRequest>
  <userId>123</userId>
  <config>&lt;config>&lt;lastSearches>&lt;search>foo&lt;/search>&lt;/lastSearches>&lt;/config></config>
</setUserConfigRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/user/setPassword


## Request

```xml
<setPasswordRequest>
  <userId>123</userId>
  <password>my-new-secret-password</password>
</setPasswordRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/user/update


## Request

```xml
<updateUserAccountRequest>
  <userAccounts/>
</updateUserAccountRequest>

```


## Response

```xml
<UserAccountWrapper>
  <users>
    <user>
      <id/>
      <name>user-wrapper-example</name>
      <loginType>CINNAMON</loginType>
      <password>see-creta</password>
      <activated>true</activated>
      <locked>false</locked>
      <uiLanguageId>1</uiLanguageId>
      <fullname>U.W.Example</fullname>
      <email>user@example.com</email>
      <changeTracking>true</changeTracking>
      <activateTriggers>true</activateTriggers>
      <passwordExpired>false</passwordExpired>
      <groupIds/>
      <config>&lt;config/></config>
    </user>
  </users>
</UserAccountWrapper>

```


---

