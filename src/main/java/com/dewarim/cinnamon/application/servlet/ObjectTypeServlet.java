package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.ObjectTypeDao;
import com.dewarim.cinnamon.model.ObjectType;
import com.dewarim.cinnamon.model.request.objectType.CreateObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.DeleteObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.ListObjectTypeRequest;
import com.dewarim.cinnamon.model.request.objectType.UpdateObjectTypeRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet(name = "ObjectType", urlPatterns = "/")
public class ObjectTypeServlet extends HttpServlet implements CruddyServlet<ObjectType> {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CinnamonRequest  cinnamonRequest  = (CinnamonRequest) request;
        CinnamonResponse cinnamonResponse = (CinnamonResponse) response;
        ObjectTypeDao    objectTypeDao    = new ObjectTypeDao();
        UrlMapping       mapping          = UrlMapping.getByPath(request.getRequestURI());

        switch (mapping) {
            case OBJECT_TYPE__LIST ->
                    list(convertListRequest(cinnamonRequest, ListObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            case OBJECT_TYPE__CREATE -> {
                superuserCheck();
                create(convertCreateRequest(cinnamonRequest, CreateObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            }
            // TODO: add test for OBJECT_TYPE__UPDATE
            case OBJECT_TYPE__UPDATE -> {
                superuserCheck();
                update(convertUpdateRequest(cinnamonRequest, UpdateObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            }
            // TODO: add test for OBJECT_TYPE__DELETE
            case OBJECT_TYPE__DELETE -> {
                superuserCheck();
                delete(convertDeleteRequest(cinnamonRequest, DeleteObjectTypeRequest.class), objectTypeDao, cinnamonResponse);
            }
            default -> ErrorCode.RESOURCE_NOT_FOUND.throwUp();
        }
    }


}
