package com.dewarim.cinnamon.api;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

/**
 *
 */
public class Constants {

    public static final XmlMapper XML_MAPPER = (XmlMapper) new XmlMapper().configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true);

    public static final String DEFAULT_DATABASE_SESSION_FACTORY = "com.dewarim.cinnamon.application.DbSessionFactory";
    public static final String DATA_ROOT_PATH_PROPERTY_NAME     = "dataRootPath";

    public static final String DAO_USER_ACCOUNT = "UserAccountDao";

    public static final String ADMIN_USER_NAME = "admin";

    public static final String CONTENT_TYPE_XML = "application/xml";

    public static final String LANGUAGE_UNDETERMINED_ISO_CODE = "und";

    // Group and ACL related constants:
    public static final String ACL_DEFAULT      = "_default_acl";
    public static final String GROUP_SUPERUSERS = "_superusers";
    public static final String ALIAS_EVERYONE   = "_everyone";
    public static final String ALIAS_OWNER      = "_owner";

    public static final String FOLDER_TYPE_DEFAULT = "_default_folder_type";
    public static final String OBJTYPE_DEFAULT     = "_default_objtype";
    public static final String ROOT_FOLDER_NAME    = "root";

    public static final String HEADER_FIELD_CINNAMON_ERROR = "cinnamon-error";

    public static final Integer EXPECTED_SIZE_ANY = -1;

    /**
     * Name of the field of the multipart-entity request which contains the XML request data.
     */
    public static final String CREATE_NEW_VERSION = "createNewVersionRequest";
    public static final String CREATE_NEW_OSD     = "createOsdRequest";

}
