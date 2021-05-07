package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.Objects;

@JacksonXmlRootElement(localName = "acl")
public class Acl implements Identifiable {
    
    private Long id;
    private String name;

    public Acl() {
    }

    public Acl(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Acl{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Acl acl = (Acl) o;
        return Objects.equals(name, acl.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
