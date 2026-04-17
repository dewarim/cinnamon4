package com.dewarim.cinnamon.model.response;


public class ChangeTriggerResponse {

    private String url;
    private String response;
    private int    httpCode;

    public ChangeTriggerResponse() {
    }

    public ChangeTriggerResponse(String url, String response, int httpCode) {
        this.url = url;
        this.response = response;
        this.httpCode = httpCode;
    }

    public String getUrl() {
        return url;
    }

    public String getResponse() {
        return response;
    }

    public int getHttpCode() {
        return httpCode;
    }

    @Override
    public String toString() {
        return "ChangeTriggerResponse{" +
                "url='" + url + '\'' +
                ", response='" + response + '\'' +
                ", httpCode=" + httpCode +
                '}';
    }

}
