package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.exceptions.PersistenceException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * An interface for Servlets that implements CRUD operations.
 */
public interface CruddyServlet<T> {

    default void create(CreateRequest<T> createRequest, CrudDao<T> crudDao, CinnamonResponse cinnamonResponse) {
        List<T> ts = crudDao.create(createRequest.list());
        cinnamonResponse.setWrapper(createRequest.fetchResponseWrapper().setList(ts));
    }

    default CreateRequest<T> convertCreateRequest(HttpServletRequest request, Class<? extends CreateRequest<T>> clazz) throws IOException {
        return getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    default DeleteRequest<T> convertDeleteRequest(HttpServletRequest request, Class<? extends DeleteRequest<T>> clazz) throws IOException {
        return getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    ObjectMapper getMapper();

    default void delete(DeleteRequest<T> deleteRequest, CrudDao<T> crudDao, CinnamonResponse cinnamonResponse) {
        try {
            int deletedRows = crudDao.delete(deleteRequest.list());
            if (deletedRows != deleteRequest.list().size() && !deleteRequest.isIgnoreNotFound()) {
                throw new FailedRequestException(ErrorCode.OBJECT_NOT_FOUND);
            }
        } catch (PersistenceException | SQLException e) {
            throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
        }
        cinnamonResponse.setWrapper(deleteRequest.fetchResponseWrapper());
    }

    default void superuserCheck() {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorCode.REQUIRES_SUPERUSER_STATUS.throwUp();
        }
    }

//    CrudDao<T> getDao();
//
//    List<T> list();
//
//    T update(T t);
//
//    void delete(T t);

}
