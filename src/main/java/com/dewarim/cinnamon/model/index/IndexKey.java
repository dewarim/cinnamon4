package com.dewarim.cinnamon.model.index;

/**
 * Identifies one indexable item for the index pipeline. Three ids meet around this class and are
 * easy to confuse:
 * <ul>
 *     <li>{@code itemId} - the id of the OSD or folder itself, from
 *     {@code seq_folder_and_object_ids}, stored in {@code index_jobs.item_id}.</li>
 *     <li>the <i>index-job</i> id - {@code index_jobs.id}, from {@code seq_index_job_id}. It is
 *     not part of this key: it lives on {@link IndexJob}, identifies the queue entry rather than
 *     the item, and is only used to delete that row once the job has been handled.</li>
 *     <li>the Lucene document's identity - the {@code unique_id} field, which is exactly what
 *     {@link #toString()} returns.</li>
 * </ul>
 * {@link #equals(Object)} and {@link #toString()} deliberately disagree about identity:
 * equals includes the action, because it identifies a unit of work, while toString does not,
 * because every action on an item - CREATE, UPDATE and DELETE alike - addresses that item's one
 * Lucene document.
 */
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
