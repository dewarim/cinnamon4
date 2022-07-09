package com.dewarim.cinnamon.application;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class to collect statistics.
 */
public class CinnamonStats {

    private AtomicLong deletions = new AtomicLong();


    public AtomicLong getDeletions() {
        return deletions;
    }

    public void setDeletions(AtomicLong deletions) {
        this.deletions = deletions;
    }
}
