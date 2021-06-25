package com.dewarim.cinnamon.model.relations;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class RelationType implements Identifiable {

    private Long    id;
    private boolean leftObjectProtected;
    private boolean rightObjectProtected;
    private String  name;
    private boolean cloneOnRightCopy;
    private boolean cloneOnLeftCopy;
    private boolean cloneOnLeftVersion;
    private boolean cloneOnRightVersion;

    public RelationType() {
    }

    public RelationType(String name,
                        boolean leftObjectProtected, boolean rightObjectProtected,
                        boolean cloneOnRightCopy, boolean cloneOnLeftCopy,
                        boolean cloneOnLeftVersion, boolean cloneOnRightVersion) {
        this.leftObjectProtected = leftObjectProtected;
        this.rightObjectProtected = rightObjectProtected;
        this.name = name;
        this.cloneOnRightCopy = cloneOnRightCopy;
        this.cloneOnLeftCopy = cloneOnLeftCopy;
        this.cloneOnLeftVersion = cloneOnLeftVersion;
        this.cloneOnRightVersion = cloneOnRightVersion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isLeftObjectProtected() {
        return leftObjectProtected;
    }

    public void setLeftObjectProtected(boolean leftObjectProtected) {
        this.leftObjectProtected = leftObjectProtected;
    }

    public boolean isRightObjectProtected() {
        return rightObjectProtected;
    }

    public void setRightObjectProtected(boolean rightObjectProtected) {
        this.rightObjectProtected = rightObjectProtected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCloneOnRightCopy() {
        return cloneOnRightCopy;
    }

    public void setCloneOnRightCopy(boolean cloneOnRightCopy) {
        this.cloneOnRightCopy = cloneOnRightCopy;
    }

    public boolean isCloneOnLeftCopy() {
        return cloneOnLeftCopy;
    }

    public void setCloneOnLeftCopy(boolean cloneOnLeftCopy) {
        this.cloneOnLeftCopy = cloneOnLeftCopy;
    }

    public boolean isCloneOnLeftVersion() {
        return cloneOnLeftVersion;
    }

    public void setCloneOnLeftVersion(boolean cloneOnLeftVersion) {
        this.cloneOnLeftVersion = cloneOnLeftVersion;
    }

    public boolean isCloneOnRightVersion() {
        return cloneOnRightVersion;
    }

    public void setCloneOnRightVersion(boolean cloneOnRightVersion) {
        this.cloneOnRightVersion = cloneOnRightVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationType that = (RelationType) o;
        return leftObjectProtected == that.leftObjectProtected &&
                rightObjectProtected == that.rightObjectProtected &&
                cloneOnRightCopy == that.cloneOnRightCopy &&
                cloneOnLeftCopy == that.cloneOnLeftCopy &&
                cloneOnLeftVersion == that.cloneOnLeftVersion &&
                cloneOnRightVersion == that.cloneOnRightVersion &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
