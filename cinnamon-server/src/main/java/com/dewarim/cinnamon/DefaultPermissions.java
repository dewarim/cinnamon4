package com.dewarim.cinnamon;

/**
 *
 */
public enum DefaultPermissions {

    /**
     * Permission to lock an object. Implies permission to unlock the user's own locks.
     */
    LOCK("_lock"),

    /**
     * Permission to write to an object, that is: change its content.
     */
    WRITE_OBJECT_CONTENT("_write_object_content"),

    /**
     * Permission to read to an object's content.
     */
    READ_OBJECT_CONTENT("_read_object_content"),

    /**
     * Permission to change an object's system metadata
     */
    WRITE_OBJECT_SYS_METADATA("_write_object_sysmeta"),

    /**
     * Permission to read an object's system metadata
     */
    READ_OBJECT_SYS_METADATA("_read_object_sysmeta"),

    /**
     * Permission to change an object's custom metadata
     */
    WRITE_OBJECT_CUSTOM_METADATA("_write_object_custom_metadata"),

    /**
     * Permission to read an object's custom metadata
     */
    READ_OBJECT_CUSTOM_METADATA("_read_object_custom_metadata"),

    /**
     * Permission to create a new version of an object.
     */
    VERSION_OBJECT("_version"),

    /**
     * Permission to delete an object.
     */
    DELETE_OBJECT("_delete_object"),

    /**
     * Permission to browse an object, that is: the server will list it when
     * queried for the content of a folder or when a search for turns up this
     * object.
     */
    BROWSE_OBJECT("_browse"),

    /**
     * Permission to create a folder inside the current one.
     */
    CREATE_FOLDER("_create_folder"),

    /**
     * Permission to delete a folder.
     */
    DELETE_FOLDER("_delete_folder"),

    /**
     * Permission to create an object inside a folder.
     */
    CREATE_OBJECT("_create_inside_folder"), // create_object_inside_folder
    
    /**
     * Permission to edit a folder (change name, change metadata).
     */
    EDIT_FOLDER("_edit_folder"),

    /**
     * Permission to browse this folder, that is: the folder will be
     * displayed in a list of its parent's content or may turn up
     * during a search.
     */
    BROWSE_FOLDER("_browse_folder"),

    /**
     * Permission to move an object or folder, depending on which it is set.
     */
    MOVE("_move"),

    /**
     * Permission to change an object's or folder's ACL.
     */
    SET_ACL("_set_acl");
    
    String name;

    DefaultPermissions(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
