package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.api.Ownable;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import nu.xom.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
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

    static void createMetaResponse(MetaRequest metaRequest, HttpServletResponse response, List<Meta> metaList, ObjectMapper mapper) throws IOException {

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
            response.getWriter().print(document.toXML());
        } else {
            MetaWrapper wrapper = new MetaWrapper(metaList);
            mapper.writeValue(response.getOutputStream(), wrapper);
        }
        ResponseUtil.responseIsOkayAndXml(response);
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
