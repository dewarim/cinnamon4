package com.dewarim.cinnamon.api;

import java.util.List;

public interface ApiRequest<T> {

    default List<ApiRequest<T>> examples(){
        return List.of(this);
    }
}
