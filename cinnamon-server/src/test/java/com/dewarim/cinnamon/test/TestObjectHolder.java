package com.dewarim.cinnamon.test;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.Constants;
import com.dewarim.cinnamon.application.CinnamonClient;
import com.dewarim.cinnamon.model.Acl;
import com.dewarim.cinnamon.model.AclGroup;
import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.Format;
import com.dewarim.cinnamon.model.Group;
import com.dewarim.cinnamon.model.Language;
import com.dewarim.cinnamon.model.LifecycleState;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.Permission;
import com.dewarim.cinnamon.model.request.osd.CreateOsdRequest;
import com.dewarim.cinnamon.model.response.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TestObjectHolder {

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
    public List<Permission> permissions;
    public List<Format>     formats;
    public List<ObjectType> objectTypes;
    public String           summary = "<summary/>";

    public TestObjectHolder(CinnamonClient client) {
        this.client = client;
        try {
            permissions = client.listPermissions();
            formats = client.listFormats();
            objectTypes = client.listObjectTypes();
            objectType = objectTypes.stream().filter(type ->
                    type.getName().equals(Constants.OBJTYPE_DEFAULT)).findFirst().orElseThrow(ErrorCode.OBJECT_NOT_FOUND.getException());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize test object holder", e);
        }

    }

    public TestObjectHolder setAcl(Acl acl){
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

}
