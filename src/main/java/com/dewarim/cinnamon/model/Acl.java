package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

@JsonRootName("acl")
public class Acl implements Identifiable {
    
    // Request validation, checked when a request cascades into this class with @Valid.
    @NotNull(groups = UpdateRequest.class)
    @Positive(groups = UpdateRequest.class)
    private Long id;
    @NotBlank
    private String name;

    public Acl() {
    }

    public Acl(String name) {
        this.name = name;
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
                "id=" + id +
                ", name='" + name + '\'' +
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
