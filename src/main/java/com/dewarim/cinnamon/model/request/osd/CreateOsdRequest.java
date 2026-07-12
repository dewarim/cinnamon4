package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.model.Meta;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.DEFAULT_SUMMARY;

@JsonRootName("createOsdRequest")
public record CreateOsdRequest(
        String name,
        Long parentId,
        Long ownerId,
        Long aclId,
        Long typeId,
        Long formatId,
        Long languageId,
        Long lifecycleStateId,
        String summary,
        @JacksonXmlElementWrapper(localName = "metasets")
        @JacksonXmlProperty(localName = "metaset")
        List<Meta> metasets) implements ApiRequest<CreateOsdRequest> {

    public CreateOsdRequest {
        if (summary == null) {
            summary = DEFAULT_SUMMARY;
        }
        if (metasets == null) {
            metasets = new ArrayList<>();
        }
    }

    public CreateOsdRequest() {
        this(null, null, null, null, null, null, null, null, DEFAULT_SUMMARY, new ArrayList<>());
    }

    public CreateOsdRequest(String name, Long parentId, Long ownerId, Long aclId,
                            Long typeId, Long formatId, Long languageId,
                            Long lifecycleStateId, String summary) {
        this(name, parentId, ownerId, aclId, typeId, formatId, languageId, lifecycleStateId, summary, new ArrayList<>());
    }

    private boolean validated() {
        return name != null && name.trim().length() > 0
                && parentId != null && parentId > 0
                && (typeId == null || typeId > 0)
                && (aclId == null || aclId > 0)
                && ownerId != null && ownerId > 0
                && (formatId == null || formatId > 0)
                && (languageId == null || languageId > 0)
                && metaIsValid()
                && (summary == null || summary.trim().length() > 0);
    }

    private boolean metaIsValid() {
        return metasets.stream().allMatch(meta ->
                meta.getTypeId() != null && meta.getTypeId() > 0 && meta.getContent() != null
        );
    }

    public Optional<CreateOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<CreateOsdRequest>> examples() {
        return List.of(new CreateOsdRequest("create OSD request must be sent via multipart-request",
                1L, 23L, 44L, 2L, 3L, 1L, null,
                "<summary>Optional fields: typeId, aclId, ownerId, formatId, languageId, summary, metas</summary>",
                List.of(new Meta(1L, 2L, "<xml>some meta content</xml>"))));
    }
}
