package com.dewarim.cinnamon.test;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.client.CinnamonClient;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.response.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TestObjectHolder {

    static        Object            SYNC_OBJECT = new Object();
    static        boolean           initialized = false;
    static public List<Permission>  permissions;
    static public List<Format>      formats;
    static public List<ObjectType>  objectTypes;
    static public List<MetasetType> metasetTypes;

    CinnamonClient client;
    public ObjectSystemData osd;
    public Acl              acl;
    public Group            group;
    public AclGroup         aclGroup;
    public UserInfo         user;
    public Folder           folder;
    public ObjectType       objectType;
    public Format           format;
    public Language         language;
    public LifecycleState   lifecycleState;

    public List<Meta> metas;
    public String     summary = "<summary/>";

    public TestObjectHolder(CinnamonClient client) {
        this.client = client;
        initialize();
    }

    public TestObjectHolder(CinnamonClient client, String aclName, Long userId, Long createFolderId) throws IOException {
        this.client = client;
        this.acl = client.getAclByName(aclName);
        setUser(userId);
        setFolder(createFolderId);
        initialize();

    }

    private void initialize() {
        synchronized (SYNC_OBJECT) {
            if (!initialized) {
                try {
                    permissions = client.listPermissions();
                    formats = client.listFormats();
                    objectTypes = client.listObjectTypes();
                    metasetTypes = client.listMetasetTypes();
                    objectType = objectTypes.stream().filter(type ->
                            type.getName().equals(Constants.OBJTYPE_DEFAULT)).findFirst().orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to initialize test object holder", e);
                }
            }
        }
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

    public TestObjectHolder createAcl(String name) throws IOException {
        acl = client.createAcl(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createGroup(String name) throws IOException {
        group = client.createGroups(List.of(name)).get(0);
        return this;
    }

    public TestObjectHolder createAclGroup() throws IOException {
        aclGroup = client.createAclGroups(List.of(new AclGroup(acl.getId(), group.getId()))).get(0);
        return this;
    }

    public TestObjectHolder addPermissions(List<String> names) throws IOException {
        client.addAndRemovePermissions(aclGroup.getId(), permissions.stream().filter(p -> names.contains(p.getName())).map(Permission::getId).collect(Collectors.toList()), List.of());
        return this;
    }

    public TestObjectHolder setUser(Long id) throws IOException {
        user = client.getUser(id);
        return this;
    }

    public TestObjectHolder setFolder(Long id) throws IOException {
        folder = client.getFolders(List.of(id), true).get(0);
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


}
