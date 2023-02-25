package com.dewarim.cinnamon;

/**
 *
 */
public enum DefaultPermission {

    /*

planned:
folder.createfolder
folder.createobject
node.acl.write
node.browse
node.delete
node.metadata.read
node.metadata.write
node.move
node.name.write
node.parentfolder.write
node.sysmetadata.read
node.type.write
object.content.read
object.content.write
object.lifecyclestate.write
object.lock
object.version
relation.child.add
relation.child.remove
relation.parent.add
relation.parent.remove

Abweichungen:
folder.createfolder -> folder.create.folder
folder.createobject -> folder.create.object
node.sysmetadata.write -> fehlte noch (um language, owner, ... zu setzen)
node.parentfolder.write -> entfällt, da es node.move gibt.

relation.child.add
relation.parent.add
relation.child.remove
relation.parent.remove
folder.createfolder
folder.createobject
node.browse
node.delete
object.lock
node.sysmetadata.read
node.acl.write
node.move
object.content.write
object.content.read
object.version
node.metadata.write
node.metadata.read
object.lifecyclestate.write
node.name.write
node.type.write

     */

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
     * Permission to move an object or folder, depending on which it is set.
     */
    MOVE("node.move"),

    NAME_WRITE("node.name.write"),

    /**
     * Permission to read to an object's content.
     */
    READ_OBJECT_CONTENT("object.content.read"),

    /**
     * Permission to read an object's custom metadata
     */
    READ_OBJECT_CUSTOM_METADATA("node.metadata.read"),

    /**
     * Permission to read an object's system metadata
     */
    READ_OBJECT_SYS_METADATA("node.sysmetadata.read"),

    RELATION_CHILD_ADD("relation.child.add"),

    RELATION_CHILD_REMOVE("relation.child.remove"),

    RELATION_PARENT_ADD("relation.parent.add"),

    RELATION_PARENT_REMOVE("relation.parent.remove"),

    /**
     * Permission to change an object's or folder's ACL.
     */
    SET_ACL("node.acl.write"),
    TYPE_WRITE("node.type.write"),
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
    WRITE_OBJECT_CUSTOM_METADATA("node.metadata.write"),
    /**
     * Permission to change an object's system metadata
     */
    WRITE_OBJECT_SYS_METADATA("node.sysmetadata.write");

    final String name;

    DefaultPermission(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
