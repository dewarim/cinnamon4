package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectSystemData;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.LOCAL_DATE_TIME_EXAMPLE;

@JsonRootName("updateOsdRequest")
public record UpdateOsdRequest(
        @JacksonXmlElementWrapper(localName = "osds")
        @JacksonXmlProperty(localName = "osd")
        List<ObjectSystemData> osds,
        boolean updateContentChanged,
        boolean updateMetadataChanged) implements ApiRequest<UpdateOsdRequest> {

    public UpdateOsdRequest {
        if (osds == null) {
            osds = new ArrayList<>();
        }
    }

    public UpdateOsdRequest() {
        this(new ArrayList<>(), false, false);
    }

    public UpdateOsdRequest(List<ObjectSystemData> osds) {
        this(osds, false, false);
    }

    public UpdateOsdRequest(Long id, Long parentFolderId, String name, Long ownerId, Long aclId,
                            Long objectTypeId, Long languageId, Boolean contentChanged, Boolean metadataChanged) {
        this(singleOsd(id, parentFolderId, name, ownerId, aclId, objectTypeId, languageId, contentChanged, metadataChanged), false, false);
    }

    private static List<ObjectSystemData> singleOsd(Long id, Long parentFolderId, String name, Long ownerId, Long aclId,
                                                    Long objectTypeId, Long languageId, Boolean contentChanged, Boolean metadataChanged) {
        ObjectSystemData osd = new ObjectSystemData(id, name, ownerId, languageId, aclId, parentFolderId, objectTypeId);
        osd.setContentChanged(contentChanged);
        osd.setMetadataChanged(metadataChanged);
        List<ObjectSystemData> list = new ArrayList<>();
        list.add(osd);
        return list;
    }

    /**
     * A valid UpdateOsdRequest must contain the object's id as well at least
     * one potentially valid field that should be updated.
     */
    private boolean validated() {
        return osds != null && !osds.isEmpty() && osds.stream().allMatch(osd -> {
            String name = osd.getName();
            if (name != null && (name.isEmpty() || name.trim().length() < name.length() || name.matches("^\\s+$"))) {
                return false;
            }
            return assertNullOrPositiveLong(osd.getParentId()) &&
                    assertNullOrPositiveLong(osd.getOwnerId()) &&
                    assertNullOrPositiveLong(osd.getAclId()) &&
                    assertNullOrPositiveLong(osd.getTypeId()) &&
                    assertNullOrPositiveLong(osd.getLanguageId()) &&
                    osd.getId() != null && osd.getId() > 0;
        });
    }

    private boolean assertNullOrPositiveLong(Long id) {
        return id == null || id >= 0;
    }

    public Optional<UpdateOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<UpdateOsdRequest>> examples() {
        UpdateOsdRequest request = new UpdateOsdRequest(1L, 2L, "new name", 45L, 56L, 1L, 1L, false, true);
        request.osds().getFirst().setCreated(LOCAL_DATE_TIME_EXAMPLE);
        request.osds().getFirst().setModified(LOCAL_DATE_TIME_EXAMPLE);
        UpdateOsdRequest base2 = new UpdateOsdRequest(1L, 2L, "new name", 45L, 56L, 1L, 1L, false, true);
        base2.osds().getFirst().setCreated(LOCAL_DATE_TIME_EXAMPLE);
        base2.osds().getFirst().setModified(LOCAL_DATE_TIME_EXAMPLE);
        UpdateOsdRequest request2 = new UpdateOsdRequest(base2.osds(), true, true);
        return List.of(request, request2);
    }
}
