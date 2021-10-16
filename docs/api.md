# /api/aclGroup/create
Create a new ACL group. Note: permissions parameter is not yet implemented.

## Request

```xml
<createAclGroupRequest>
  <aclGroups>
    <aclGroups>
      <id/>
      <aclId>1</aclId>
      <groupId>2</groupId>
      <permissions/>
    </aclGroups>
    <aclGroups>
      <id/>
      <aclId>1</aclId>
      <groupId>3</groupId>
      <permissions/>
    </aclGroups>
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
  <names/>
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
  <acls/>
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

# /api/configEntry/getConfigEntry


## Request

```xml
<configEntryRequest>
  <name/>
</configEntryRequest>

```


## Response

```xml
<cinnamon>
  <configEntries/>
</cinnamon>

```


---

# /api/configEntry/setConfigEntry


## Request

```xml
<createConfigEntryRequest>
  <name/>
  <config/>
  <publicVisibility>false</publicVisibility>
</createConfigEntryRequest>

```


## Response

```xml
<cinnamon>
  <configEntries/>
</cinnamon>

```


---

# /api/config/listAllConfigurations


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
</cinnamon>

```


---

# /api/folderType/create


## Request

```xml
<createFolderTypeRequest>
  <names/>
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


## Request

```xml
<createFolderRequest>
  <name/>
  <parentId/>
  <summary>&lt;summary /></summary>
  <ownerId/>
  <aclId/>
  <typeId/>
</createFolderRequest>

```


## Response



---

# /api/folder/createMeta


## Request

```xml
<createMetaRequest>
  <id/>
  <content/>
  <typeId/>
  <typeName/>
</createMetaRequest>

```


## Response

```xml
<cinnamon>
  <metasets/>
</cinnamon>

```


---

# /api/folder/deleteMeta


## Request

```xml
<deleteMetaRequest>
  <id/>
  <metaId/>
  <typeName/>
</deleteMetaRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

```


---

# /api/folder/getFolder
Fetch a single folder

## Request

```xml
<singleFolderRequest>
  <id/>
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
  <path/>
  <includeSummary>false</includeSummary>
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
  <id/>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
  <typeNames>
    <typeName>copyright</typeName>
    <typeName>thumbnail</typeName>
  </typeNames>
</metaRequest>

```
```xml
<metaRequest>
  <id>1</id>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
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
  <id/>
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


## Request

```xml
<setSummaryRequest>
  <id/>
  <summary/>
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

# /api/group/addUserToGroups


## Request

```xml
<addUserToGroupsRequest>
  <userId/>
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
  <names/>
</createGroupRequest>

```


## Response

```xml
<cinnamon>
  <groups/>
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
  <groups/>
</cinnamon>

```


---

# /api/group/removeUserFromGroups


## Request

```xml
<removeUserFromGroupsRequest>
  <userId/>
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
  <groups/>
</updateGroupRequest>

```


## Response

```xml
<cinnamon>
  <groups/>
</cinnamon>

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
<cinnamon>
  <indexItems/>
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

# /api/lifecycleState/attachLifecycle


## Request

```xml
<attachLifecycleRequest>
  <osdId/>
  <lifecycleId/>
  <lifecycleStateId/>
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
  <stateName/>
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

# /api/lifecycleState/getLifecycleState


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

# /api/lifecycle/getLifecycle


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
  <links/>
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

# /api//


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

# /api/objectType/create


## Request

```xml
<createObjectTypeRequest>
  <names/>
</createObjectTypeRequest>

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

# /api/osd/createMeta


## Request

```xml
<createMetaRequest>
  <id/>
  <content/>
  <typeId/>
  <typeName/>
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
  <name/>
  <parentId/>
  <ownerId/>
  <aclId/>
  <typeId/>
  <formatId/>
  <languageId/>
  <lifecycleStateId/>
  <summary>&lt;summary /></summary>
  <metas/>
</createOsdRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
  <references/>
</cinnamon>

```


---

# /api/osd/deleteMeta


## Request

```xml
<deleteMetaRequest>
  <id/>
  <metaId/>
  <typeName/>
</deleteMetaRequest>

```


## Response

```xml
<genericResponse>
  <message/>
  <successful>false</successful>
</genericResponse>

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
  <id/>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
  <typeNames>
    <typeName>copyright</typeName>
    <typeName>thumbnail</typeName>
  </typeNames>
</metaRequest>

```
```xml
<metaRequest>
  <id>1</id>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
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
</cinnamon>

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


## Request

```xml
<setSummaryRequest>
  <id/>
  <summary/>
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

# /api/relationType/create


## Request

```xml
<createRelationTypeRequest>
  <types/>
</createRelationTypeRequest>

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
  <types/>
</updateRelationTypeRequest>

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

# /api/relation/create


## Request

```xml
<createRelationRequest>
  <leftId/>
  <rightId/>
  <typeName/>
  <metadata>&lt;meta/></metadata>
</createRelationRequest>

```


## Response

```xml
<cinnamon>
  <relations/>
</cinnamon>

```


---

# /api/relation/delete


## Request

```xml
<deleteRelationRequest>
  <leftId/>
  <rightId/>
  <typeName/>
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

# /api/relation/list


## Request

```xml
<relationRequest>
  <includeMetadata>false</includeMetadata>
</relationRequest>

```


## Response

```xml
<cinnamon>
  <relations/>
</cinnamon>

```


---

# /static/
Returns a static file from the server (for example, a favicon.ico if one exists).

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

# /api/user/list


## Request

```xml
<listUserInfoRequest>
  <type>FULL</type>
</listUserInfoRequest>

```


## Response

```xml
<cinnamon>
  <users/>
</cinnamon>

```


---

# /api/user/setPassword


## Request

```xml
<setPasswordRequest>
  <userId/>
  <password/>
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

# /api/user/userInfo


## Request

```xml
<userInfoRequest>
  <userId/>
  <username/>
</userInfoRequest>

```


## Response

```xml
<cinnamon>
  <users/>
</cinnamon>

```


---

