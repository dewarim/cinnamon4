package com.dewarim.cinnamon.api;

import java.util.Collections;
import java.util.List;

public interface ApiResponse {

    default List<Object> examples(){
        return Collections.singletonList(this);
    }
}
