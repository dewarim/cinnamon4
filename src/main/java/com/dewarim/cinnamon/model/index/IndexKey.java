package com.dewarim.cinnamon.model.index;

public record IndexKey(IndexJobType type, Long itemId, IndexJobAction action) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexKey indexKey = (IndexKey) o;
        return type == indexKey.type && itemId.equals(indexKey.itemId)
                && action == indexKey.action;
    }

    @Override
    public String toString() {
        return type.name() + "#" + itemId;
    }
}
