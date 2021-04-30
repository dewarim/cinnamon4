package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

/** An interface for Servlets that implements CRUD operations.
 */
public interface CruddyServlet<T> {

    default void create(CreateRequest<T> createRequest, CrudDao<T> crudDao, CinnamonResponse cinnamonResponse){
        List<T> ts = crudDao.create(createRequest.list());
        cinnamonResponse.setWrapper(createRequest.fetchResponseWrapper().setList(ts));
    }

    default CreateRequest<T> convertCreateRequest(HttpServletRequest request, Class<? extends CreateRequest<T>> clazz) throws IOException {
        return getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    ObjectMapper getMapper();

    default void delete(DeleteRequest<T> deleteRequest, CrudDao<T> crudDao, CinnamonResponse cinnamonResponse){

    }

//    CrudDao<T> getDao();
//
//    List<T> list();
//
//    T update(T t);
//
//    void delete(T t);

}
