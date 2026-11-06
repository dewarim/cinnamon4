# API Endpoint Documentation

Note: response examples are not directly matching request examples, they just show the format you can expect.

The constraint table of an endpoint lists what the server checks before a request reaches the business
logic; a violation is answered with the error code INVALID_REQUEST and a message naming every offending
field. A path ending in `[]` constrains the elements of a list or set, and a nested path such as
`osds[].name` constrains the objects the request carries - these are the paths the error message uses,
so a client can match a rejection against the table. What a create and an update of the same object
require differs, and each table shows only the constraints of its own endpoint. The tables are generated
from the annotations the server validates with, so they cannot drift from what it enforces; a rule that
no annotation expresses is stated in prose under the table instead.


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
      <permissions>
        <permissionId>10</permissionId>
        <permissionId>12</permissionId>
      </permissions>
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

### Constraints

| field | constraints |
|---|---|
| `aclGroups` | `@NotEmpty` |
| `aclGroups[].aclId` | `@NotNull`, `@Positive` |
| `aclGroups[].groupId` | `@NotNull`, `@Positive` |
| `aclGroups[].permissionIds[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <aclGroups>
    <aclGroup>
      <id>1</id>
      <aclId>1</aclId>
      <groupId>2</groupId>
      <permissions/>
    </aclGroup>
    <aclGroup>
      <id>2</id>
      <aclId>1</aclId>
      <groupId>3</groupId>
      <permissions/>
    </aclGroup>
  </aclGroups>
</cinnamon>

```


---

# /api/aclGroup/delete


## Request

```xml
<deleteAclGroupRequest>
  <ids>
    <id>5</id>
    <id>78</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteAclGroupRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
<cinnamon>
  <changeTriggerResponses/>
  <aclGroups>
    <aclGroup>
      <id>1</id>
      <aclId>1</aclId>
      <groupId>2</groupId>
      <permissions/>
    </aclGroup>
    <aclGroup>
      <id>2</id>
      <aclId>1</aclId>
      <groupId>3</groupId>
      <permissions/>
    </aclGroup>
  </aclGroups>
</cinnamon>

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

### Constraints

| field | constraints |
|---|---|
| `id` | `@Positive` |

**Also:** At least one of id and idType has to be set.


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <aclGroups>
    <aclGroup>
      <id>1</id>
      <aclId>1</aclId>
      <groupId>2</groupId>
      <permissions/>
    </aclGroup>
    <aclGroup>
      <id>2</id>
      <aclId>1</aclId>
      <groupId>3</groupId>
      <permissions/>
    </aclGroup>
  </aclGroups>
</cinnamon>

```


---

