package com.dewarim.cinnamon.application.servlet;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.application.CinnamonRequest;
import com.dewarim.cinnamon.application.CinnamonResponse;
import com.dewarim.cinnamon.dao.CrudDao;
import com.dewarim.cinnamon.dao.UserAccountDao;
import com.dewarim.cinnamon.model.request.CreateRequest;
import com.dewarim.cinnamon.model.request.DeleteRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.dewarim.cinnamon.model.response.Wrapper;
import org.apache.ibatis.exceptions.PersistenceException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * An interface for Servlets that implements CRUD operations.
 */
public interface CruddyServlet<T extends Identifiable> {

    default List<T> create(CreateRequest<T> createRequest, CrudDao<T> dao, CinnamonResponse cinnamonResponse) {
        List<T> ts = dao.create(createRequest.list());
        cinnamonResponse.setWrapper(createRequest.fetchResponseWrapper().setList(ts));
        return ts;
    }

    default CreateRequest<T> convertCreateRequest(CinnamonRequest request, Class<? extends CreateRequest<T>> clazz) throws IOException {
        return request.getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    default DeleteRequest<T> convertDeleteRequest(CinnamonRequest request, Class<? extends DeleteRequest<T>> clazz) throws IOException {
        return request.getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    default ListRequest<T> convertListRequest(CinnamonRequest request, Class<? extends ListRequest<T>> clazz) throws IOException {
        return request.getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    default UpdateRequest<T> convertUpdateRequest(CinnamonRequest request, Class<? extends UpdateRequest<T>> clazz) throws IOException {
        return request.getMapper().readValue(request.getInputStream(), clazz)
                .validateRequest().orElseThrow(ErrorCode.INVALID_REQUEST.getException());
    }

    default void deleteByIds(List<Long> ids, CrudDao<T> dao, boolean ignoreNotFound) {
        deleteInner(ids, dao, ignoreNotFound);
    }

    default List<Long> delete(DeleteRequest<T> deleteRequest, CrudDao<T> dao, CinnamonResponse cinnamonResponse) {
        try {
            deleteInner(deleteRequest.list(),dao, deleteRequest.isIgnoreNotFound());

        } catch (PersistenceException e) {
            throw new FailedRequestException(ErrorCode.DB_DELETE_FAILED, e);
        }
        cinnamonResponse.setWrapper(deleteRequest.fetchResponseWrapper());
        return deleteRequest.list();
    }

    default void deleteInner(List<Long> ids, CrudDao<T> dao, boolean ignoreNotFound){
        int deletedRows = dao.delete(ids);
        if (deletedRows != ids.size() && !ignoreNotFound) {
            throw new FailedRequestException(ErrorCode.OBJECT_NOT_FOUND);
        }
    }

    default void superuserCheck() {
        if (!UserAccountDao.currentUserIsSuperuser()) {
            ErrorCode.REQUIRES_SUPERUSER_STATUS.throwUp();
        }
    }

    //    CrudDao<T> getDao();

    default void list(ListRequest<T> listRequest, CrudDao<T> dao, CinnamonResponse cinnamonResponse) {
        Wrapper<T> wrapper = listRequest.fetchResponseWrapper().setList(dao.list());
        cinnamonResponse.setWrapper(wrapper);
    }

    default List<T> update(UpdateRequest<T> updateRequest, CrudDao<T> dao, CinnamonResponse cinnamonResponse){
        try {
            List<T> updatedItems = dao.update(updateRequest.list());
            cinnamonResponse.setWrapper(updateRequest.fetchResponseWrapper().setList(updatedItems));
            return updatedItems;
        }
        catch (PersistenceException | SQLException e){
            throw new FailedRequestException(ErrorCode.DB_UPDATE_FAILED, e.getMessage());
        }
    }

}
