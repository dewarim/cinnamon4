package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.ObjectSystemData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.DATE_EXAMPLE;

@JacksonXmlRootElement(localName = "updateOsdRequest")
public class UpdateOsdRequest implements ApiRequest<UpdateOsdRequest> {

    @JacksonXmlElementWrapper(localName = "osds")
    @JacksonXmlProperty(localName = "osd")
    private List<ObjectSystemData> osds = new ArrayList<>();
    private boolean                updateContentChanged;
    private boolean                updateMetadataChanged;


    public UpdateOsdRequest() {
    }

    public UpdateOsdRequest(Long id, Long parentFolderId, String name, Long ownerId, Long aclId,
                            Long objectTypeId, Long languageId, Boolean contentChanged, Boolean metadataChanged) {
        ObjectSystemData osd = new ObjectSystemData(id, name, ownerId, languageId, aclId, parentFolderId, objectTypeId);
        osd.setContentChanged(contentChanged);
        osd.setMetadataChanged(metadataChanged);
        osds.add(osd);
    }

    public void setUpdateContentChanged(boolean updateContentChanged) {
        this.updateContentChanged = updateContentChanged;
    }

    public void setUpdateMetadataChanged(boolean updateMetadataChanged) {
        this.updateMetadataChanged = updateMetadataChanged;
    }

    public UpdateOsdRequest(List<ObjectSystemData> osds) {
        this.osds = osds;
    }

    @Override
    public String toString() {
        return "UpdateOsdRequest{" +
                "osds=" + osds +
                '}';
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

    public boolean isUpdateContentChanged() {
        return updateContentChanged;
    }

    public boolean isUpdateMetadataChanged() {
        return updateMetadataChanged;
    }

    public List<ObjectSystemData> getOsds() {
        return osds;
    }

    public void setOsds(List<ObjectSystemData> osds) {
        this.osds = osds;
    }

    private boolean assertNullOrPositiveLong(Long id) {
        return id == null || id >= 0;
    }

    public Optional<UpdateOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<UpdateOsdRequest>> examples() {
        Date             date     = DATE_EXAMPLE;
        UpdateOsdRequest request  = new UpdateOsdRequest(1L, 2L, "new name", 45L, 56L, 1L, 1L, false, true);
        UpdateOsdRequest request2 = new UpdateOsdRequest(1L, 2L, "new name", 45L, 56L, 1L, 1L, false, true);
        request.getOsds().getFirst().setCreated(date);
        request2.getOsds().getFirst().setCreated(date);
        request.getOsds().getFirst().setModified(date);
        request2.getOsds().getFirst().setModified(date);
        request2.setUpdateMetadataChanged(true);
        request2.setUpdateContentChanged(true);

        return List.of(request, request2);
    }
}