# /api/aclGroup/update
This replaces the existing permissions of the given AclGroup with the ones you send with the request.
It does not otherwise change the AclGroup (as it only contains of a group and acl reference - to change those, create a new AclGroup.


## Request

```xml
<updateAclGroupRequest>
  <aclGroups>
    <aclGroup>
      <id>1345</id>
      <aclId>54</aclId>
      <groupId>4</groupId>
      <permissions>
        <permissionId>5</permissionId>
        <permissionId>2</permissionId>
      </permissions>
    </aclGroup>
  </aclGroups>
</updateAclGroupRequest>

```

### Constraints

| field | constraints |
|---|---|
| `aclGroups` | `@NotEmpty` |
| `aclGroups[].aclId` | `@NotNull`, `@Positive` |
| `aclGroups[].groupId` | `@NotNull`, `@Positive` |
| `aclGroups[].id` | `@NotNull`, `@Positive` |
| `aclGroups[].permissionIds[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <aclGroups>
    <aclGroup>
      <id>1</id>
      <aclId>1</aclId>
      <groupId>2</groupId>
      <permissions/>
    </aclGroup>
    <aclGroup>
      <id>2</id>
      <aclId>1</aclId>
      <groupId>3</groupId>
      <permissions/>
    </aclGroup>
  </aclGroups>
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

### Constraints

| field | constraints |
|---|---|
| `acls` | `@NotEmpty` |
| `acls[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <acls>
    <acl>
      <id>1</id>
      <name>DEFAULT_ACL</name>
    </acl>
    <acl>
      <id>43</id>
      <name>reviewers</name>
    </acl>
  </acls>
</cinnamon>

```


---

# /api/acl/delete


## Request

```xml
<deleteAclRequest>
  <ids>
    <id>99</id>
    <id>43</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteAclRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/acl/getUserAcls


## Request

```xml
<idRequest>
  <id>7</id>
</idRequest>

```

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <acls>
    <acl>
      <id>1</id>
      <name>DEFAULT_ACL</name>
    </acl>
    <acl>
      <id>43</id>
      <name>reviewers</name>
    </acl>
  </acls>
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
  <changeTriggerResponses/>
  <acls>
    <acl>
      <id>1</id>
      <name>DEFAULT_ACL</name>
    </acl>
    <acl>
      <id>43</id>
      <name>reviewers</name>
    </acl>
  </acls>
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

### Constraints

| field | constraints |
|---|---|
| `acls` | `@NotEmpty` |
| `acls[].id` | `@NotNull`, `@Positive` |
| `acls[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <acls>
    <acl>
      <id>1</id>
      <name>DEFAULT_ACL</name>
    </acl>
    <acl>
      <id>43</id>
      <name>reviewers</name>
    </acl>
  </acls>
</cinnamon>

```


---

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
      <postCommitTrigger>true</postCommitTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>100</ranking>
      <config>&lt;config>&lt;remoteServer>http://localhost:64888/createThumbnail&lt;/remoteServer>&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
</createChangeTriggerRequest>

```

### Constraints

| field | constraints |
|---|---|
| `changeTriggers` | `@NotEmpty` |
| `changeTriggers[].action` | `@NotBlank` |
| `changeTriggers[].config` | `@NotNull` |
| `changeTriggers[].controller` | `@NotBlank` |
| `changeTriggers[].name` | `@NotBlank` |
| `changeTriggers[].triggerType` | `@NotNull` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <changeTriggers>
    <changeTrigger>
      <id>4</id>
      <name>logging-trigger</name>
      <controller>osd</controller>
      <action>delete</action>
      <active>true</active>
      <preTrigger>false</preTrigger>
      <postTrigger>true</postTrigger>
      <postCommitTrigger>true</postCommitTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>1000</ranking>
      <config>&lt;config>&lt;!-- define URL to send notice of successful delete events to -->&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
</cinnamon>

```


---

# /api/changeTrigger/delete


## Request

```xml
<deleteChangeTriggerRequest>
  <ids>
    <id>77</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteChangeTriggerRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <changeTriggers>
    <changeTrigger>
      <id>4</id>
      <name>logging-trigger</name>
      <controller>osd</controller>
      <action>delete</action>
      <active>true</active>
      <preTrigger>false</preTrigger>
      <postTrigger>true</postTrigger>
      <postCommitTrigger>true</postCommitTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>1000</ranking>
      <config>&lt;config>&lt;!-- define URL to send notice of successful delete events to -->&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
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
      <postCommitTrigger>false</postCommitTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>100</ranking>
      <config>&lt;config>&lt;url>http://localhost:64888/createThumbnail&lt;/url>&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
</updateChangeTriggerRequest>

```

### Constraints

| field | constraints |
|---|---|
| `changeTriggers` | `@NotEmpty` |
| `changeTriggers[].action` | `@NotBlank` |
| `changeTriggers[].config` | `@NotNull` |
| `changeTriggers[].controller` | `@NotBlank` |
| `changeTriggers[].id` | `@NotNull`, `@Positive` |
| `changeTriggers[].name` | `@NotBlank` |
| `changeTriggers[].triggerType` | `@NotNull` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <changeTriggers>
    <changeTrigger>
      <id>4</id>
      <name>logging-trigger</name>
      <controller>osd</controller>
      <action>delete</action>
      <active>true</active>
      <preTrigger>false</preTrigger>
      <postTrigger>true</postTrigger>
      <postCommitTrigger>true</postCommitTrigger>
      <copyFileContent>false</copyFileContent>
      <ranking>1000</ranking>
      <config>&lt;config>&lt;!-- define URL to send notice of successful delete events to -->&lt;/config></config>
      <triggerType>MICROSERVICE</triggerType>
    </changeTrigger>
  </changeTriggers>
</cinnamon>

```


---

# /api/changeTrigger/nop


---

# /cinnamon/connect
Connect to the cinnamon server by sending a ConnectionRequest.

Example call:

    TICKET=$(curl --silent --show-error -X POST "<connectionRequest><username>joe</username><password>1234Geheim</password><format>xml</format></connectionRequest>",)

If you choose "format=text", you will receive a plain/text response with just the session ticket.
Otherwise, you will get:

    <cinnamon><cinnamonConnection><ticket>9d55332e-9ef3-4743-969c-28316e58e146</ticket></cinnamonConnection></cinnamon>

The ticket is a session id that must be sent with all other requests to the server,
in the request header field "ticket".


## Request

```xml
<connectionRequest>
  <username>john</username>
  <password>password</password>
  <format>text</format>
</connectionRequest>

```
```xml
<connectionRequest>
  <username>jane</username>
  <password>password</password>
  <format/>
</connectionRequest>

```

### Constraints

| field | constraints |
|---|---|
| `password` | `@NotBlank` |
| `username` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <cinnamonConnection>
    <ticket>64772ea0-0184-4f94-96d4-6348d88e9e82</ticket>
  </cinnamonConnection>
</cinnamon>

```


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

### Constraints

| field | constraints |
|---|---|
| `configEntries` | `@NotEmpty` |
| `configEntries[].config` | `@NotNull` |
| `configEntries[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
<deleteConfigRequest>
  <ids>
    <id>1</id>
    <id>2</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteConfigRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
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
<updateConfigEntryRequest>
  <configEntries>
    <configEntry>
      <id>321</id>
      <name>default-ui-settings</name>
      <config>&lt;xml>&lt;show-logo>true&lt;/show-logo>&lt;/xml></config>
      <publicVisibility>true</publicVisibility>
    </configEntry>
  </configEntries>
</updateConfigEntryRequest>

```
```xml
<updateConfigEntryRequest>
  <configEntries>
    <configEntry>
      <id>444</id>
      <name>render-server-password</name>
      <config>xxx</config>
      <publicVisibility>false</publicVisibility>
    </configEntry>
  </configEntries>
</updateConfigEntryRequest>

```

### Constraints

| field | constraints |
|---|---|
| `configEntries` | `@NotEmpty` |
| `configEntries[].config` | `@NotNull` |
| `configEntries[].id` | `@NotNull`, `@Positive` |
| `configEntries[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
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

# /api/config/reloadLogging
reload the logging configuration

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
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `folderTypes` | `@NotEmpty` |
| `folderTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folderTypes>
    <folderType>
      <id>1</id>
      <name>system-folder</name>
    </folderType>
  </folderTypes>
</cinnamon>

```


---

# /api/folderType/delete
Delete a folder type

## Request

```xml
<deleteFolderTypeRequest>
  <ids>
    <id>44</id>
    <id>543</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteFolderTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <folderTypes>
    <folderType>
      <id>1</id>
      <name>system-folder</name>
    </folderType>
  </folderTypes>
</cinnamon>

```


---

# /api/folderType/update


## Request

```xml
<updateFolderTypeRequest>
  <folderTypes>
    <folderType>
      <id>665</id>
      <name>almost-evil-type</name>
    </folderType>
  </folderTypes>
</updateFolderTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `folderTypes` | `@NotEmpty` |
| `folderTypes[].id` | `@NotNull`, `@Positive` |
| `folderTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folderTypes>
    <folderType>
      <id>1</id>
      <name>system-folder</name>
    </folderType>
  </folderTypes>
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
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets>
        <metaset>
          <id/>
          <objectId>1</objectId>
          <typeId>2</typeId>
          <content>&lt;xml>some meta content&lt;/xml></content>
        </metaset>
      </metasets>
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
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
</createFolderRequest>

```

### Constraints

| field | constraints |
|---|---|
| `folders` | `@NotEmpty` |
| `folders[].aclId` | `@Positive` |
| `folders[].metasets[].content` | `@NotNull` |
| `folders[].metasets[].objectId` | `@Positive` |
| `folders[].metasets[].typeId` | `@NotNull`, `@Positive` |
| `folders[].name` | `@NotNull`, `@Pattern(flags = [DOTALL], regexp = ".*\S.*")` |
| `folders[].ownerId` | `@Positive` |
| `folders[].parentId` | `@NotNull`, `@Positive` |
| `folders[].typeId` | `@Positive` |


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

### Constraints

| field | constraints |
|---|---|
| `metas` | `@NotEmpty` |
| `metas[].content` | `@NotNull` |
| `metas[].objectId` | `@NotNull`, `@Positive` |
| `metas[].typeId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <metasets>
    <metaset>
      <id/>
      <objectId>6</objectId>
      <typeId>8</typeId>
      <content>&lt;xml> folder meta object &lt;/xml></content>
    </metaset>
    <metaset>
      <id/>
      <objectId>7</objectId>
      <typeId>65</typeId>
      <content>&lt;xml> osd meta object &lt;/xml></content>
    </metaset>
  </metasets>
</cinnamon>

```


---

# /api/folder/delete


## Request

```xml
<deleteFolderRequest>
  <ids>
    <id>1</id>
    <id>2</id>
    <id>3</id>
  </ids>
  <deleteRecursively>true</deleteRecursively>
  <deleteContent>false</deleteContent>
</deleteFolderRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/folder/deleteAllMetas
Delete all metasets linked to the given Folder ids. Parameter ignoreNotFound is not used.

## Request

```xml
<deleteAllMetasRequest>
  <ids>
    <id>14</id>
    <id>15</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteAllMetasRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/folder/deleteMeta
Delete the folder metasets with the given meta ids.

## Request

```xml
<deleteMetaRequest>
  <ids>
    <id>3</id>
    <id>5</id>
    <id>6</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteMetaRequest>

```
```xml
<deleteMetaRequest>
  <ids>
    <id>1</id>
  </ids>
  <ignoreNotFound>true</ignoreNotFound>
</deleteMetaRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folders>
    <folder>
      <id>2</id>
      <name>images</name>
      <aclId>1</aclId>
      <ownerId>33</ownerId>
      <parentId>3</parentId>
      <typeId>5</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
  <links>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>3</ownerId>
      <aclId>2</aclId>
      <parentId>1</parentId>
      <folderId>20</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
  <references>
    <reference>
      <id>203</id>
      <name>linked folder</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>23</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary/></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>1970-01-01T00:00:00</created>
      <metasets/>
    </reference>
  </references>
</cinnamon>

```


---

# /api/folder/getFolders


## Request

```xml
<folderRequest>
  <ids>
    <id>1</id>
    <id>2</id>
    <id>3</id>
  </ids>
  <includeSummary>true</includeSummary>
  <addFolderPath>true</addFolderPath>
</folderRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folders>
    <folder>
      <id>2</id>
      <name>images</name>
      <aclId>1</aclId>
      <ownerId>33</ownerId>
      <parentId>3</parentId>
      <typeId>5</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
  <links>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>3</ownerId>
      <aclId>2</aclId>
      <parentId>1</parentId>
      <folderId>20</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
  <references>
    <reference>
      <id>203</id>
      <name>linked folder</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>23</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary/></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>1970-01-01T00:00:00</created>
      <metasets/>
    </reference>
  </references>
</cinnamon>

```


---

# /api/folder/getFolderByPath
Fetch a folder specified by its complete path. The path parameter must not end with a forward slash '/'.

## Request

```xml
<folderPathRequest>
  <path>/home/creation/some-sub-folder</path>
  <includeSummary>true</includeSummary>
</folderPathRequest>

```

### Constraints

| field | constraints |
|---|---|
| `path` | `@NotEmpty` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folders>
    <folder>
      <id>2</id>
      <name>images</name>
      <aclId>1</aclId>
      <ownerId>33</ownerId>
      <parentId>3</parentId>
      <typeId>5</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
  <links>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>3</ownerId>
      <aclId>2</aclId>
      <parentId>1</parentId>
      <folderId>20</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
  <references>
    <reference>
      <id>203</id>
      <name>linked folder</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>23</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary/></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>1970-01-01T00:00:00</created>
      <metasets/>
    </reference>
  </references>
</cinnamon>

```


---

# /api/folder/getFolderByRelativePath
Fetch a folder specified by its relative path. The relative path must not start or end with a forward slash: '/'.

## Request

```xml
<folderByRelativePathRequest>
  <relativePath>creation/some-sub-folder</relativePath>
  <parentId>213</parentId>
  <includeSummary>true</includeSummary>
</folderByRelativePathRequest>

```

### Constraints

| field | constraints |
|---|---|
| `parentId` | `@NotNull`, `@Positive` |
| `relativePath` | `@NotEmpty`, `@Pattern(flags = [DOTALL], regexp = "[^/](.*[^/])?")` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folders>
    <folder>
      <id>2</id>
      <name>images</name>
      <aclId>1</aclId>
      <ownerId>33</ownerId>
      <parentId>3</parentId>
      <typeId>5</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
  <links>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>3</ownerId>
      <aclId>2</aclId>
      <parentId>1</parentId>
      <folderId>20</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
  <references>
    <reference>
      <id>203</id>
      <name>linked folder</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>23</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary/></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>1970-01-01T00:00:00</created>
      <metasets/>
    </reference>
  </references>
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

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |
| `typeIds[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <metasets>
    <metaset>
      <id/>
      <objectId>6</objectId>
      <typeId>8</typeId>
      <content>&lt;xml> folder meta object &lt;/xml></content>
    </metaset>
    <metaset>
      <id/>
      <objectId>7</objectId>
      <typeId>65</typeId>
      <content>&lt;xml> osd meta object &lt;/xml></content>
    </metaset>
  </metasets>
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

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <folders>
    <folder>
      <id>2</id>
      <name>images</name>
      <aclId>1</aclId>
      <ownerId>33</ownerId>
      <parentId>3</parentId>
      <typeId>5</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
  <links>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>3</ownerId>
      <aclId>2</aclId>
      <parentId>1</parentId>
      <folderId>20</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
  <references>
    <reference>
      <id>203</id>
      <name>linked folder</name>
      <aclId>1</aclId>
      <ownerId>2</ownerId>
      <parentId>3</parentId>
      <typeId>23</typeId>
      <metadataChanged>false</metadataChanged>
      <summary>&lt;summary/></summary>
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>1970-01-01T00:00:00</created>
      <metasets/>
    </reference>
  </references>
</cinnamon>

```


---

# /api/folder/getSummaries


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>5</id>
    <id>44</id>
  </ids>
</idListRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <summaries>
    <summary>
      <id>5</id>
      <content>&lt;summary> a summary of an OSD's content &lt;/summary></content>
    </summary>
  </summaries>
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

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |
| `summary` | `@NotNull` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/folder/update


## Request

```xml
<updateFolderRequest>
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
      <folderPath/>
      <hasSubfolders>false</hasSubfolders>
      <created>2022-08-10T03:21:00</created>
      <metasets/>
    </folder>
  </folders>
  <updateMetadataChanged>false</updateMetadataChanged>
</updateFolderRequest>

```

### Constraints

| field | constraints |
|---|---|
| `folders` | `@NotEmpty` |
| `folders[].aclId` | `@Positive` |
| `folders[].id` | `@NotNull`, `@Positive` |
| `folders[].metasets[].content` | `@NotNull` |
| `folders[].metasets[].id` | `@NotNull`, `@Positive` |
| `folders[].metasets[].objectId` | `@NotNull`, `@Positive` |
| `folders[].metasets[].typeId` | `@NotNull`, `@Positive` |
| `folders[].name` | `@Pattern(flags = [DOTALL], regexp = ".*\S.*")` |
| `folders[].ownerId` | `@Positive` |
| `folders[].parentId` | `@Positive` |
| `folders[].typeId` | `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `metas` | `@NotEmpty` |
| `metas[].content` | `@NotNull` |
| `metas[].id` | `@NotNull`, `@Positive` |
| `metas[].objectId` | `@NotNull`, `@Positive` |
| `metas[].typeId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/format/create


## Request

```xml
<createFormatRequest>
  <formats>
    <format>
      <id/>
      <contentType>application/cinnamon</contentType>
      <extension>cnm</extension>
      <name>CinnamonType</name>
      <defaultObjectTypeId>1</defaultObjectTypeId>
      <indexMode>NONE</indexMode>
    </format>
  </formats>
</createFormatRequest>

```

### Constraints

| field | constraints |
|---|---|
| `formats` | `@NotEmpty` |
| `formats[].contentType` | `@NotBlank` |
| `formats[].defaultObjectTypeId` | `@Positive` |
| `formats[].extension` | `@NotBlank` |
| `formats[].indexMode` | `@NotNull` |
| `formats[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <formats>
    <format>
      <id>50</id>
      <contentType>text/adoc</contentType>
      <extension>adoc</extension>
      <name>AsciiDoc</name>
      <defaultObjectTypeId>1</defaultObjectTypeId>
      <indexMode>PLAIN_TEXT</indexMode>
    </format>
  </formats>
</cinnamon>

```


---

# /api/format/delete


## Request

```xml
<deleteFormatRequest>
  <ids>
    <id>999</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteFormatRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <formats>
    <format>
      <id>50</id>
      <contentType>text/adoc</contentType>
      <extension>adoc</extension>
      <name>AsciiDoc</name>
      <defaultObjectTypeId>1</defaultObjectTypeId>
      <indexMode>PLAIN_TEXT</indexMode>
    </format>
  </formats>
</cinnamon>

```


---

# /api/format/update


## Request

```xml
<updateFormatRequest>
  <formats>
    <format>
      <id/>
      <contentType>text/plain</contentType>
      <extension>txt</extension>
      <name>text</name>
      <defaultObjectTypeId>2</defaultObjectTypeId>
      <indexMode>PLAIN_TEXT</indexMode>
    </format>
  </formats>
</updateFormatRequest>

```

### Constraints

| field | constraints |
|---|---|
| `formats` | `@NotEmpty` |
| `formats[].contentType` | `@NotBlank` |
| `formats[].defaultObjectTypeId` | `@NotNull`, `@Positive` |
| `formats[].extension` | `@NotBlank` |
| `formats[].id` | `@NotNull`, `@Positive` |
| `formats[].indexMode` | `@NotNull` |
| `formats[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <formats>
    <format>
      <id>50</id>
      <contentType>text/adoc</contentType>
      <extension>adoc</extension>
      <name>AsciiDoc</name>
      <defaultObjectTypeId>1</defaultObjectTypeId>
      <indexMode>PLAIN_TEXT</indexMode>
    </format>
  </formats>
</cinnamon>

```


---

# /api/group/addUserToGroups


## Request

```xml
<addUserToGroupsRequest>
  <groupIds>
    <groupId>1</groupId>
    <groupId>2</groupId>
    <groupId>3</groupId>
  </groupIds>
  <userId>33</userId>
</addUserToGroupsRequest>

```

### Constraints

| field | constraints |
|---|---|
| `groupIds` | `@NotEmpty` |
| `groupIds[]` | `@NotNull`, `@Positive` |
| `userId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `groups` | `@NotEmpty` |
| `groups[].name` | `@NotBlank` |
| `groups[].parentId` | `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
  <ids>
    <id>4</id>
    <id>6</id>
    <id>7</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
  <recursive>true</recursive>
</deleteGroupRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
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
  <groupIds>
    <groupId>1</groupId>
    <groupId>2</groupId>
    <groupId>3</groupId>
  </groupIds>
  <userId>33</userId>
</removeUserFromGroupsRequest>

```

### Constraints

| field | constraints |
|---|---|
| `groupIds` | `@NotEmpty` |
| `groupIds[]` | `@NotNull`, `@Positive` |
| `userId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `groups` | `@NotEmpty` |
| `groups[].id` | `@NotNull`, `@Positive` |
| `groups[].name` | `@NotBlank` |
| `groups[].parentId` | `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `indexItems` | `@NotEmpty` |
| `indexItems[].fieldName` | `@NotBlank` |
| `indexItems[].indexType` | `@NotNull` |
| `indexItems[].name` | `@NotBlank` |
| `indexItems[].searchCondition` | `@NotBlank` |
| `indexItems[].searchString` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <indexItems>
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
  </indexItems>
</cinnamon>

```


---

# /api/indexItem/delete


## Request

```xml
<deleteIndexItemRequest>
  <ids>
    <id>679</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteIndexItemRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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
<cinnamon>
  <changeTriggerResponses/>
  <indexItems>
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
  </indexItems>
</cinnamon>

```


---

# /api/indexItem/update


## Request

```xml
<updateIndexItemRequest>
  <indexItems>
    <indexItem>
      <id>550</id>
      <name>headline</name>
      <fieldName>content</fieldName>
      <searchString>//title</searchString>
      <searchCondition>true()</searchCondition>
      <multipleResults>true</multipleResults>
      <indexType>DEFAULT_INDEXER</indexType>
      <storeField>false</storeField>
    </indexItem>
  </indexItems>
</updateIndexItemRequest>

```

### Constraints

| field | constraints |
|---|---|
| `indexItems` | `@NotEmpty` |
| `indexItems[].fieldName` | `@NotBlank` |
| `indexItems[].id` | `@NotNull`, `@Positive` |
| `indexItems[].indexType` | `@NotNull` |
| `indexItems[].name` | `@NotBlank` |
| `indexItems[].searchCondition` | `@NotBlank` |
| `indexItems[].searchString` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <indexItems>
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
  </indexItems>
</cinnamon>

```


---

# /api/index/info
Provides information on the status of the Lucene search index

## Request

```xml
<indexInfoRequest>
  <countDocuments>true</countDocuments>
  <listFailedIndexJobs>false</listFailedIndexJobs>
  <listFailedTikaExtractions>false</listFailedTikaExtractions>
</indexInfoRequest>

```


## Response

```xml
<indexInfoResponse>
  <documentsInIndex>100</documentsInIndex>
  <foldersInIndex>20</foldersInIndex>
  <failedJobCount>2</failedJobCount>
  <failedIndexJobs>
    <failedIndexJobs>
      <id/>
      <jobType>OSD</jobType>
      <itemId>143</itemId>
      <failed>0</failed>
      <action>CREATE</action>
    </failedIndexJobs>
  </failedIndexJobs>
  <jobCount>41</jobCount>
  <failedTikaExtractionCount>1</failedTikaExtractionCount>
  <failedTikaExtractions>
    <failedTikaExtractions>
      <osdId>143</osdId>
      <attempts>3</attempts>
      <lastAttempt>2022-08-10T03:21:00</lastAttempt>
      <lastError>Tika answered with HTTP status 422</lastError>
    </failedTikaExtractions>
  </failedTikaExtractions>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `folderIds[]` | `@NotNull`, `@Positive` |
| `osdIds[]` | `@NotNull`, `@Positive` |
| `updateTikaMetaset` | `@NotNull` |


## Response

```xml
<reindexResponse>
  <documentsToIndex>1000</documentsToIndex>
  <foldersToIndex>123</foldersToIndex>
  <tikaMetasetsDeleted>1000</tikaMetasetsDeleted>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `languages` | `@NotEmpty` |
| `languages[].isoCode` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <languages>
    <language>
      <id>54</id>
      <isoCode>DOG</isoCode>
    </language>
  </languages>
</cinnamon>

```


---

# /api/language/delete


## Request

```xml
<deleteLanguageRequest>
  <ids>
    <id>999</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteLanguageRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <languages>
    <language>
      <id>54</id>
      <isoCode>DOG</isoCode>
    </language>
  </languages>
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

### Constraints

| field | constraints |
|---|---|
| `languages` | `@NotEmpty` |
| `languages[].id` | `@NotNull`, `@Positive` |
| `languages[].isoCode` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <languages>
    <language>
      <id>54</id>
      <isoCode>DOG</isoCode>
    </language>
  </languages>
</cinnamon>

```


---

# /api/lifecycleState/attachLifecycle
Only superusers may use forceChange parameter to attach any state without verification.

## Request

```xml
<attachLifecycleRequest>
  <osdId>4</osdId>
  <lifecycleId>6</lifecycleId>
  <lifecycleStateId>2</lifecycleStateId>
  <forceChange>true</forceChange>
</attachLifecycleRequest>

```

### Constraints

| field | constraints |
|---|---|
| `lifecycleId` | `@NotNull`, `@Positive` |
| `lifecycleStateId` | `@Positive` |
| `osdId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/lifecycleState/changeState


## Request

```xml
<changeLifecycleStateRequest>
  <osdId>5</osdId>
  <stateId>32</stateId>
</changeLifecycleStateRequest>

```

### Constraints

| field | constraints |
|---|---|
| `osdId` | `@NotNull`, `@Positive` |
| `stateId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `lifecycleStates` | `@NotEmpty` |
| `lifecycleStates[].config` | `@NotBlank` |
| `lifecycleStates[].name` | `@NotBlank` |
| `lifecycleStates[].stateClass` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <lifecycleStates>
    <lifecycleState>
      <id>79</id>
      <name>published</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>6</lifecycleId>
      <lifecycleStateForCopyId>77</lifecycleStateForCopyId>
    </lifecycleState>
  </lifecycleStates>
</cinnamon>

```


---

# /api/lifecycleState/delete


## Request

```xml
<deleteLifecycleStateRequest>
  <ids>
    <id>6</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteLifecycleStateRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/lifecycleState/detachLifecycle


## Request

```xml
<idRequest>
  <id>7</id>
</idRequest>

```

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/lifecycleState/get


## Request

```xml
<idRequest>
  <id>7</id>
</idRequest>

```

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <lifecycleStates>
    <lifecycleState>
      <id>79</id>
      <name>published</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>6</lifecycleId>
      <lifecycleStateForCopyId>77</lifecycleStateForCopyId>
    </lifecycleState>
  </lifecycleStates>
</cinnamon>

```


---

# /api/lifecycleState/getNextStates


## Request

```xml
<idRequest>
  <id>7</id>
</idRequest>

```

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <lifecycleStates>
    <lifecycleState>
      <id>79</id>
      <name>published</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>6</lifecycleId>
      <lifecycleStateForCopyId>77</lifecycleStateForCopyId>
    </lifecycleState>
  </lifecycleStates>
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
  <changeTriggerResponses/>
  <lifecycleStates>
    <lifecycleState>
      <id>79</id>
      <name>published</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>6</lifecycleId>
      <lifecycleStateForCopyId>77</lifecycleStateForCopyId>
    </lifecycleState>
  </lifecycleStates>
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

### Constraints

| field | constraints |
|---|---|
| `lifecycleStates` | `@NotEmpty` |
| `lifecycleStates[].config` | `@NotBlank` |
| `lifecycleStates[].id` | `@NotNull`, `@Positive` |
| `lifecycleStates[].name` | `@NotBlank` |
| `lifecycleStates[].stateClass` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <lifecycleStates>
    <lifecycleState>
      <id>79</id>
      <name>published</name>
      <config>&lt;config/></config>
      <stateClass>com.dewarim.cinnamon.lifecycle.ChangeAclState</stateClass>
      <lifecycleId>6</lifecycleId>
      <lifecycleStateForCopyId>77</lifecycleStateForCopyId>
    </lifecycleState>
  </lifecycleStates>
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

### Constraints

| field | constraints |
|---|---|
| `lifecycles` | `@NotEmpty` |
| `lifecycles[].defaultStateId` | `@Positive` |
| `lifecycles[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <lifecycles>
    <lifecycle>
      <id/>
      <name>review-lifecycle</name>
      <defaultStateId>1</defaultStateId>
      <lifecycleStates>
        <lifecycleState>
          <id>1</id>
          <name>needs-edits</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>1</lifecycleStateForCopyId>
        </lifecycleState>
        <lifecycleState>
          <id>2</id>
          <name>needs-review</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>2</lifecycleStateForCopyId>
        </lifecycleState>
        <lifecycleState>
          <id>3</id>
          <name>published</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>3</lifecycleStateForCopyId>
        </lifecycleState>
      </lifecycleStates>
    </lifecycle>
  </lifecycles>
</cinnamon>

```


---

# /api/lifecycle/delete
Delete lifecycles 

## Request

```xml
<deleteLifecycleRequest>
  <ids>
    <id>1024</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteLifecycleRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <lifecycles>
    <lifecycle>
      <id/>
      <name>review-lifecycle</name>
      <defaultStateId>1</defaultStateId>
      <lifecycleStates>
        <lifecycleState>
          <id>1</id>
          <name>needs-edits</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>1</lifecycleStateForCopyId>
        </lifecycleState>
        <lifecycleState>
          <id>2</id>
          <name>needs-review</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>2</lifecycleStateForCopyId>
        </lifecycleState>
        <lifecycleState>
          <id>3</id>
          <name>published</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>3</lifecycleStateForCopyId>
        </lifecycleState>
      </lifecycleStates>
    </lifecycle>
  </lifecycles>
</cinnamon>

```


---

# /api/lifecycle/update
Update lifecycles. Note: does not update lifecycle states 

## Request

```xml
<updateLifecycleRequest>
  <lifecycles>
    <lifecycle>
      <id/>
      <name>my new LC</name>
      <defaultStateId>5</defaultStateId>
      <lifecycleStates/>
    </lifecycle>
  </lifecycles>
</updateLifecycleRequest>

```

### Constraints

| field | constraints |
|---|---|
| `lifecycles` | `@NotEmpty` |
| `lifecycles[].defaultStateId` | `@Positive` |
| `lifecycles[].id` | `@NotNull`, `@Positive` |
| `lifecycles[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <lifecycles>
    <lifecycle>
      <id/>
      <name>review-lifecycle</name>
      <defaultStateId>1</defaultStateId>
      <lifecycleStates>
        <lifecycleState>
          <id>1</id>
          <name>needs-edits</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>1</lifecycleStateForCopyId>
        </lifecycleState>
        <lifecycleState>
          <id>2</id>
          <name>needs-review</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>2</lifecycleStateForCopyId>
        </lifecycleState>
        <lifecycleState>
          <id>3</id>
          <name>published</name>
          <config>&lt;config/></config>
          <stateClass>com.dewarim.cinnamon.provider.state.NopStateProvider</stateClass>
          <lifecycleId>1</lifecycleId>
          <lifecycleStateForCopyId>3</lifecycleStateForCopyId>
        </lifecycleState>
      </lifecycleStates>
    </lifecycle>
  </lifecycles>
</cinnamon>

```


---

# /api/link/create


## Request

```xml
<createLinkRequest>
  <links>
    <link>
      <id/>
      <type>OBJECT</type>
      <ownerId>1</ownerId>
      <aclId>1</aclId>
      <parentId>5</parentId>
      <folderId>2</folderId>
      <objectId>3</objectId>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
</createLinkRequest>

```
```xml
<createLinkRequest>
  <links>
    <link>
      <id/>
      <type>FOLDER</type>
      <ownerId>1</ownerId>
      <aclId>1</aclId>
      <parentId>5</parentId>
      <folderId>2</folderId>
      <objectId>3</objectId>
      <resolvedId/>
      <resolver>LATEST_HEAD</resolver>
    </link>
  </links>
</createLinkRequest>

```

### Constraints

| field | constraints |
|---|---|
| `links` | `@NotEmpty` |
| `links[].aclId` | `@NotNull`, `@Positive` |
| `links[].folderId` | `@Positive` |
| `links[].id` | `@Positive` |
| `links[].objectId` | `@Positive` |
| `links[].ownerId` | `@NotNull`, `@Positive` |
| `links[].parentId` | `@NotNull`, `@Positive` |
| `links[].type` | `@NotNull` |

**Also:** Every link points either at a folder or at an object - never at both, never at neither.


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <links>
    <link>
      <id>1</id>
      <type>OBJECT</type>
      <ownerId>2</ownerId>
      <aclId>3</aclId>
      <parentId>4</parentId>
      <folderId/>
      <objectId>123</objectId>
      <resolvedId/>
      <resolver>LATEST_HEAD</resolver>
    </link>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>2</ownerId>
      <aclId>3</aclId>
      <parentId>4</parentId>
      <folderId>321</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
</cinnamon>

```


---

# /api/link/delete


## Request

```xml
<deleteLinkRequest>
  <ids>
    <id>51</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteLinkRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/link/getLinksById


## Request

```xml
<getLinksRequest>
  <ids>
    <id>9</id>
  </ids>
  <includeSummary>true</includeSummary>
</getLinksRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <links>
    <link>
      <id/>
      <type/>
      <ownerId/>
      <aclId/>
      <parentId/>
      <folderId/>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
      <osd>
        <id>1</id>
        <name>my osd</name>
        <contentPath/>
        <contentSize/>
        <predecessorId/>
        <rootId/>
        <creatorId>1</creatorId>
        <modifierId>1</modifierId>
        <ownerId>3</ownerId>
        <lockerId/>
        <created>2022-08-10T03:21:00</created>
        <modified>2022-08-10T03:21:00</modified>
        <languageId>4</languageId>
        <aclId>5</aclId>
        <parentId>5</parentId>
        <formatId>23</formatId>
        <typeId>1</typeId>
        <folderPath/>
        <latestHead>false</latestHead>
        <latestBranch>true</latestBranch>
        <contentChanged>false</contentChanged>
        <metadataChanged>false</metadataChanged>
        <cmnVersion>1</cmnVersion>
        <lifecycleStateId/>
        <summary>&lt;summary/></summary>
        <contentHash/>
        <contentProvider>FILE_SYSTEM</contentProvider>
        <metasets/>
      </osd>
      <folder/>
    </link>
    <link>
      <id/>
      <type/>
      <ownerId/>
      <aclId/>
      <parentId/>
      <folderId/>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
      <osd/>
      <folder>
        <id>2</id>
        <name>images</name>
        <aclId>1</aclId>
        <ownerId>33</ownerId>
        <parentId>3</parentId>
        <typeId>5</typeId>
        <metadataChanged>false</metadataChanged>
        <summary>&lt;summary></summary>
        <folderPath/>
        <hasSubfolders>false</hasSubfolders>
        <created>2022-08-10T03:21:00</created>
        <metasets/>
      </folder>
    </link>
  </links>
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
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
</updateLinkRequest>

```

### Constraints

| field | constraints |
|---|---|
| `links` | `@NotEmpty` |
| `links[].aclId` | `@NotNull`, `@Positive` |
| `links[].folderId` | `@Positive` |
| `links[].id` | `@Positive` |
| `links[].objectId` | `@Positive` |
| `links[].ownerId` | `@NotNull`, `@Positive` |
| `links[].parentId` | `@NotNull`, `@Positive` |
| `links[].type` | `@NotNull` |

**Also:** Every link points either at a folder or at an object - never at both, never at neither.


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <links>
    <link>
      <id>1</id>
      <type>OBJECT</type>
      <ownerId>2</ownerId>
      <aclId>3</aclId>
      <parentId>4</parentId>
      <folderId/>
      <objectId>123</objectId>
      <resolvedId/>
      <resolver>LATEST_HEAD</resolver>
    </link>
    <link>
      <id>1</id>
      <type>FOLDER</type>
      <ownerId>2</ownerId>
      <aclId>3</aclId>
      <parentId>4</parentId>
      <folderId>321</folderId>
      <objectId/>
      <resolvedId/>
      <resolver>FIXED</resolver>
    </link>
  </links>
</cinnamon>

```


---

# /api/metasetType/create


## Request

```xml
<createMetasetTypeRequest>
  <metasetTypes>
    <metasetType>
      <id/>
      <name>tika</name>
      <unique>true</unique>
    </metasetType>
  </metasetTypes>
</createMetasetTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `metasetTypes` | `@NotEmpty` |
| `metasetTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <metasetTypes>
    <metasetType>
      <id>6</id>
      <name>license-key</name>
      <unique>true</unique>
    </metasetType>
  </metasetTypes>
</cinnamon>

```


---

# /api/metasetType/delete


## Request

```xml
<deleteMetasetTypeRequest>
  <ids>
    <id>7</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteMetasetTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <metasetTypes>
    <metasetType>
      <id>6</id>
      <name>license-key</name>
      <unique>true</unique>
    </metasetType>
  </metasetTypes>
</cinnamon>

```


---

# /api/metasetType/update


## Request

```xml
<updateMetasetTypeRequest>
  <metasetTypes>
    <metasetType>
      <id>1</id>
      <name>thumbnail</name>
      <unique>false</unique>
    </metasetType>
  </metasetTypes>
</updateMetasetTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `metasetTypes` | `@NotEmpty` |
| `metasetTypes[].id` | `@NotNull`, `@Positive` |
| `metasetTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <metasetTypes>
    <metasetType>
      <id>6</id>
      <name>license-key</name>
      <unique>true</unique>
    </metasetType>
  </metasetTypes>
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

### Constraints

| field | constraints |
|---|---|
| `objectTypes` | `@NotEmpty` |
| `objectTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <objectTypes>
    <objectType>
      <id>8</id>
      <name>image</name>
    </objectType>
  </objectTypes>
</cinnamon>

```


---

# /api/objectType/delete


## Request

```xml
<deleteObjectTypeRequest>
  <ids>
    <id>2</id>
    <id>3</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteObjectTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <objectTypes>
    <objectType>
      <id>8</id>
      <name>image</name>
    </objectType>
  </objectTypes>
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

### Constraints

| field | constraints |
|---|---|
| `objectTypes` | `@NotEmpty` |
| `objectTypes[].id` | `@NotNull`, `@Positive` |
| `objectTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <objectTypes>
    <objectType>
      <id>8</id>
      <name>image</name>
    </objectType>
  </objectTypes>
</cinnamon>

```


---

# /api/osd/copy


## Request

```xml
<copyOsdRequest>
  <sourceIds>
    <sourceId>1</sourceId>
    <sourceId>2</sourceId>
    <sourceId>3</sourceId>
  </sourceIds>
  <targetFolderId>20</targetFolderId>
  <metasetTypeIds>
    <metasetTypeId>13</metasetTypeId>
    <metasetTypeId>15</metasetTypeId>
    <metasetTypeId>2</metasetTypeId>
  </metasetTypeIds>
</copyOsdRequest>

```

### Constraints

| field | constraints |
|---|---|
| `metasetTypeIds[]` | `@NotNull`, `@Positive` |
| `sourceIds` | `@NotEmpty` |
| `sourceIds[]` | `@NotNull`, `@Positive` |
| `targetFolderId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <osds>
    <osd>
      <id>1</id>
      <name>my osd</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId>1</creatorId>
      <modifierId>1</modifierId>
      <ownerId>3</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>4</languageId>
      <aclId>5</aclId>
      <parentId>5</parentId>
      <formatId>23</formatId>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>false</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
  <links/>
  <references/>
  <relations/>
</cinnamon>

```


---

# /api/osd/copyToExisting


## Request

```xml
<copyToExistingOsdRequest>
  <copyTasks>
    <copyTask>
      <sourceOsdId>100</sourceOsdId>
      <targetOsdId>200</targetOsdId>
      <copyContent>true</copyContent>
      <metasetTypeIds>
        <metasetTypeId>12</metasetTypeId>
        <metasetTypeId>13</metasetTypeId>
      </metasetTypeIds>
    </copyTask>
  </copyTasks>
</copyToExistingOsdRequest>

```

### Constraints

| field | constraints |
|---|---|
| `copyTasks` | `@NotEmpty` |
| `copyTasks[].metasetTypeIds[]` | `@NotNull`, `@Positive` |
| `copyTasks[].sourceOsdId` | `@NotNull`, `@Positive` |
| `copyTasks[].targetOsdId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

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

### Constraints

| field | constraints |
|---|---|
| `metas` | `@NotEmpty` |
| `metas[].content` | `@NotNull` |
| `metas[].objectId` | `@NotNull`, `@Positive` |
| `metas[].typeId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <metasets>
    <metaset>
      <id/>
      <objectId>6</objectId>
      <typeId>8</typeId>
      <content>&lt;xml> folder meta object &lt;/xml></content>
    </metaset>
    <metaset>
      <id/>
      <objectId>7</objectId>
      <typeId>65</typeId>
      <content>&lt;xml> osd meta object &lt;/xml></content>
    </metaset>
  </metasets>
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
  <summary>&lt;summary>Optional fields: formatId, languageId, lifecycleStateId, summary, metas&lt;/summary></summary>
  <metasets>
    <metaset>
      <id/>
      <objectId>1</objectId>
      <typeId>2</typeId>
      <content>&lt;xml>some meta content&lt;/xml></content>
    </metaset>
  </metasets>
</createOsdRequest>

```

### Constraints

| field | constraints |
|---|---|
| `aclId` | `@Positive` |
| `formatId` | `@Positive` |
| `languageId` | `@Positive` |
| `lifecycleStateId` | `@Positive` |
| `metasets[].content` | `@NotNull` |
| `metasets[].objectId` | `@Positive` |
| `metasets[].typeId` | `@NotNull`, `@Positive` |
| `name` | `@NotBlank` |
| `ownerId` | `@NotNull`, `@Positive` |
| `parentId` | `@NotNull`, `@Positive` |
| `summary` | `@NotBlank` |
| `typeId` | `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <osds>
    <osd>
      <id>1</id>
      <name>my osd</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId>1</creatorId>
      <modifierId>1</modifierId>
      <ownerId>3</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>4</languageId>
      <aclId>5</aclId>
      <parentId>5</parentId>
      <formatId>23</formatId>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>false</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
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
  <ids>
    <id>4</id>
    <id>6</id>
  </ids>
  <deleteDescendants>true</deleteDescendants>
  <deleteAllVersions>true</deleteAllVersions>
</deleteOsdRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/osd/deleteAllMetas
    Delete all metasets linked to the given OSD ids. Parameter ignoreNotFound is not used.


## Request

```xml
<deleteAllMetasRequest>
  <ids>
    <id>14</id>
    <id>15</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteAllMetasRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/osd/deleteMeta
Delete the OSD metasets with the given meta ids.

## Request

```xml
<deleteMetaRequest>
  <ids>
    <id>3</id>
    <id>5</id>
    <id>6</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteMetaRequest>

```
```xml
<deleteMetaRequest>
  <ids>
    <id>1</id>
  </ids>
  <ignoreNotFound>true</ignoreNotFound>
</deleteMetaRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
</cinnamon>

```


---

# /api/osd/getContent
Returns an OSD's content according to it's format's content type.

## Request

```xml
<idRequest>
  <id>7</id>
</idRequest>

```

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |


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

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |
| `typeIds[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <metasets>
    <metaset>
      <id/>
      <objectId>6</objectId>
      <typeId>8</typeId>
      <content>&lt;xml> folder meta object &lt;/xml></content>
    </metaset>
    <metaset>
      <id/>
      <objectId>7</objectId>
      <typeId>65</typeId>
      <content>&lt;xml> osd meta object &lt;/xml></content>
    </metaset>
  </metasets>
</cinnamon>

```


---

# /api/osd/getObjectsByFolderId


## Request

```xml
<osdByFolderRequest>
  <includeSummary>false</includeSummary>
  <folderId>6</folderId>
  <linksAsOsd>true</linksAsOsd>
  <includeCustomMetadata>true</includeCustomMetadata>
  <versionPredicate>BRANCH</versionPredicate>
</osdByFolderRequest>

```

### Constraints

| field | constraints |
|---|---|
| `folderId` | `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <osds>
    <osd>
      <id>1</id>
      <name>my osd</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId>1</creatorId>
      <modifierId>1</modifierId>
      <ownerId>3</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>4</languageId>
      <aclId>5</aclId>
      <parentId>5</parentId>
      <formatId>23</formatId>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>false</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
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
  <ids>
    <id>45</id>
    <id>23</id>
    <id>2</id>
  </ids>
  <includeSummary>true</includeSummary>
  <includeCustomMetadata>true</includeCustomMetadata>
  <addFolderPath>true</addFolderPath>
</osdRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <osds>
    <osd>
      <id>1</id>
      <name>my osd</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId>1</creatorId>
      <modifierId>1</modifierId>
      <ownerId>3</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>4</languageId>
      <aclId>5</aclId>
      <parentId>5</parentId>
      <formatId>23</formatId>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>false</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
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
    <id>1</id>
    <id>32</id>
    <id>4</id>
  </ids>
  <includeMetadata>false</includeMetadata>
</getRelationsRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <relations>
    <relation>
      <id>399</id>
      <leftId>1</leftId>
      <rightId>4</rightId>
      <typeId>1</typeId>
      <metadata>&lt;generatedBy>PDF Renderer&lt;/generatedBy</metadata>
      <isParent/>
      <isChild/>
    </relation>
  </relations>
</cinnamon>

```


---

# /api/osd/getSummaries


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>5</id>
    <id>44</id>
  </ids>
</idListRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <summaries>
    <summary>
      <id>5</id>
      <content>&lt;summary> a summary of an OSD's content &lt;/summary></content>
    </summary>
  </summaries>
</cinnamon>

```


---

# /api/osd/lock


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>5</id>
    <id>44</id>
  </ids>
</idListRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `formatId` | `@NotNull`, `@Positive` |
| `id` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `id` | `@NotNull`, `@Positive` |
| `summary` | `@NotNull` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/osd/unlock


## Request

```xml
<idListRequest>
  <ids>
    <id>1</id>
    <id>5</id>
    <id>44</id>
  </ids>
</idListRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/osd/update


## Request

```xml
<updateOsdRequest>
  <osds>
    <osd>
      <id>1</id>
      <name>new name</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId/>
      <modifierId/>
      <ownerId>45</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>1</languageId>
      <aclId>56</aclId>
      <parentId>2</parentId>
      <formatId/>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>true</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
  <updateContentChanged>false</updateContentChanged>
  <updateMetadataChanged>false</updateMetadataChanged>
</updateOsdRequest>

```
```xml
<updateOsdRequest>
  <osds>
    <osd>
      <id>1</id>
      <name>new name</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId/>
      <modifierId/>
      <ownerId>45</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>1</languageId>
      <aclId>56</aclId>
      <parentId>2</parentId>
      <formatId/>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>true</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
  <updateContentChanged>true</updateContentChanged>
  <updateMetadataChanged>true</updateMetadataChanged>
</updateOsdRequest>

```

### Constraints

| field | constraints |
|---|---|
| `osds` | `@NotEmpty` |
| `osds[].aclId` | `@Positive` |
| `osds[].id` | `@NotNull`, `@Positive` |
| `osds[].languageId` | `@Positive` |
| `osds[].name` | `@Pattern(flags = [DOTALL], regexp = "\S(.*\S)?")` |
| `osds[].ownerId` | `@Positive` |
| `osds[].parentId` | `@Positive` |
| `osds[].typeId` | `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/osd/updateMetaContent
Update the content of a given OSD metaset

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

### Constraints

| field | constraints |
|---|---|
| `metas` | `@NotEmpty` |
| `metas[].content` | `@NotNull` |
| `metas[].id` | `@NotNull`, `@Positive` |
| `metas[].objectId` | `@NotNull`, `@Positive` |
| `metas[].typeId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/osd/version
Create a new version of an OSD. Requires a multipart-mime request, with part "cinnamonRequest" and optional
part "file", if the new version should contain data.


## Request

```xml
<createNewVersionRequest>
  <id>5</id>
  <metaRequests>
    <metaRequest>
      <content>&lt;xml>new metadata&lt;/xml></content>
      <typeId>1</typeId>
    </metaRequest>
  </metaRequests>
  <formatId>4</formatId>
</createNewVersionRequest>

```
```xml
<createNewVersionRequest>
  <id>6</id>
  <metaRequests/>
  <formatId/>
</createNewVersionRequest>

```

### Constraints

| field | constraints |
|---|---|
| `formatId` | `@Positive` |
| `id` | `@NotNull`, `@Positive` |
| `metaRequests[].content` | `@NotNull` |
| `metaRequests[].typeId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <osds>
    <osd>
      <id>1</id>
      <name>my osd</name>
      <contentPath/>
      <contentSize/>
      <predecessorId/>
      <rootId/>
      <creatorId>1</creatorId>
      <modifierId>1</modifierId>
      <ownerId>3</ownerId>
      <lockerId/>
      <created>2022-08-10T03:21:00</created>
      <modified>2022-08-10T03:21:00</modified>
      <languageId>4</languageId>
      <aclId>5</aclId>
      <parentId>5</parentId>
      <formatId>23</formatId>
      <typeId>1</typeId>
      <folderPath/>
      <latestHead>false</latestHead>
      <latestBranch>true</latestBranch>
      <contentChanged>false</contentChanged>
      <metadataChanged>false</metadataChanged>
      <cmnVersion>1</cmnVersion>
      <lifecycleStateId/>
      <summary>&lt;summary/></summary>
      <contentHash/>
      <contentProvider>FILE_SYSTEM</contentProvider>
      <metasets/>
    </osd>
  </osds>
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
  <aclGroupId>3</aclGroupId>
  <addPermissions>
    <addId>4</addId>
    <addId>5</addId>
    <addId>6</addId>
  </addPermissions>
  <removePermissions>
    <removeId>7</removeId>
    <removeId>8</removeId>
    <removeId>9</removeId>
  </removePermissions>
</changePermissionsRequest>

```

### Constraints

| field | constraints |
|---|---|
| `aclGroupId` | `@NotNull`, `@Positive` |
| `add[]` | `@NotNull`, `@Positive` |
| `remove[]` | `@NotNull`, `@Positive` |

**Also:** At least one of addPermissions and removePermissions has to be non-empty.


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/permission/getUserPermissions


## Request

```xml
<userPermissionRequest>
  <userId>6</userId>
  <aclId>7</aclId>
</userPermissionRequest>

```

### Constraints

| field | constraints |
|---|---|
| `aclId` | `@NotNull`, `@Positive` |
| `userId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <permissions>
    <permission>
      <id>98</id>
      <name>log viewer permission</name>
    </permission>
    <permission>
      <id>99</id>
      <name>log writer permission </name>
    </permission>
  </permissions>
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
  <changeTriggerResponses/>
  <permissions>
    <permission>
      <id>98</id>
      <name>log viewer permission</name>
    </permission>
    <permission>
      <id>99</id>
      <name>log writer permission </name>
    </permission>
  </permissions>
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

### Constraints

| field | constraints |
|---|---|
| `relationTypes` | `@NotEmpty` |
| `relationTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <relationTypes>
    <relationType>
      <id/>
      <leftObjectProtected>true</leftObjectProtected>
      <rightObjectProtected>false</rightObjectProtected>
      <name>html-to-image</name>
      <cloneOnRightCopy>false</cloneOnRightCopy>
      <cloneOnLeftCopy>false</cloneOnLeftCopy>
      <cloneOnLeftVersion>false</cloneOnLeftVersion>
      <cloneOnRightVersion>false</cloneOnRightVersion>
    </relationType>
  </relationTypes>
</cinnamon>

```


---

# /api/relationType/delete


## Request

```xml
<deleteRelationTypeRequest>
  <ids>
    <id>333</id>
    <id>543</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteRelationTypeRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <relationTypes>
    <relationType>
      <id/>
      <leftObjectProtected>true</leftObjectProtected>
      <rightObjectProtected>false</rightObjectProtected>
      <name>html-to-image</name>
      <cloneOnRightCopy>false</cloneOnRightCopy>
      <cloneOnLeftCopy>false</cloneOnLeftCopy>
      <cloneOnLeftVersion>false</cloneOnLeftVersion>
      <cloneOnRightVersion>false</cloneOnRightVersion>
    </relationType>
  </relationTypes>
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

### Constraints

| field | constraints |
|---|---|
| `relationTypes` | `@NotEmpty` |
| `relationTypes[].id` | `@NotNull`, `@Positive` |
| `relationTypes[].name` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <relationTypes>
    <relationType>
      <id/>
      <leftObjectProtected>true</leftObjectProtected>
      <rightObjectProtected>false</rightObjectProtected>
      <name>html-to-image</name>
      <cloneOnRightCopy>false</cloneOnRightCopy>
      <cloneOnLeftCopy>false</cloneOnLeftCopy>
      <cloneOnLeftVersion>false</cloneOnLeftVersion>
      <cloneOnRightVersion>false</cloneOnRightVersion>
    </relationType>
  </relationTypes>
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
      <isParent/>
      <isChild/>
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
      <isParent/>
      <isChild/>
    </relation>
  </relations>
</createRelationRequest>

```

### Constraints

| field | constraints |
|---|---|
| `relations` | `@NotEmpty` |
| `relations[].leftId` | `@NotNull`, `@Positive` |
| `relations[].metadata` | `@NotNull` |
| `relations[].rightId` | `@NotNull`, `@Positive` |
| `relations[].typeId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <relations>
    <relation>
      <id>399</id>
      <leftId>1</leftId>
      <rightId>4</rightId>
      <typeId>1</typeId>
      <metadata>&lt;generatedBy>PDF Renderer&lt;/generatedBy</metadata>
      <isParent/>
      <isChild/>
    </relation>
  </relations>
</cinnamon>

```


---

# /api/relation/delete


## Request

```xml
<deleteRelationRequest>
  <ids>
    <id>68</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteRelationRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `leftIds[]` | `@NotNull`, `@Positive` |
| `relationTypeIds[]` | `@NotNull`, `@Positive` |
| `rightIds[]` | `@NotNull`, `@Positive` |

**Also:** At least one of leftIds, rightIds and relationTypeIds has to be non-empty.


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <relations>
    <relation>
      <id>399</id>
      <leftId>1</leftId>
      <rightId>4</rightId>
      <typeId>1</typeId>
      <metadata>&lt;generatedBy>PDF Renderer&lt;/generatedBy</metadata>
      <isParent/>
      <isChild/>
    </relation>
  </relations>
</cinnamon>

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

### Constraints

| field | constraints |
|---|---|
| `query` | `@NotEmpty` |
| `searchType` | `@NotNull` |


## Response

```xml
<searchIdsResponse>
  <changeTriggerResponses/>
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

# /api/test/boom
generate an error to test error handling

---

# /api/test/echo
return the posted input xml

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

### Constraints

| field | constraints |
|---|---|
| `uiLanguages` | `@NotEmpty` |
| `uiLanguages[].isoCode` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <uiLanguages>
    <uiLanguage>
      <id>54</id>
      <isoCode>DOG</isoCode>
    </uiLanguage>
  </uiLanguages>
</cinnamon>

```


---

# /api/uiLanguage/delete


## Request

```xml
<deleteUiLanguageRequest>
  <ids>
    <id>90</id>
  </ids>
  <ignoreNotFound>false</ignoreNotFound>
</deleteUiLanguageRequest>

```

### Constraints

| field | constraints |
|---|---|
| `ids` | `@NotEmpty` |
| `ids[]` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <success>false</success>
  <changeTriggerResponses/>
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
  <changeTriggerResponses/>
  <uiLanguages>
    <uiLanguage>
      <id>54</id>
      <isoCode>DOG</isoCode>
    </uiLanguage>
  </uiLanguages>
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

### Constraints

| field | constraints |
|---|---|
| `uiLanguages` | `@NotEmpty` |
| `uiLanguages[].id` | `@NotNull`, `@Positive` |
| `uiLanguages[].isoCode` | `@NotBlank` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
  <uiLanguages>
    <uiLanguage>
      <id>54</id>
      <isoCode>DOG</isoCode>
    </uiLanguage>
  </uiLanguages>
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
      <config>&lt;config/></config>
      <groupIds>
        <groupId>12</groupId>
      </groupIds>
    </userAccount>
  </userAccounts>
</createUserAccountRequest>

```

### Constraints

| field | constraints |
|---|---|
| `userAccounts` | `@NotEmpty` |
| `userAccounts[].config` | `@NotNull` |
| `userAccounts[].email` | `@NotEmpty` |
| `userAccounts[].fullname` | `@NotEmpty` |
| `userAccounts[].groupIds[]` | `@NotNull`, `@Positive` |
| `userAccounts[].loginType` | `@NotEmpty` |
| `userAccounts[].name` | `@NotEmpty` |
| `userAccounts[].password` | `@NotEmpty` |
| `userAccounts[].uiLanguageId` | `@NotNull`, `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
      <config>&lt;config/></config>
      <groupIds>
        <groupId>3</groupId>
        <groupId>5</groupId>
      </groupIds>
    </user>
  </users>
</cinnamon>

```


---

# /api/user/delete
Delete a user and transfer all his remaining assets to another user.
Note: this is not optimized for users who own vast collections of objects or folders.


## Request

```xml
<deleteUserAccountRequest>
  <userId>4</userId>
  <assetReceiverId>5</assetReceiverId>
</deleteUserAccountRequest>

```

### Constraints

| field | constraints |
|---|---|
| `assetReceiverId` | `@NotNull`, `@Positive` |
| `userId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

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

### Constraints

| field | constraints |
|---|---|
| `userId` | `@Positive` |

**Also:** At least one of userId and username has to be set.


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
      <config>&lt;config/></config>
      <groupIds>
        <groupId>3</groupId>
        <groupId>5</groupId>
      </groupIds>
    </user>
  </users>
</cinnamon>

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
<cinnamon>
  <changeTriggerResponses/>
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
      <config>&lt;config/></config>
      <groupIds>
        <groupId>3</groupId>
        <groupId>5</groupId>
      </groupIds>
    </user>
  </users>
</cinnamon>

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

### Constraints

| field | constraints |
|---|---|
| `config` | `@NotNull` |
| `userId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
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

### Constraints

| field | constraints |
|---|---|
| `password` | `@NotNull` |
| `userId` | `@NotNull`, `@Positive` |


## Response

```xml
<genericResponse>
  <message/>
  <successful>true</successful>
  <changeTriggerResponses/>
</genericResponse>

```


---

# /api/user/update
Update a user's account. This also updates the user's groups,
 so you must make sure that the userAccount objects contain the desired list of groupIds.


## Request

```xml
<updateUserAccountRequest>
  <userAccounts>
    <userAccount>
      <id/>
      <name>foo</name>
      <loginType>CINNAMON</loginType>
      <password>secretPassword</password>
      <activated>true</activated>
      <locked>false</locked>
      <uiLanguageId>1</uiLanguageId>
      <fullname>Mr Foo Bar</fullname>
      <email>foo@example.com</email>
      <changeTracking>true</changeTracking>
      <activateTriggers>true</activateTriggers>
      <passwordExpired>false</passwordExpired>
      <config>&lt;config/></config>
      <groupIds>
        <groupId>10</groupId>
        <groupId>12</groupId>
        <groupId>45</groupId>
      </groupIds>
    </userAccount>
  </userAccounts>
</updateUserAccountRequest>

```

### Constraints

| field | constraints |
|---|---|
| `userAccounts` | `@NotEmpty` |
| `userAccounts[].config` | `@NotNull` |
| `userAccounts[].fullname` | `@NotEmpty` |
| `userAccounts[].groupIds[]` | `@NotNull`, `@Positive` |
| `userAccounts[].id` | `@NotNull`, `@Positive` |
| `userAccounts[].loginType` | `@NotEmpty` |
| `userAccounts[].name` | `@NotEmpty` |
| `userAccounts[].uiLanguageId` | `@Positive` |


## Response

```xml
<cinnamon>
  <changeTriggerResponses/>
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
      <config>&lt;config/></config>
      <groupIds>
        <groupId>3</groupId>
        <groupId>5</groupId>
      </groupIds>
    </user>
  </users>
</cinnamon>

```


---

