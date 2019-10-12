package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.api.Ownable;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.ErrorCode;
import com.dewarim.cinnamon.application.ResponseUtil;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.model.Meta;
import com.dewarim.cinnamon.model.MetasetType;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.request.MetaRequest;
import com.dewarim.cinnamon.model.response.MetaWrapper;
import com.dewarim.cinnamon.security.authorization.AuthorizationService;
import nu.xom.*;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseServlet extends HttpServlet {

    static void throwUnlessSysMetadataIsWritable(Ownable ownable) {
        UserAccount user         = ThreadLocalSqlSession.getCurrentUser();
        boolean     writeAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.WRITE_OBJECT_SYS_METADATA, user);
        if (!writeAllowed) {
            throw ErrorCode.NO_WRITE_SYS_METADATA_PERMISSION.getException().get();
        }
    }

    static void throwUnlessSysMetadataIsReadable(Ownable ownable) {
        UserAccount user        = ThreadLocalSqlSession.getCurrentUser();
        boolean     readAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_SYS_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_READ_OBJECT_SYS_METADATA_PERMISSION.getException().get();
        }
    }

    static void throwUnlessCustomMetaIsReadable(Ownable ownable){
        UserAccount user = ThreadLocalSqlSession.getCurrentUser();
        boolean     readAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_READ_CUSTOM_METADATA_PERMISSION.getException().get();
        }
    }

    static void throwUnlessCustomMetaIsWritable(Ownable ownable, UserAccount user){
        boolean     readAllowed = new AuthorizationService().hasUserOrOwnerPermission(ownable, DefaultPermission.READ_OBJECT_CUSTOM_METADATA, user);
        if (!readAllowed) {
            throw ErrorCode.NO_WRITE_CUSTOM_METADATA_PERMISSION.getException().get();
        }
    }

    static void createMetaResponse(MetaRequest metaRequest, CinnamonResponse response, List<Meta> metaList) throws IOException {

        if (metaRequest.isVersion3CompatibilityRequired()) {
            // render traditional metaset
            Map<Long, MetasetType> metasetTypes = new MetasetTypeDao().listMetasetTypes().stream().collect(Collectors.toMap(MetasetType::getId, m -> m));

            Element  root     = new Element("meta");
            Document document = new Document(root);
            metaList.forEach(meta -> {
                Attribute metasetId = new Attribute("id", meta.getId().toString());
                Attribute type      = new Attribute("type", metasetTypes.get(meta.getTypeId()).getName());
                Element   metaset   = new Element("metaset");
                metaset.addAttribute(metasetId);
                metaset.addAttribute(type);
                Document content       = parseXml(meta.getContent());
                Elements childElements = content.getRootElement().getChildElements();
                for (int x = 0; x < childElements.size(); x++) {
                    Element element = childElements.get(x);
                    element.detach();
                    metaset.appendChild(element);
                }
                root.appendChild(metaset);
            });
            ResponseUtil.responseIsOkayAndXml(response);
            response.getWriter().print(document.toXML());
        } else {
            MetaWrapper wrapper = new MetaWrapper(metaList);
            response.setWrapper(wrapper);
        }
    }

    static private Document parseXml(String content) {
        try {
            Builder parser = new Builder();
            return parser.build(content, null);
        } catch (IOException | ParsingException e) {
            // TODO: use CinnamonException with custom error message
            throw new RuntimeException(e);
        }
    }


}
