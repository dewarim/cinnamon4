package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.dao.CrudDao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PartitionLongListTest {

    @Test
    public void emptyListHasNoPartitions() {
        // an empty partition would reach a mapper's foreach as "IN ()"
        assertTrue(CrudDao.partitionLongList(List.of()).isEmpty());
    }

    @Test
    public void smallListIsSinglePartition() {
        assertEquals(List.of(List.of(1L, 2L)), CrudDao.partitionLongList(List.of(1L, 2L)));
    }

    @Test
    public void largeListIsSplitWithoutLosingIds() {
        List<Long>       ids        = LongStream.rangeClosed(1, CrudDao.BATCH_SIZE * 2L + 1).boxed().toList();
        List<List<Long>> partitions = CrudDao.partitionLongList(ids);
        assertEquals(3, partitions.size());
        assertTrue(partitions.stream().noneMatch(List::isEmpty));
        assertEquals(ids, partitions.stream().flatMap(List::stream).toList());
    }
}
