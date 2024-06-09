package com.dewarim.cinnamon.model.response;

import com.dewarim.cinnamon.api.ApiResponse;
import com.dewarim.cinnamon.api.UrlMapping;
import com.dewarim.cinnamon.model.UrlMappingInfo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "cinnamon")
public class UrlMappingInfoWrapper extends BaseResponse implements Wrapper<UrlMappingInfo>, ApiResponse {

    @JacksonXmlElementWrapper(localName = "urlMappings")
    @JacksonXmlProperty(localName = "urlMapping")
    List<UrlMappingInfo> urlMappingInfos = new ArrayList<>();

    public UrlMappingInfoWrapper(List<UrlMappingInfo> urlMappings) {
        this.urlMappingInfos = urlMappings;
    }

    public UrlMappingInfoWrapper() {
    }

    public List<UrlMappingInfo> getUrlMappingInfos() {
        return urlMappingInfos;
    }

    public void setUrlMappingInfos(List<UrlMappingInfo> urlMappingInfos) {
        this.urlMappingInfos = urlMappingInfos;
    }

    @Override
    public List<UrlMappingInfo> list() {
        return getUrlMappingInfos();
    }

    @Override
    public Wrapper<UrlMappingInfo> setList(List<UrlMappingInfo> urlMappingInfos) {
        setUrlMappingInfos(urlMappingInfos);
        return this;
    }

    @Override
    public List<Object> examples() {
        UrlMapping echo = UrlMapping.TEST__ECHO;
        return List.of(new UrlMappingInfoWrapper(List.of(new UrlMappingInfo(echo.getServlet(), echo.getAction(), echo.getPath(), echo.getDescription()))));
    }
}
