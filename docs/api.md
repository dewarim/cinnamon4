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
<AclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</AclGroup>

```
```xml
<AclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</AclGroup>

```


---

# /api/aclGroup/delete


## Request

```xml
<DeleteAclGroupRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteAclGroupRequest>

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
<ListAclGroupRequest>
  <type>FULL</type>
</ListAclGroupRequest>

```


## Response

```xml
<AclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</AclGroup>

```
```xml
<AclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</AclGroup>

```


---

# /api/aclGroup/listByGroupOrAcl


## Request

```xml
<AclGroupListRequest>
  <id/>
  <idType/>
</AclGroupListRequest>

```


## Response

```xml
<AclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</AclGroup>

```
```xml
<AclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</AclGroup>

```


---

# /api/aclGroup/update


## Request

```xml
<UpdateAclGroupRequest>
  <aclGroups/>
</UpdateAclGroupRequest>

```


## Response

```xml
<AclGroup>
  <id>1</id>
  <aclId>1</aclId>
  <groupId>2</groupId>
  <permissions/>
</AclGroup>

```
```xml
<AclGroup>
  <id>2</id>
  <aclId>1</aclId>
  <groupId>3</groupId>
  <permissions/>
</AclGroup>

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
<IdRequest>
  <id/>
</IdRequest>

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

# /api/acl/updateAcl


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
<ConfigEntryRequest>
  <name/>
</ConfigEntryRequest>

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
<CreateConfigEntryRequest>
  <name/>
  <config/>
  <publicVisibility>false</publicVisibility>
</CreateConfigEntryRequest>

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
<ListConfigRequest>
  <type>FULL</type>
</ListConfigRequest>

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
<CreateFolderTypeRequest>
  <names/>
</CreateFolderTypeRequest>

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
<DeleteFolderTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteFolderTypeRequest>

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
<ListFolderTypeRequest>
  <type>FULL</type>
</ListFolderTypeRequest>

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
<UpdateFolderTypeRequest>
  <folderTypes/>
</UpdateFolderTypeRequest>

```


## Response

```xml
<cinnamon>
  <folderTypes/>
</cinnamon>

```


---

# /api/folder/createFolder


## Request

```xml
<CreateFolderRequest>
  <name/>
  <parentId/>
  <summary>&lt;summary /></summary>
  <ownerId/>
  <aclId/>
  <typeId/>
</CreateFolderRequest>

```


## Response



---

# /api/folder/createMeta


## Request

```xml
<CreateMetaRequest>
  <id/>
  <content/>
  <typeId/>
  <typeName/>
</CreateMetaRequest>

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
<DeleteMetaRequest>
  <id/>
  <metaId/>
  <typeName/>
</DeleteMetaRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/folder/getFolder
Fetch a single folder

## Request

```xml
<SingleFolderRequest>
  <id/>
  <includeSummary>false</includeSummary>
</SingleFolderRequest>

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
<FolderRequest>
  <ids/>
  <includeSummary>false</includeSummary>
</FolderRequest>

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
<FolderPathRequest>
  <path/>
  <includeSummary>false</includeSummary>
</FolderPathRequest>

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
<MetaRequest>
  <id/>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
  <typeNames>
    <typeName>copyright</typeName>
    <typeName>thumbnail</typeName>
  </typeNames>
</MetaRequest>

```
```xml
<MetaRequest>
  <id>1</id>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
</MetaRequest>

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
<SingleFolderRequest>
  <id/>
  <includeSummary>false</includeSummary>
</SingleFolderRequest>

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
<IdListRequest/>

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
<SetSummaryRequest>
  <id/>
  <summary/>
</SetSummaryRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/folder/updateFolder


## Request

```xml
<UpdateFolderRequest>
  <id/>
  <parentId/>
  <name/>
  <ownerId/>
  <typeId/>
  <aclId/>
</UpdateFolderRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/format/list


## Request

