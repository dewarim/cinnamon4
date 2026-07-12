package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.api.Constants;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the API contract for null vs. empty-string handling, identically for XML and JSON:
 * a null property round-trips to null, and an empty string round-trips to "" (never null).
 * <p>
 * This is subtle on XML: Jackson can't tell {@code <x/>} (null) from {@code <x></x>} ("") by shape, so
 * {@link Constants#XML_MAPPER} writes nulls as {@code xsi:nil="true"} (WRITE_NULLS_AS_XSI_NIL) and does
 * NOT enable EMPTY_ELEMENT_AS_NULL. JSON distinguishes null from "" natively. See {@code Constants}.
 */
public class NullVsEmptyStringContractTest {

    public static class Bean {
        public Long   nullLong;   // null
        public Long   setLong;    // 42
        public String nullStr;    // null
        public String emptyStr;   // ""
        public String setStr;     // "hello"

        public Bean() {
        }

        static Bean sample() {
            Bean b = new Bean();
            b.nullLong = null;
            b.setLong = 42L;
            b.nullStr = null;
            b.emptyStr = "";
            b.setStr = "hello";
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Bean b)) {
                return false;
            }
            return Objects.equals(nullLong, b.nullLong) && Objects.equals(setLong, b.setLong)
                    && Objects.equals(nullStr, b.nullStr) && Objects.equals(emptyStr, b.emptyStr)
                    && Objects.equals(setStr, b.setStr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nullLong, setLong, nullStr, emptyStr, setStr);
        }
    }

    private void assertContract(ObjectMapper mapper) {
        Bean original = Bean.sample();
        String serialized = mapper.writeValueAsString(original);
        Bean back = mapper.readValue(serialized, Bean.class);

        assertNull(back.nullLong, "null Long must stay null; wire: " + serialized);
        assertEquals(42L, back.setLong, () -> "wire: " + serialized);
        assertNull(back.nullStr, "null String must stay null; wire: " + serialized);
        assertEquals("", back.emptyStr, "empty String must stay \"\" (not null); wire: " + serialized);
        assertEquals("hello", back.setStr, () -> "wire: " + serialized);
        assertEquals(original, back, "full round-trip; wire: " + serialized);
    }

    @Test
    public void xmlRoundTripPreservesNullAndEmptyString() {
        assertContract(Constants.XML_MAPPER);
    }

    @Test
    public void jsonRoundTripPreservesNullAndEmptyString() {
        assertContract(Constants.JSON_MAPPER);
    }
}
