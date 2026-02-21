package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.DefaultPermission;
import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.application.RequestScope;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.dao.RelationDao;
import com.dewarim.cinnamon.dao.RelationTypeDao;
import com.dewarim.cinnamon.model.UserAccount;
import com.dewarim.cinnamon.model.relations.Relation;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteRequest;
import com.dewarim.cinnamon.model.request.relation.CreateRelationRequest;
import com.dewarim.cinnamon.model.request.relation.DeleteRelationRequest;
import com.dewarim.cinnamon.model.request.relation.SearchRelationRequest;
import com.dewarim.cinnamon.model.response.RelationWrapper;
import com.dewarim.cinnamon.security.authorization.AccessFilter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@WebServlet(name = "Relation", urlPatterns = "/")
public class RelationServlet extends HttpServlet implements CruddyServlet<Relation> {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest cinnamonRequest = (CinnamonRequest) request;

        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        RelationDao      relationDao      = new RelationDao();
        UserAccount      user             = RequestScope.getCurrentUser();

        UrlMapping mapping = UrlMapping.getByPath(request.getRequestURI());
        switch (mapping) {
            case RELATION__SEARCH -> searchRelations(cinnamonRequest, relationDao, cinnamonResponse);
            case RELATION__CREATE -> createRelations(cinnamonRequest, user, relationDao, cinnamonResponse);
            case RELATION__DELETE -> deleteRelation(cinnamonRequest, user, relationDao, cinnamonResponse);
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }

    private void createRelations(CinnamonRequest request, UserAccount user, RelationDao relationDao, CinnamonResponse cinnamonResponse) throws IOException {
        CreateRequest<Relation> createRequest = convertCreateRequest(request, CreateRelationRequest.class);
        List<Relation>          relations     = createRequest.list();
        RelationTypeDao         rtDao         = new RelationTypeDao();
        var                     missingTypes    = rtDao.verifyAllObjectsFromSetExist(relations.stream().map(Relation::getTypeId).collect(Collectors.toList()));
        if (!missingTypes.isEmpty()) {
            String message = "Could not find the following relation types: "+missingTypes.stream().map(Object::toString).collect(Collectors.joining(","));
            throw new FailedRequestException(ErrorCode.RELATION_TYPE_NOT_FOUND, message);
        }
        AccessFilter accessFilter = AccessFilter.getInstance(user);
        OsdDao       osdDao       = new OsdDao();
        List<Long>   parentIds    = relations.stream().map(Relation::getLeftId).toList();
        List<Long>   childIds     = relations.stream().map(Relation::getRightId).toList();

        boolean addChildPermission = osdDao.getObjectsById(parentIds, false).stream()
                .allMatch(osd -> accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.RELATION_CHILD_ADD, osd));
        if (!addChildPermission) {
            throw ErrorCode.NO_RELATION_CHILD_ADD_PERMISSION.exception();
        }

        boolean addParentPermission = osdDao.getObjectsById(childIds, false).stream()
                .allMatch(osd -> accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.RELATION_PARENT_ADD, osd));
        if (!addParentPermission) {
            throw ErrorCode.NO_RELATION_PARENT_ADD_PERMISSION.exception();
        }
        create(createRequest, relationDao, cinnamonResponse);
    }


    private void deleteRelation(CinnamonRequest request, UserAccount user, RelationDao relationDao, CinnamonResponse response) throws IOException {
        DeleteRequest<Relation> deleteRequest = convertDeleteRequest(request, DeleteRelationRequest.class);
        List<Long>              relationIds   = deleteRequest.list();
        List<Relation>          relations     = relationDao.getObjectsById(relationIds);
        AccessFilter            accessFilter  = AccessFilter.getInstance(user);
        OsdDao                  osdDao        = new OsdDao();
        List<Long>              parentIds     = relations.stream().map(Relation::getLeftId).toList();
        List<Long>              childIds      = relations.stream().map(Relation::getRightId).toList();

        boolean addChildPermission = osdDao.getObjectsById(parentIds, false).stream()
                .allMatch(osd -> accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.RELATION_CHILD_REMOVE, osd));
        if (!addChildPermission) {
            throw ErrorCode.NO_RELATION_CHILD_REMOVE_PERMISSION.exception();
        }

        boolean addParentPermission = osdDao.getObjectsById(childIds, false).stream()
                .allMatch(osd -> accessFilter.hasPermissionOnOwnable(osd, DefaultPermission.RELATION_PARENT_REMOVE, osd));
        if (!addParentPermission) {
            throw ErrorCode.NO_RELATION_PARENT_REMOVE_PERMISSION.exception();
        }

        delete(deleteRequest, relationDao, response);
    }

    private void searchRelations(CinnamonRequest request, RelationDao relationDao, CinnamonResponse response) throws IOException {
        SearchRelationRequest relationRequest = request.getMapper().readValue(request.getInputStream(), SearchRelationRequest.class)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());

        List<Relation> relations;
        if (relationRequest.isOrMode()) {
            relations = relationDao.getRelationsOrMode(relationRequest.getLeftIds(), relationRequest.getRightIds(),
                    relationRequest.getRelationTypeIds(), relationRequest.isIncludeMetadata());
        } else {
            relations = relationDao.getRelations(relationRequest.getLeftIds(), relationRequest.getRightIds(),
                    relationRequest.getRelationTypeIds(), relationRequest.isIncludeMetadata());
        }
        RelationWrapper wrapper = new RelationWrapper(relations);
        response.setWrapper(wrapper);
    }

}