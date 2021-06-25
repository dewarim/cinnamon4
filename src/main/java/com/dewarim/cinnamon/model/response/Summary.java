package com.dewarim.cinnamon.model.response;

public class Summary {
    private Long   id;
    private String content;

    public Summary() {
    }

    public Summary(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Summary{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }

}
