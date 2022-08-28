package com.dewarim.cinnamon.api;

public interface Ownable extends Accessible, Identifiable {
    
    Long getOwnerId();
    
}