```xml
<ListFormatRequest>
  <type>FULL</type>
</ListFormatRequest>

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
<CreateFormatRequest>
  <formats/>
</CreateFormatRequest>

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
<UpdateFormatRequest>
  <formats/>
</UpdateFormatRequest>

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
<DeleteFormatRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteFormatRequest>

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
<AddUserToGroupsRequest/>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/group/create


## Request

```xml
<CreateGroupRequest>
  <names/>
</CreateGroupRequest>

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
<DeleteGroupRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteGroupRequest>

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
<ListGroupRequest>
  <type>FULL</type>
</ListGroupRequest>

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
<RemoveUserFromGroupsRequest/>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/group/update


## Request

```xml
<UpdateGroupRequest>
  <groups/>
</UpdateGroupRequest>

```


## Response

```xml
<cinnamon>
  <groups/>
</cinnamon>

```


---

# /api/indexItem/listIndexItems


## Request

```xml
<ListIndexItemRequest>
  <type>FULL</type>
</ListIndexItemRequest>

```


## Response

```xml
<cinnamon>
  <indexItems/>
</cinnamon>

```


---

# /api/language/listLanguages


## Request

```xml
<ListLanguageRequest>
  <type>FULL</type>
</ListLanguageRequest>

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
<AttachLifecycleRequest>
  <osdId/>
  <lifecycleId/>
  <lifecycleStateId/>
</AttachLifecycleRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/lifecycleState/changeState


## Request

```xml
<ChangeLifecycleStateRequest>
  <osdId/>
  <stateName/>
  <stateId/>
</ChangeLifecycleStateRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/lifecycleState/detachLifecycle


## Request

```xml
<IdRequest>
  <id/>
</IdRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/lifecycleState/getLifecycleState


## Request

```xml
<IdRequest>
  <id/>
</IdRequest>

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
<IdRequest>
  <id/>
</IdRequest>

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
<LifecycleRequest>
  <id/>
  <name/>
</LifecycleRequest>

```


## Response

```xml
<cinnamon>
  <lifecycles/>
</cinnamon>

```


---

# /api/lifecycle/listLifecycles
List lifecycles 

## Request

```xml
<ListLifecycleRequest>
  <type>FULL</type>
</ListLifecycleRequest>

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
<CreateLinkRequest>
  <links/>
</CreateLinkRequest>

```


## Response

```xml
<cinnamon>
  <links/>
</cinnamon>

```


---

# /api/link/delete


## Request

```xml
<DeleteLinkRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteLinkRequest>

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
<GetLinksRequest>
  <includeSummary>false</includeSummary>
  <ids/>
</GetLinksRequest>

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
<UpdateLinkRequest>
  <links/>
</UpdateLinkRequest>

```


## Response

```xml
<cinnamon>
  <links/>
</cinnamon>

```


---

# /api/metasetType/listMetasetTypes


## Request

```xml
<ListMetasetTypeRequest>
  <type>FULL</type>
</ListMetasetTypeRequest>

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
<ListObjectTypeRequest>
  <type>FULL</type>
</ListObjectTypeRequest>

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
<CreateObjectTypeRequest>
  <names/>
</CreateObjectTypeRequest>

```


## Response

```xml
<cinnamon>
  <objectTypes/>
</cinnamon>

```


---

# /api/objectType/udpate


## Request

```xml
<UpdateObjectTypeRequest>
  <objectTypes/>
</UpdateObjectTypeRequest>

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
<DeleteObjectTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteObjectTypeRequest>

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
<CreateMetaRequest>
  <id/>
  <content/>
  <typeId/>
  <typeName/>
</CreateMetaRequest>

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
<CreateOsdRequest>
  <name/>
  <parentId/>
  <ownerId/>
  <aclId/>
  <typeId/>
  <formatId/>
  <languageId/>
  <lifecycleStateId/>
  <summary>&lt;summary /></summary>
</CreateOsdRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
</cinnamon>

```


---

# /api/osd/deleteMeta


## Request

```xml
<DeleteMetaRequest>
  <id/>
  <metaId/>
  <typeName/>
</DeleteMetaRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/osd/deleteOsds


