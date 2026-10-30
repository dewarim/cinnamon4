package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.model.request.osd.UpdateOsdRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpdateOsdRequestTest {

    /**
     * The name rules live on ObjectSystemData now, so a violation is reported by field instead of
     * only as an empty Optional.
     */
    private void assertNameIsRejected(UpdateOsdRequest request) {
        FailedRequestException ex = assertThrows(FailedRequestException.class, request::validateRequest);
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("osds[0].name"), "should name the field: " + ex.getMessage());
    }

    @Test
    public void emptyName() {
        assertNameIsRejected(new UpdateOsdRequest(1L, 1L, "", 1L, 1L, 1L, 1L, false, false));
    }

    @Test
    public void whitespaceName() {
        assertNameIsRejected(new UpdateOsdRequest(1L, 1L, " ", 1L, 1L, 1L, 1L, false, false));
    }

    @Test
    public void trimmedName() {
        assertNameIsRejected(new UpdateOsdRequest(1L, 1L, " leading-or-trailing-whitespace ",
                1L, 1L, 1L, 1L, false, false));
    }

    @Test
    public void happyName() {
        var request = new UpdateOsdRequest(1L, 1L, "okay",
                1L, 1L, 1L, 1L, false, false);
        assertTrue(request.validateRequest().isPresent());
    }

    @Test
    public void aNameMayContainANewline() {
        // the old check used name.trim(), which accepts an inner newline - the @Pattern has to match
        var request = new UpdateOsdRequest(1L, 1L, "two\nlines", 1L, 1L, 1L, 1L, false, false);
        assertTrue(request.validateRequest().isPresent());
    }
}
