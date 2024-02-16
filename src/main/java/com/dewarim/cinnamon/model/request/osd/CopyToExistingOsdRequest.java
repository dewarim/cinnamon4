package com.dewarim.cinnamon.model.request.osd;

import com.dewarim.cinnamon.api.ApiRequest;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JacksonXmlRootElement(localName = "copyToExistingOsdRequest")
public class CopyToExistingOsdRequest implements ApiRequest<CopyToExistingOsdRequest> {

    @JacksonXmlElementWrapper(localName = "copyTasks")
    @JacksonXmlProperty(localName = "copyTask")
    private List<CopyTask> copyTasks = new ArrayList<>();
    public CopyToExistingOsdRequest() {
    }

    public CopyToExistingOsdRequest(List<CopyTask> copyTasks) {
        this.copyTasks = copyTasks;
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

    public List<CopyTask> getCopyTasks() {
        return copyTasks;
    }

    public void setCopyTasks(List<CopyTask> copyTasks) {
        this.copyTasks = copyTasks;
    }

    @Override
    public List<ApiRequest<CopyToExistingOsdRequest>> examples() {
        return List.of(new CopyToExistingOsdRequest(List.of(new CopyTask(100L, 200L, true, List.of(12L,13L)))));
    }

    @Override
    public String toString() {
        return "CopyToExistingOsdRequest{" +
                "copyTasks=" + copyTasks +
                '}';
    }
}
