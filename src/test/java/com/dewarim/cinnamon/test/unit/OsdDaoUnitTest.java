package com.dewarim.cinnamon.test.unit;

import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.FailedRequestException;
import com.dewarim.cinnamon.dao.OsdDao;
import com.dewarim.cinnamon.model.ObjectSystemData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * objects.owner_id, creator_id and modifier_id are not null in the database since v1.15.1.
 * saveOsd checks them before it asks for a SqlSession, so the rejection can be tested without
 * a database - which is the point of the check: a create path that forgets one of the three
 * should name the field rather than produce a constraint violation from Postgres.
 */
public class OsdDaoUnitTest {

    private ObjectSystemData osdWithAllUsers() {
        ObjectSystemData osd = new ObjectSystemData();
        osd.setName("osd-dao-unit-test");
        osd.setParentId(1L);
        osd.setOwnerId(1L);
        osd.setCreatorId(1L);
        osd.setModifierId(1L);
        return osd;
    }

    @Test
    public void saveOsdWithoutOwnerIsRejected() {
        ObjectSystemData osd = osdWithAllUsers();
        osd.setOwnerId(null);
        assertEquals(ErrorCode.OBJECT_IS_MISSING_MANDATORY_USER,
                assertThrows(FailedRequestException.class, () -> new OsdDao().saveOsd(osd)).getErrorCode());
    }

    @Test
    public void saveOsdWithoutCreatorIsRejected() {
        ObjectSystemData osd = osdWithAllUsers();
        osd.setCreatorId(null);
        assertEquals(ErrorCode.OBJECT_IS_MISSING_MANDATORY_USER,
                assertThrows(FailedRequestException.class, () -> new OsdDao().saveOsd(osd)).getErrorCode());
    }

    @Test
    public void saveOsdWithoutModifierIsRejected() {
        ObjectSystemData osd = osdWithAllUsers();
        osd.setModifierId(null);
        assertEquals(ErrorCode.OBJECT_IS_MISSING_MANDATORY_USER,
                assertThrows(FailedRequestException.class, () -> new OsdDao().saveOsd(osd)).getErrorCode());
    }

    @Test
    public void saveOsdWithoutAnyUserNamesAllThree() {
        ObjectSystemData osd = new ObjectSystemData();
        osd.setName("osd-dao-unit-test");
        osd.setParentId(1L);
        assertEquals(ErrorCode.OBJECT_IS_MISSING_MANDATORY_USER,
                assertThrows(FailedRequestException.class, () -> new OsdDao().saveOsd(osd)).getErrorCode());
    }

}
