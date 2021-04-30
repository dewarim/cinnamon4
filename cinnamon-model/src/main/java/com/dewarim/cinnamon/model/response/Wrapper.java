package com.dewarim.cinnamon.model.response;

import java.util.List;

public interface Wrapper<T> {

    List<T> list();

    Wrapper<T> setList(List<T> ts);
}
