package com.dewarim.cinnamon.application;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class CinnamonRequest  extends HttpServletRequestWrapper {

    public CinnamonRequest(HttpServletRequest request) {
        super(request);
    }
}
