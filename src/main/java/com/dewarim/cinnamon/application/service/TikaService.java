package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.dao.MetasetTypeDao;
import com.dewarim.cinnamon.dao.OsdMetaDao;
import com.dewarim.cinnamon.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.TIKA_METASET_NAME;
import static java.net.HttpURLConnection.HTTP_OK;

public class TikaService {
    private static final Logger log = LogManager.getLogger(TikaService.class);

    private final CinnamonTikaConfig config;
    private final Long tikaMetasetTypeId;

    public TikaService(CinnamonTikaConfig config) {
        this.config = config;
        Optional<MetasetType> tikaMetasetType = new MetasetTypeDao().list().stream().filter(meta -> meta.getName().equals(TIKA_METASET_NAME)).findFirst();
        tikaMetasetTypeId = tikaMetasetType.map(MetasetType::getId).orElse(null);
    }

    public String parseData(File input, Format format) throws IOException {
        if (input.length() == 0) {
            log.debug("Tika was given a file without content.");
            return "<empty/>";
        }
        if (!config.isUseTika()) {
            return "<tikaIsDisabled/>";
        }
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPut  httpPut = new HttpPut(config.getBaseUrl() + "/tika");
            httpPut.setEntity(new FileEntity(input, ContentType.parseLenient(format.getContentType())));
            return httpclient.execute(httpPut, response -> {

                if (response.getCode() != HTTP_OK) {
                    log.info("Failed to parse tika file: " + response.getCode());
                    // TODO: improve error handling & reporting of tikaService.
                    return "<tikaFailedToParse/>";
                }
                return new String(response.getEntity().getContent().readAllBytes());
            });

        }
    }

    public boolean isEnabled() {
        return config.isUseTika();
    }

    public String parseWithTika(InputStream contentStream, Format format) throws IOException {
        File tempData = File.createTempFile("tika-indexing-", ".data");
        try (FileOutputStream tempFos = new FileOutputStream(tempData)) {
            tempFos.write(contentStream.readAllBytes());
            tempFos.flush();
            return parseData(tempData, format);
        } finally {
            FileUtils.delete(tempData);
        }
    }

    public void convertContentToTikaMetaset(ObjectSystemData osd, InputStream contentStream, Format format) throws IOException {
        OsdMetaDao osdMetaDao = new OsdMetaDao();
        if (isEnabled() && tikaMetasetTypeId != null && format.getIndexMode() == IndexMode.TIKA) {
            String tikaMetadata = parseWithTika(contentStream, format);
            log.debug("Tika returned: " + tikaMetadata);
            Optional<Meta> tikaMetaset = osdMetaDao.listByOsd(osd.getId()).stream().filter(meta -> meta.getTypeId().equals(tikaMetasetTypeId)).findFirst();
            if (tikaMetaset.isPresent()) {
                Meta tikaMeta = tikaMetaset.get();
                tikaMeta.setContent(tikaMetadata);
                try {
                    osdMetaDao.update(List.of(tikaMeta));
                } catch (SQLException e) {
                    throw new CinnamonException("Failed to update tika metaset:", e);
                }
            } else {
                Meta tikaMeta = new Meta(osd.getId(),tikaMetasetTypeId, tikaMetadata);
                List<Meta> metas = osdMetaDao.create(List.of(tikaMeta));
                log.debug("tikaMeta: " + metas.get(0));
            }
        }
    }
}
