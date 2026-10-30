package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
        @NotBlank String name,
        @NotNull @Positive Long parentId,
        @NotNull @Positive Long ownerId,
        // aclId and typeId stay optional here, exactly as the hand-written check had them:
        // createOsd resolves them unconditionally and answers ACL_NOT_FOUND / OBJECT_TYPE_NOT_FOUND
        // when they are missing. @Positive alone means "if present, must be positive".
        @Positive Long aclId,
        @Positive Long typeId,
        @Positive Long formatId,
        @Positive Long languageId,
        Long lifecycleStateId,
        // never null: the compact constructor substitutes DEFAULT_SUMMARY
        @NotBlank String summary,
        @JacksonXmlElementWrapper(localName = "metasets")
        @JacksonXmlProperty(localName = "metaset")
        List<@Valid Meta> metasets) implements ApiRequest<CreateOsdRequest> {

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

    public Optional<CreateOsdRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<CreateOsdRequest>> examples() {
        return List.of(new CreateOsdRequest("create OSD request must be sent via multipart-request",
                1L, 23L, 44L, 2L, 3L, 1L, null,
                "<summary>Optional fields: formatId, languageId, lifecycleStateId, summary, metas</summary>",
                List.of(new Meta(1L, 2L, "<xml>some meta content</xml>"))));
    }
}
