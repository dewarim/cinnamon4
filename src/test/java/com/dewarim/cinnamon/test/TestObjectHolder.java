package com.dewarim.cinnamon.test;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.client.CinnamonClientException;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.request.osd.CreateNewVersionRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;
import static com.dewarim.cinnamon.api.Constants.FOLDER_TYPE_DEFAULT;

public class TestObjectHolder {

    static final  Object            SYNC_OBJECT = new Object();
    public static Long              defaultCreationFolderId;
    /**
     * The default ACL with no permissions for a normal user
     */
    public static Acl               defaultAcl;
    /**
     * The default creation ACL with all permissions for a normal user
     */
    public static Acl               defaultCreationAcl;
    static        boolean           initialized = false;
    static public List<Permission>  permissions;
    static public List<Format>      formats;
    static public List<Group>       groups;
    static public List<Language>    languages;
    static public List<ObjectType>  objectTypes;
    static public List<MetasetType> metasetTypes;
    static public List<FolderType>  folderTypes;
    static public List<Acl>         acls;

    CinnamonClient client;
    public ObjectSystemData osd;
    public Acl              acl;
    public Group            group;
    public AclGroup         aclGroup;
    public UserAccount      user;
    public Folder           folder;
    public FolderType       folderType;
    public ObjectType       objectType;
    public Format           format;
    public Language         language;
    public LifecycleState   lifecycleState;
    public Meta             meta;
    public Link             link;

    public List<Meta>   metas;
    public String       summary = DEFAULT_SUMMARY;
    public MetasetType  metasetType;
    public Relation     relation;
    public RelationType relationType;
    public String       newUserPassword;
    public Lifecycle    lifecycle;

    /**
     * Initialize a new TestObjectHolder with default values, using the given client
     * with its configured user as background client to perform requests.
     * <p>
     * This results in a bare-bones TOH without a set user/folder/acl, ideal for admin tasks.
     */
    public TestObjectHolder(CinnamonClient client) {
        this.client = client;
        initialize();
        objectType = objectTypes.stream().filter(type ->
                type.getName().equals(Constants.OBJTYPE_DEFAULT)).findFirst().orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        folderType = folderTypes.stream().filter(type ->
                type.getName().equals(FOLDER_TYPE_DEFAULT)).findFirst().orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
    }

    /**
     * Initialize a new TestObjectHolder with default values, using the given client
     * as background client to perform requests, but set acl and user  to be used
     * for requests separately.
     * <p>
     * This results in a TOH ready to create objects and folders and such which require an ACL,
     * base folder and user (for ownerId etc)
     */
    public TestObjectHolder(CinnamonClient client, Long userId) throws IOException {
        this.client = client;
        setAcl(defaultCreationAcl);
        setUser(userId);
        // during initial test class setup, defaultCreationFolderId is still null.
        if (defaultCreationFolderId != null) {
            setFolder(defaultCreationFolderId);
        }
        initialize();
        objectType = objectTypes.stream().filter(type ->
                type.getName().equals(Constants.OBJTYPE_DEFAULT)).findFirst().orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        folderType = folderTypes.stream().filter(type ->
                type.getName().equals(FOLDER_TYPE_DEFAULT)).findFirst().orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
    }

    private void initialize() {
        if (!initialized) {
            synchronized (SYNC_OBJECT) {
                try {
                    permissions  = client.listPermissions();
                    formats      = client.listFormats();
                    objectTypes  = client.listObjectTypes();
                    metasetTypes = client.listMetasetTypes();
                    folderTypes  = client.listFolderTypes();
                    languages    = client.listLanguages();
                    acls         = client.listAcls();
                    groups       = client.listGroups();
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to initialize test object holder", e);
                }
                initialized = true;
            }
        }
        language = languages.get(0);
    }

    public TestObjectHolder setAcl(Acl acl) {
        this.acl = acl;
        return this;
    }

    public String createRandomName() {
        return UUID.randomUUID().toString();
    }

    public TestObjectHolder createOsd() throws IOException {
        return createOsd(createRandomName());
    }

    public TestObjectHolder unlockOsd() throws IOException {
        return unlockOsd(this.osd.getId());
    }

    public TestObjectHolder unlockOsd(Long id) throws IOException {
        client.unlockOsd(id);
        return this;
    }


    public TestObjectHolder createOsd(String name) throws IOException {
        CreateOsdRequest request = new CreateOsdRequest(name, folder.getId(), user.getId(), acl.getId(),
                objectType.getId(),
                format != null ? format.getId() : null,
                language != null ? language.getId() : null,
                lifecycleState != null ? lifecycleState.getId() : null,
                summary);
        if (metas != null) {
            request.setMetas(metas);
        }
        osd = client.createOsd(request);
        return this;
    }

    public TestObjectHolder createOsdWithContent(String name, Format format, File content) throws IOException {
        CreateOsdRequest request = new CreateOsdRequest(name, folder.getId(), user.getId(), acl.getId(),
                objectType.getId(),
                format != null ? format.getId() : null,
                language != null ? language.getId() : null,
                lifecycleState != null ? lifecycleState.getId() : null,
                summary);
        if (metas != null) {
            request.setMetas(metas);
        }
        osd = client.createOsdWithContent(request, content);
        return this;
    }

