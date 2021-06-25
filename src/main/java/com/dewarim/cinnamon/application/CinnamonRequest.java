package com.dewarim.cinnamon.application;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class CinnamonRequest  extends HttpServletRequestWrapper {

    public CinnamonRequest(HttpServletRequest request) {
        super(request);
    }
}
