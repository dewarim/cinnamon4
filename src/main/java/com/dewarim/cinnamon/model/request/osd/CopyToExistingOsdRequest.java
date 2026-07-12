package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonRootName("copyToExistingOsdRequest")
public record CopyToExistingOsdRequest(
        @JacksonXmlElementWrapper(localName = "copyTasks")
        @JacksonXmlProperty(localName = "copyTask")
        List<CopyTask> copyTasks) implements ApiRequest<CopyToExistingOsdRequest> {

    public CopyToExistingOsdRequest {
        if (copyTasks == null) {
            copyTasks = new ArrayList<>();
        }
    }

    private boolean validated() {
        return copyTasks != null && !copyTasks.isEmpty() &&
                copyTasks.stream().allMatch(copyTask -> copyTask.getSourceOsdId() != null && copyTask.getSourceOsdId() > 0
                        && copyTask.getTargetOsdId() != null && copyTask.getTargetOsdId() > 0 && copyTask.getMetasetTypeIds().stream().noneMatch(Objects::isNull)
                        && copyTask.getMetasetTypeIds().stream().noneMatch(id -> id <= 0)
                );
    }

    public Optional<CopyToExistingOsdRequest> validateRequest() {
        if (validated()) {
            return Optional.of(this);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ApiRequest<CopyToExistingOsdRequest>> examples() {
        return List.of(new CopyToExistingOsdRequest(List.of(new CopyTask(100L, 200L, true, List.of(12L, 13L)))));
    }
}