    public TestObjectHolder createFolder(Long parentId) throws IOException {
        return createFolder(createRandomName(), parentId);
    }

    public TestObjectHolder createFolder() throws IOException {
        Objects.requireNonNull(folder);
        return createFolder(createRandomName(), folder.getId());
    }

    public TestObjectHolder createFolderType() throws IOException {
        folderType = client.createFolderTypes(List.of(createRandomName())).get(0);
        return this;
    }

    public TestObjectHolder createFolder(String name, Long parentId) throws IOException {
        var defaultFolderType = folderTypes.stream().filter(ft -> ft.getName().equals(FOLDER_TYPE_DEFAULT)).findFirst().orElseThrow();
        folder = client.createFolder(parentId, name, user.getId(), acl.getId(), defaultFolderType.getId());
        return this;
    }

    public TestObjectHolder createAcl(String name) throws IOException {
        acl = client.createAcls(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createAcl() throws IOException {
        return createAcl(createRandomName());
    }

    public TestObjectHolder createGroup(String name) throws IOException {
        group = client.createGroupsByName(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createGroup() throws IOException {
        return createGroup(createRandomName());
    }

    public TestObjectHolder createGroup(Long parentId) throws IOException {
        group = client.createGroup(new Group(createRandomName(), parentId));
        return this;
    }

    public TestObjectHolder createAclGroup() throws IOException {
        aclGroup = client.createAclGroups(List.of(new AclGroup(acl.getId(), group.getId()))).get(0);
        return this;
    }

    public TestObjectHolder createAclGroupWithPermissionIds(List<Long> permissionIds) throws IOException {
        AclGroup group1 = new AclGroup(acl.getId(), group.getId());
        group1.getPermissionIds().addAll(permissionIds);
        aclGroup = client.createAclGroups(List.of(group1)).get(0);
        return this;
    }

    public TestObjectHolder addPermissionsByName(List<String> permissionsToAdd) throws IOException {
        addAndRemovePermissionsByName(permissionsToAdd, List.of());
        return this;
    }

    public TestObjectHolder addAndRemovePermissionsByName(List<String> permissionsToAdd, List<String> permissionsToRemove) throws IOException {
        if (permissionsToAdd.isEmpty() && permissionsToRemove.isEmpty()) {
            // do nothing
            return this;
        }
        client.addAndRemovePermissions(aclGroup.getId(),
                permissions.stream().filter(p -> permissionsToAdd.contains(p.getName())).map(Permission::getId).collect(Collectors.toList()),
                permissions.stream().filter(p -> permissionsToRemove.contains(p.getName())).map(Permission::getId).collect(Collectors.toList()));
        return this;
    }

    public TestObjectHolder addPermissions(List<DefaultPermission> permissionList) throws IOException {
        addAndRemovePermissionsByName(permissionList.stream().map(DefaultPermission::getName).toList(), List.of());
        return this;
    }

    public TestObjectHolder removePermissions(List<DefaultPermission> permissionList) throws IOException {
        addAndRemovePermissionsByName(List.of(), permissionList.stream().map(DefaultPermission::getName).toList());
        return this;
    }

    public TestObjectHolder setUser(Long id) throws IOException {
        user = client.getUser(id);
        return this;
    }

    public TestObjectHolder setFolder(Long id) throws IOException {
        if (id != null) {
            folder = client.getFolders(List.of(id), true).get(0);
        }
        return this;
    }

    public TestObjectHolder setMetas(List<Meta> metas) {
        this.metas = metas;
        return this;
    }

    public static MetasetType getMetasetType(String name) {
        return metasetTypes.stream().filter(type ->
                        type.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Metaset with name " + name + " is unknown."));
    }


    public TestObjectHolder lockOsd(Long id) throws IOException {
        client.lockOsd(id);
        return this;
    }

    public TestObjectHolder lockOsd() throws IOException {
        client.lockOsd(osd.getId());
        return this;
    }

    public TestObjectHolder addUserToGroup(long userId) throws IOException {
        client.addUserToGroups(userId, List.of(group.getId()));
        return this;
    }

    public TestObjectHolder setAclOnFolder(Long aclId, Long folderId) throws IOException {
        UpdateFolderRequest request = new UpdateFolderRequest(folderId, null, null, null, null, aclId);
        client.updateFolder(request);
        return this;
    }

    public TestObjectHolder createObjectType(String name) throws IOException {
        objectType = client.createObjectTypes(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createObjectType() throws IOException {
        return createObjectType(createRandomName());
    }

    public TestObjectHolder createOsdMeta(String content) throws IOException {
        meta = client.createOsdMeta(osd.getId(), content, Objects.requireNonNullElse(metasetType, metasetTypes.get(0)).getId());
        return this;
    }

    public TestObjectHolder createFolderMeta(String content) throws IOException {
        meta = client.createFolderMeta(folder.getId(), content, Objects.requireNonNullElse(metasetType, metasetTypes.get(0)).getId()).get(0);
        return this;
    }

    /**
     * Create a link to the given osd using the current folder, acl, owner.
     * New link object is stored in TOH.link field.
     */
    public TestObjectHolder createLinkToOsd(ObjectSystemData osd) throws IOException {
        link = client.createLinkToOsd(folder.getId(), acl.getId(), user.getId(), osd.getId());
        return this;
    }

    public TestObjectHolder createLinkToOsd() throws IOException {
        link = client.createLinkToOsd(folder.getId(), acl.getId(), user.getId(), osd.getId());
        return this;
    }

    public TestObjectHolder createLifecycle() throws IOException {
        lifecycle = client.createLifecycle(createRandomName());
        return this;
    }

    public TestObjectHolder attachLifecycle() throws IOException {
        client.attachLifecycle(osd.getId(), lifecycle.getId(), lifecycleState.getId(), true);
        return this;
    }

    public TestObjectHolder loadOsd(Long osdId) throws IOException {
        osd = client.getOsdById(osdId, true, true);
        return this;
    }


    public TestObjectHolder createLifecycleState(LifecycleState lcs) throws IOException {
        lifecycleState = client.createLifecycleState(lcs);
        return this;
    }

    /**
     * Create a link to the given folder using the current folder, acl, owner.
     * New link object is stored in TOH.link field.
     */
    public TestObjectHolder createLinkToFolder(Folder folder) throws IOException {
        link = client.createLinkToFolder(folder.getId(), acl.getId(), user.getId(), folder.getId());
        return this;
    }

    public List<Acl> getAcls() {
        return acls;
    }

    public TestObjectHolder setMetasetType(MetasetType metasetType) {
        this.metasetType = metasetType;
        return this;
    }

    public TestObjectHolder setGroup(Group group) {
        this.group = group;
        return this;
    }

    public TestObjectHolder setSummaryOnFolder(String foo) throws IOException {
        client.setFolderSummary(folder.getId(), foo);
        return this;
    }

    public TestObjectHolder setSummaryOnOsd(String foo) throws IOException {
        client.setSummary(osd.getId(), foo);
        return this;
    }

    public TestObjectHolder createMetaSetType(boolean unique) throws IOException {
        createMetaSetType(createRandomName(), unique);
        return this;
    }

    public TestObjectHolder createMetaSetType(String name, boolean unique) throws IOException {
        metasetType = client.createMetasetType(name, unique);
        return this;
    }

    /**
     * Create a relation type with random name and all flags set to true
     */
    public TestObjectHolder createRelationType() throws IOException {
        relationType = client.createRelationType(new RelationType(createRandomName(), true, true, true, true, true, true));
        return this;
    }

    public TestObjectHolder createRelation(Long rightId, String metadata) throws IOException {
        relation = client.createRelation(osd.getId(), rightId, relationType.getId(), metadata);
        return this;
    }

    public TestObjectHolder setAclByNameOnOsd(String aclName) throws IOException {
        var myAcl = client.listAcls().stream().filter(acl -> acl.getName().equals(aclName))
                .findFirst().orElseThrow(() -> new CinnamonClientException("Could not find ACL: " + aclName));
        client.updateOsd(new UpdateOsdRequest(osd.getId(), null, null, null, myAcl.getId(), null, null, false, false));
        osd = client.getOsdById(osd.getId(), true, true);
        return this;
    }

    public TestObjectHolder createUser() throws IOException {
        newUserPassword = createRandomName();
        user            = client.createUser(new UserAccount(createRandomName(), newUserPassword, "-", "-", 1L, LoginType.CINNAMON.name(), true, true, true));
        return this;
    }

    public TestObjectHolder deleteUser(Long userId, Long assetReceiverId) throws IOException {
        client.deleteUser(userId, assetReceiverId);
        return this;
    }

    public void reloadLogging() throws IOException {
        client.reloadLogging();
    }

    public TestObjectHolder version() throws IOException {
        return version(this.osd.getId());

    }

    public TestObjectHolder version(Long id) throws IOException {
        this.osd = client.version(new CreateNewVersionRequest(id));
        return this;
    }

    public TestObjectHolder deleteOsd() throws IOException {
        return deleteOsd(osd.getId());
    }

    public TestObjectHolder deleteOsd(Long id) throws IOException {
        client.deleteOsd(id);
        return this;
    }

    public TestObjectHolder deleteFolder() throws IOException {
        deleteFolder(folder.getId());
        return this;
    }


    /**
     * Delete folder, without recursion into sub folders or deleting content (will fail if content/subfolders exist)
     */
    public TestObjectHolder deleteFolder(Long folderId) throws IOException {
        client.deleteFolder(folderId, false, false);
        return this;
    }

    public TestObjectHolder updateLifecycleDefaultState() throws IOException {
        lifecycle.setDefaultStateId(lifecycleState.getId());
        client.updateLifecycle(lifecycle);
        return this;
    }

    public TestObjectHolder deleteAclGroup() throws IOException {
        client.deleteAclGroups(Collections.singletonList(this.aclGroup.getId()));
        return this;
    }
}