## Request

```xml
<DeleteOsdRequest>
  <ids/>
  <deleteDescendants>false</deleteDescendants>
</DeleteOsdRequest>

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
<IdRequest>
  <id/>
</IdRequest>

```


## Response



---

# /api/osd/getMeta


## Request

```xml
<MetaRequest>
  <id/>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
  <typeNames>
    <typeName>copyright</typeName>
    <typeName>thumbnail</typeName>
  </typeNames>
</MetaRequest>

```
```xml
<MetaRequest>
  <id>1</id>
  <version3CompatibilityRequired>false</version3CompatibilityRequired>
</MetaRequest>

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
<OsdByFolderRequest>
  <includeSummary>false</includeSummary>
  <folderId>0</folderId>
</OsdByFolderRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
</cinnamon>

```


---

# /api/osd/getObjectsById


## Request

```xml
<OsdRequest>
  <ids/>
  <includeSummary>false</includeSummary>
</OsdRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
</cinnamon>

```


---

# /api/osd/getSummaries


## Request

```xml
<IdListRequest/>

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
<IdRequest>
  <id/>
</IdRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/osd/setContent
Set an OSD's content. Requires a multipart-mime request, with part "setContentRequest" and part "file".


## Request

```xml
<SetContentRequest>
  <id/>
  <formatId/>
</SetContentRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/osd/setSummary


## Request

```xml
<SetSummaryRequest>
  <id/>
  <summary/>
</SetSummaryRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/osd/unlock


## Request

```xml
<IdRequest>
  <id/>
</IdRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/osd/version
Create a new version of an OSD. Requires a multipart-mime request, with part "createNewVersionRequest" and optional
part "file", if the new version should contain data.


## Request

```xml
<CreateNewVersionRequest>
  <id/>
  <metaRequests/>
  <formatId/>
</CreateNewVersionRequest>

```


## Response

```xml
<cinnamon>
  <osds/>
  <links/>
</cinnamon>

```


---

# /api/permission/changePermissions


## Request

```xml
<ChangePermissionsRequest>
  <aclGroupId/>
  <add/>
  <remove/>
</ChangePermissionsRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/permission/getUserPermissions


## Request

```xml
<UserPermissionRequest>
  <userId/>
  <aclId/>
</UserPermissionRequest>

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
<ListPermissionRequest>
  <type>FULL</type>
</ListPermissionRequest>

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
<ListRelationTypeRequest>
  <type>FULL</type>
</ListRelationTypeRequest>

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
<CreateRelationTypeRequest>
  <types/>
</CreateRelationTypeRequest>

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
<UpdateRelationTypeRequest>
  <types/>
</UpdateRelationTypeRequest>

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
<DeleteRelationTypeRequest>
  <ignoreNotFound>false</ignoreNotFound>
  <ids/>
</DeleteRelationTypeRequest>

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
<CreateRelationRequest>
  <leftId/>
  <rightId/>
  <typeName/>
  <metadata>&lt;meta/></metadata>
</CreateRelationRequest>

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
<DeleteRelationRequest>
  <leftId/>
  <rightId/>
  <typeName/>
</DeleteRelationRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/relation/list


## Request

```xml
<RelationRequest>
  <includeMetadata>false</includeMetadata>
</RelationRequest>

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

# /api/uiLanguage/listUiLanguages


## Request

```xml
<ListUiLanguageRequest>
  <type>FULL</type>
</ListUiLanguageRequest>

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
<ListUserInfoRequest>
  <type>FULL</type>
</ListUserInfoRequest>

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
<SetPasswordRequest>
  <userId/>
  <password/>
</SetPasswordRequest>

```


## Response

```xml
<GenericResponse>
  <message/>
  <successful>false</successful>
</GenericResponse>

```


---

# /api/user/userInfo


## Request

```xml
<UserInfoRequest>
  <userId/>
  <username/>
</UserInfoRequest>

```


## Response

```xml
<cinnamon>
  <users/>
</cinnamon>

```


---

