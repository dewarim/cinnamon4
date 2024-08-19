package com.dewarim.cinnamon.api;

import com.dewarim.cinnamon.model.Folder;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 */
public class Constants {

    public static final String NEW_USER_HEADER_FLAG = "cinnamon-new-user";
    public static final String CINNAMON_REQUEST_HEADER = "cinnamon-request";

    /**
     * Date object for API examples (so we do not have changes in generated file
     * api.md on each build)
     */
    public static final Date             DATE_EXAMPLE = Date.from(LocalDateTime.of(2022, 8, 10, 3, 21).atZone(ZoneId.systemDefault()).toInstant());
    public static final ObjectSystemData OSD_EXAMPLE;
    public static final Folder           FOLDER_EXAMPLE;

    static {
        ObjectSystemData osd = new ObjectSystemData(1L, "my osd", 3L, 4L, 5L, 5L, 1L);
        osd.setCreated(DATE_EXAMPLE);
        osd.setModified(DATE_EXAMPLE);
        osd.setModifierId(1L);
        osd.setCreatorId(1L);
        osd.setFormatId(23L);
        OSD_EXAMPLE = osd;

        Folder folder = new Folder("images", 1L, 2L, 3L, 5L, "<summary>");
        folder.setId(2L);
        folder.setOwnerId(33L);
        folder.setHasSubfolders(false);
        folder.setCreated(DATE_EXAMPLE);
        FOLDER_EXAMPLE = folder;
    }

    public static final XmlMapper XML_MAPPER = (XmlMapper) new XmlMapper()
            .configure(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static final String DEFAULT_DATABASE_SESSION_FACTORY = "com.dewarim.cinnamon.application.DbSessionFactory";
    public static final String DATA_ROOT_PATH_PROPERTY_NAME     = "dataRootPath";

    public static final String DAO_USER_ACCOUNT = "UserAccountDao";

    public static final String ADMIN_USER_NAME = "admin";

    public static final String CONTENT_TYPE_XML        = "application/xml";
    public static final String CONTENT_TYPE_PLAIN_TEXT = "text/plain";

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


    public static final String DEFAULT_SUMMARY = "<summary/>";
    public static final String INDEX_SERVICE   = "IndexService";
    public static final String SEARCH_SERVICE  = "SearchService";
    public static final String TIKA_SERVICE    = "TikaService";
    public static final String CINNAMON_CONFIG = "CinnamonConfig";

    public static final String LUCENE_FIELD_UNIQUE_ID      = "unique_id";
    public static final String LUCENE_FIELD_CINNAMON_CLASS = "cinnamon_class";
    public static final String LUCENE_FIELD_ACL_ID         = "acl";
    public static final String LUCENE_FIELD_OWNER_ID       = "owner";

    public static final String TIKA_METASET_NAME = "tika";

    public static final String MULTIPART = "multipart/";

    /**
     * Name of the field of the multipart-entity request which contains the XML request data.
     */
    public static final String CINNAMON_REQUEST_PART = "cinnamonRequest";
}
