package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateOsdRequestTest {

    @Test
    public void emptyName() {
        var request = new UpdateOsdRequest(1L, 1L, "", 1L, 1L, 1L, 1L);
        assertTrue(request.validateRequest().isEmpty());
    }

    @Test
    public void whitespaceName() {
        var request = new UpdateOsdRequest(1L, 1L, " ", 1L, 1L, 1L, 1L);
        assertTrue(request.validateRequest().isEmpty());
    }

    @Test
    public void trimmedName(){
        var request = new UpdateOsdRequest(1L, 1L, " leading-or-trailing-whitespace ",
                1L, 1L, 1L, 1L);
        assertTrue(request.validateRequest().isEmpty());
    }

    @Test
    public void happyName(){
        var request = new UpdateOsdRequest(1L, 1L, "okay",
                1L, 1L, 1L, 1L);
        assertTrue(request.validateRequest().isPresent());
    }

}

