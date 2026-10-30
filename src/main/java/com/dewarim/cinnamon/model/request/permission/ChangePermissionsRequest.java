package com.dewarim.cinnamon.model.request.permission;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("changePermissionsRequest")
public record ChangePermissionsRequest(
        @NotNull @Positive Long aclGroupId,
        @JacksonXmlElementWrapper(localName = "addPermissions")
        @JacksonXmlProperty(localName = "addId")
        List<@NotNull @Positive Long> add,
        @JacksonXmlElementWrapper(localName = "removePermissions")
        @JacksonXmlProperty(localName = "removeId")
        List<@NotNull @Positive Long> remove) implements ApiRequest<ChangePermissionsRequest> {

    public ChangePermissionsRequest {
        if (add == null) {
            add = new ArrayList<>();
        }
        if (remove == null) {
            remove = new ArrayList<>();
        }
    }

    /**
     * There has to be something to change. Neither list is required on its own, so this one rule
     * stays hand-written.
     */
    public boolean validated() {
        return !add.isEmpty() || !remove.isEmpty();
    }

    public Optional<ChangePermissionsRequest> validateRequest() {
        RequestValidator.validate(this);
        return validated() ? Optional.of(this) : Optional.empty();
    }

    @Override
    public List<ApiRequest<ChangePermissionsRequest>> examples() {
        return List.of(new ChangePermissionsRequest(3L, List.of(4L, 5L, 6L), List.of(7L, 8L, 9L)));
    }
}
