package com.dewarim.cinnamon.model.request.index;

import com.dewarim.cinnamon.api.ApiRequest;
import com.dewarim.cinnamon.application.service.RequestValidator;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonRootName("reindexRequest")
public record ReindexRequest(List<@NotNull @Positive Long> osdIds,
                            List<@NotNull @Positive Long> folderIds,
                            @NotNull Boolean updateTikaMetaset) implements ApiRequest<ReindexRequest> {

    public ReindexRequest {
        if (osdIds == null) {
            osdIds = new ArrayList<>();
        }
        if (folderIds == null) {
            folderIds = new ArrayList<>();
        }
        if (updateTikaMetaset == null) {
            updateTikaMetaset = false;
        }
    }

    public ReindexRequest() {
        this(new ArrayList<>(), new ArrayList<>(), false);
    }

    public ReindexRequest(List<Long> osdIds, List<Long> folderIds) {
        this(osdIds, folderIds, false);
    }

    public boolean doFullReindex() {
        return osdIds.isEmpty() && folderIds.isEmpty();
    }

    public Optional<ReindexRequest> validateRequest() {
        RequestValidator.validate(this);
        return Optional.of(this);
    }

    @Override
    public List<ApiRequest<ReindexRequest>> examples() {
        return List.of(new ReindexRequest(), new ReindexRequest(List.of(13L, 23L), List.of(43L, 2L)));
    }
}
