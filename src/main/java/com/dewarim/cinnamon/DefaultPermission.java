package com.dewarim.cinnamon;

/**
 *
 */
public enum DefaultPermission {

    /**
     * Permission to browse an object (OSD, folder, link etc), that is: the server will list it when
     * queried for the content of a folder or when a search for turns up this
     * object.
     */
    BROWSE("node.browse"),

    /**
     * Permission to create a folder inside the current one.
     */
    CREATE_FOLDER("folder.create.folder"),

    /**
     * Permission to create an object inside a folder.
     */
    CREATE_OBJECT("folder.create.object"), // create_object_inside_folder

    /**
     * Permission to delete an object.
     */
    DELETE("node.delete"),

    LIFECYCLE_STATE_WRITE("object.lifecyclestate.write"),

    /**
     * Permission to lock an object. Implies permission to unlock the user's own locks.
     */
    LOCK("object.lock"),

    /**
     * Permission to read to an object's content.
     */
    READ_OBJECT_CONTENT("object.content.read"),

    /**
     * Permission to read an object's custom metadata
     */
    READ_OBJECT_CUSTOM_METADATA("node.metadata.read"),

    RELATION_CHILD_ADD("relation.child.add"),

    RELATION_CHILD_REMOVE("relation.child.remove"),

    RELATION_PARENT_ADD("relation.parent.add"),

    RELATION_PARENT_REMOVE("relation.parent.remove"),

    /**
     * Permission to change an object's or folder's ACL.
     */
    SET_ACL("node.acl.write"),

    SET_LANGUAGE("object.language.write"),

    SET_LINK_TARGET("link.target.write"),
    SET_NAME("node.name.write"),
    SET_OWNER("node.owner.write"),
    /**
     * Permission to move an object or folder, depending on which it is set.
     */
    SET_PARENT("node.parent_folder.write"),
    SET_SUMMARY("node.summary.write"),

    SET_TYPE("node.type.write"),
    /**
     * Permission to create a new version of an object.
     */
    VERSION_OBJECT("object.version"),
    /**
     * Permission to write to an object, that is: change its content.
     */
    WRITE_OBJECT_CONTENT("object.content.write"),
    /**
     * Permission to change an object's custom metadata
     */
    WRITE_OBJECT_CUSTOM_METADATA("node.metadata.write");

    final String name;

    DefaultPermission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
