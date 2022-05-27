package com.dewarim.cinnamon.test;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.FolderType;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.links.Link;
import com.dewarim.cinnamon.model.links.LinkType;
import com.dewarim.cinnamon.model.request.folder.UpdateFolderRequest;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.dewarim.cinnamon.api.Constants.FOLDER_TYPE_DEFAULT;

public class TestObjectHolder {

    static final  Object            SYNC_OBJECT = new Object();
    static        boolean           initialized = false;
    static public List<Permission>  permissions;
    static public List<Format>      formats;
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

    public List<Meta> metas;
    public String     summary = "<summary/>";

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
     * as background client to perform requests, but set acl, user and createFolder to be used
     * for requests separately.
     * <p>
     * This results in a TOH ready to create objects and folders and such which require an ACL,
     * base folder and user (for ownerId etc)
     */
    public TestObjectHolder(CinnamonClient client, String aclName, Long userId, Long createFolderId) throws IOException {
        this.client = client;
        if (aclName != null) {
            this.acl = client.getAclByName(aclName);
        }
        setUser(userId);
        setFolder(createFolderId);
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
                    permissions = client.listPermissions();
                    formats = client.listFormats();
                    objectTypes = client.listObjectTypes();
                    metasetTypes = client.listMetasetTypes();
                    folderTypes = client.listFolderTypes();
                    languages = client.listLanguages();
                    acls = client.listAcls();
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

    public TestObjectHolder createFolder(String name, Long parentId) throws IOException {
        var defaultFolderType = folderTypes.stream().filter(ft -> ft.getName().equals(FOLDER_TYPE_DEFAULT)).findFirst().orElseThrow();
        folder = client.createFolder(parentId, name, user.getId(), acl.getId(), defaultFolderType.getId());
        return this;
    }

    public TestObjectHolder createAcl(String name) throws IOException {
        acl = client.createAcl(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createGroup(String name) throws IOException {
        group = client.createGroupsByName(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createAclGroup() throws IOException {
        aclGroup = client.createAclGroups(List.of(new AclGroup(acl.getId(), group.getId()))).get(0);
        return this;
    }

    public TestObjectHolder addPermissionsByName(List<String> names) throws IOException {
        client.addAndRemovePermissions(aclGroup.getId(), permissions.stream().filter(p -> names.contains(p.getName())).map(Permission::getId).collect(Collectors.toList()), List.of());
        return this;
    }

    public TestObjectHolder addPermissions(List<DefaultPermission> permissionList) throws IOException {
        addPermissionsByName(permissionList.stream().map(DefaultPermission::getName).toList());
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
        client.updateFolders(request);
        return this;
    }

    public TestObjectHolder createObjectType(String name) throws IOException {
        objectType = client.createObjectTypes(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createOsdMeta(String content) throws IOException {
        meta = client.createOsdMeta(osd.getId(), content, metasetTypes.get(0).getId());
        return this;
    }

    /**
     * Create a link to the given osd using the current folder, acl, owner.
     * New link object is stored in TOH.link field.
     */
    public TestObjectHolder createLinkToOsd(ObjectSystemData osd) throws IOException {
        link = client.createLink(folder.getId(), LinkType.OBJECT, acl.getId(), user.getId(), null, osd.getId());
        return this;
    }
}
