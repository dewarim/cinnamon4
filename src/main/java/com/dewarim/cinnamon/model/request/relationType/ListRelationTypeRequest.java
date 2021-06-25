package com.dewarim.cinnamon.model.request.relationType;

import com.dewarim.cinnamon.model.relations.RelationType;
import com.dewarim.cinnamon.model.request.DefaultListRequest;
import com.dewarim.cinnamon.model.request.ListRequest;
import com.dewarim.cinnamon.model.response.RelationTypeWrapper;
import com.dewarim.cinnamon.model.response.Wrapper;

public class ListRelationTypeRequest extends DefaultListRequest implements ListRequest<RelationType> {

    @Override
    public Wrapper<RelationType> fetchResponseWrapper() {
        return new RelationTypeWrapper();
    }
}
