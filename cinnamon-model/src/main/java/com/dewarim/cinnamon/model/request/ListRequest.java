package com.dewarim.cinnamon.model.request;

/**
 * A simple list request. Currently this class is empty, but should be used to avoid
 * empty POST requests send to the server.
 * 
 * Future versions may include filter fields (for example, String nameFilter).
 */
public class ListRequest {
    
    int version = 0;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
