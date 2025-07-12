package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.ThreadLocalSqlSession;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.provider.ContentProviderService;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.FileEntity;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.TIKA_METASET_NAME;
import static java.net.HttpURLConnection.HTTP_OK;

public class TikaService implements Runnable {
    private static final Logger log = LogManager.getLogger(TikaService.class);

    private final CinnamonTikaConfig     config;
    private       Long                   tikaMetasetTypeId;
    private final RetryPolicy<String>    retryPolicy;
    private final ContentProviderService contentProviderService;

    public TikaService(CinnamonTikaConfig config, ContentProviderService contentProviderService) {
        this.config                 = config;
        this.contentProviderService = contentProviderService;
        retryPolicy                 = RetryPolicy.<String>builder()
                .handle(IOException.class)
                .withBackoff(Duration.ofSeconds(1L), Duration.ofSeconds(10L))
                .build();
    }


    private Long getTikaMetasetTypeId() {
        if (tikaMetasetTypeId == null) {
            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                Optional<MetasetType> tikaMetasetType = new MetasetTypeDao(sqlSession).list().stream().filter(metasetType -> metasetType.getName().equals(TIKA_METASET_NAME)).findFirst();
                if (tikaMetasetType.isPresent()) {
                    tikaMetasetTypeId = tikaMetasetType.get().getId();
                }
                else {
                    String msg = "Could not find tika metaset type - this is a configuration error, contact your administrator.";
                    log.error(msg);
                    throw new CinnamonException(msg);
                }
            }
        }
        return tikaMetasetTypeId;
    }

    @Override
    public void run() {
        getTikaMetasetTypeId();
        while (true) {
            long tikaPauseInMillis = config.getTikaPauseInMillis();

            List<ObjectSystemData> osds;
            Map<Long, Format>      formats = new HashMap<>();
            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                OsdDao    osdDao    = new OsdDao(sqlSession);
                FormatDao formatDao = new FormatDao(sqlSession);
                osds = osdDao.getOsdsMissingTikaMetaset(getTikaMetasetTypeId(), config.getTikaBatchSize());
                for (ObjectSystemData objectSystemData : osds) {
                    Long   formatId = objectSystemData.getFormatId();
                    Format format   = formatDao.getObjectById(formatId).orElseThrow();
                    formats.put(formatId, format);
                }
            }
            if(osds.size() == config.getTikaBatchSize()) {
                // if we got a full batch, do another without waiting.
                tikaPauseInMillis = 0L;
            }
            try {
                for (ObjectSystemData osd : osds) {
                    Format          format          = formats.get(osd.getFormatId());
                    ContentProvider contentProvider = contentProviderService.getContentProvider(osd.getContentProvider());
                    try (InputStream contentStream = contentProvider.getContentStream(osd)) {
                        convertContentToTikaMetaset(osd, contentStream, format);
                    } catch (IOException e) {
                        throw new CinnamonException("Failed to load content for OSD " + osd.getId() + " at " + osd.getContentPath(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse tika data", e);
            }

            try{
                if(tikaPauseInMillis > 0) {
                    Thread.sleep(tikaPauseInMillis);
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public String parseData(File input, Format format, Long osdId) throws IOException {
        if (input.length() == 0) {
            log.debug("Tika was given a file without content.");
            return "<empty/>";
        }
        if (!config.isUseTika()) {
            return "<tikaIsDisabled/>";
        }
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPut httpPut = new HttpPut(config.getBaseUrl() + "/tika");
            httpPut.setEntity(new FileEntity(input, ContentType.parseLenient(format.getContentType())));
            return httpclient.execute(httpPut, response -> {
                if (response.getCode() != HTTP_OK) {
                    log.info("Failed to parse tika file of OSD {}: {}", osdId, response.getCode());
                    // TODO: improve error handling & reporting of tikaService.
                    return "<tikaFailedToParse/>";
                }
                return new String(response.getEntity().getContent().readAllBytes());
            });
        } catch (IOException e) {
            log.error("Failed to parse tika file of OSD {}: {}; re-try with backoff policy", osdId, e.getMessage());
            throw e;
        }

    }

    public boolean isEnabled() {
        return config.isUseTika();
    }

    public String parseWithTika(InputStream contentStream, Format format, Long osdId) throws IOException {
        // if the content comes from the file system anyway, this is some overhead.
        // if (some day) it comes from an API, the retry mechanism is easier when using a file
        // instead of an inputStream that may have been closed / cannot be reset().
        File tempData = File.createTempFile("tika-indexing-", ".data");
        try (FileOutputStream tempFos = new FileOutputStream(tempData)) {
            tempFos.write(contentStream.readAllBytes());
            tempFos.flush();
            return Failsafe.with(retryPolicy).get(() ->  parseData(tempData, format, osdId));
        } finally {
            FileUtils.delete(tempData);
        }
    }

    public void convertContentToTikaMetaset(ObjectSystemData osd, InputStream contentStream, Format format) throws
            IOException {
        if (isEnabled() && tikaMetasetTypeId != null && format.getIndexMode() == IndexMode.TIKA) {
            log.debug("Parse OSD #{} with Tika", osd.getId());
            String tikaMetadata = parseWithTika(contentStream, format, osd.getId());
            tikaMetadata = tikaMetadata.replace(" xmlns=\"http://www.w3.org/1999/xhtml\"","");
            log.trace("Tika returned: {}", tikaMetadata);
            try (SqlSession sqlSession = ThreadLocalSqlSession.getNewSession(TransactionIsolationLevel.READ_COMMITTED)) {
                OsdMetaDao     osdMetaDao  = new OsdMetaDao(sqlSession);
                Optional<Meta> tikaMetaset = osdMetaDao.listByOsd(osd.getId()).stream().filter(meta -> meta.getTypeId().equals(tikaMetasetTypeId)).findFirst();
                if (tikaMetaset.isPresent()) {
                    Meta tikaMeta = tikaMetaset.get();
                    tikaMeta.setContent(tikaMetadata);
                    try {
                        osdMetaDao.update(List.of(tikaMeta));
                    } catch (SQLException e) {
                        throw new CinnamonException("Failed to update tika metaset:", e);
                    }
                }
                else {
                    Meta       tikaMeta = new Meta(osd.getId(), tikaMetasetTypeId, tikaMetadata);
                    List<Meta> metas    = osdMetaDao.create(List.of(tikaMeta));
                    log.info("tikaMeta: {}", metas.getFirst());
                }
                IndexJobDao  indexJobDao = new IndexJobDao(sqlSession);
                indexJobDao.insertIndexJob(new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.UPDATE));
                sqlSession.commit();
            }
        }
    }
}
