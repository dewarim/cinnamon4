package com.dewarim.cinnamon.model;

import com.dewarim.cinnamon.api.Identifiable;

import java.util.Objects;

public class GroupUser implements Identifiable {

    private Long id;
    private Long userId;
    private Long groupId;

    public GroupUser() {
    }

    public GroupUser(Long userId, Long groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupUser groupUser = (GroupUser) o;
        return Objects.equals(userId, groupUser.userId) && Objects.equals(groupId, groupUser.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }

    @Override
    public String toString() {
        return "GroupUser{" +
                "id=" + id +
                ", userId=" + userId +
                ", groupId=" + groupId +
                '}';
    }

    @Override
    public Long getId() {
        return id;
    }
}
