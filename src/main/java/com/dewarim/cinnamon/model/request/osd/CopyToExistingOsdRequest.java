package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("copyToExistingOsdRequest")
public record CopyToExistingOsdRequest(
        @JacksonXmlElementWrapper(localName = "copyTasks")
        @JacksonXmlProperty(localName = "copyTask")
        @NotEmpty List<@Valid CopyTask> copyTasks) implements ApiRequest<CopyToExistingOsdRequest> {

    public CopyToExistingOsdRequest {
        if (copyTasks == null) {
            copyTasks = new ArrayList<>();
        }
    }

    public Optional<CopyToExistingOsdRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<CopyToExistingOsdRequest>> examples() {
        return List.of(new CopyToExistingOsdRequest(List.of(new CopyTask(100L, 200L, true, List.of(12L, 13L)))));
    }
}
