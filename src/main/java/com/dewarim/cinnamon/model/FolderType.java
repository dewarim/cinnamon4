package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;
import com.dewarim.cinnamon.model.request.UpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

public class FolderType implements Identifiable {
    
    /*
     * An update must name the row to change, a create must not - so the id constraints belong to
     * the UpdateRequest group, and Bean Validation groups are plain interfaces, which lets the
     * existing UpdateRequest interface serve as the group marker.
     */
    @NotNull(groups = UpdateRequest.class)
    @Positive(groups = UpdateRequest.class)
    private Long id;

    @NotBlank
    private String name;

    public FolderType() {
    }

    public FolderType(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public FolderType(String name) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FolderType that = (FolderType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "FolderType{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}
