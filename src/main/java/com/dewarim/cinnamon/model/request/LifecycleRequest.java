package com.dewarim.cinnamon.model.request;

public class LifecycleRequest {

    private Long id;
    private String name;

    public LifecycleRequest() {
    }

    public LifecycleRequest(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean validated(){
        return (id != null && id > 0) || (name != null && name.trim().length() > 0);
    }
}
