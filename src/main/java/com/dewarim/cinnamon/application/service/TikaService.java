package com.dewarim.cinnamon.application.service;

import com.dewarim.cinnamon.api.content.ContentProvider;
import com.dewarim.cinnamon.application.CinnamonServer;
import com.dewarim.cinnamon.application.exception.CinnamonException;
import com.dewarim.cinnamon.application.exception.TikaFailedException;
import com.dewarim.cinnamon.application.exception.TikaUnavailableException;
import com.dewarim.cinnamon.configuration.CinnamonTikaConfig;
import com.dewarim.cinnamon.dao.*;
import com.dewarim.cinnamon.model.*;
import com.dewarim.cinnamon.model.index.IndexJob;
import com.dewarim.cinnamon.model.index.IndexJobAction;
import com.dewarim.cinnamon.model.index.IndexJobType;
import com.dewarim.cinnamon.provider.ContentProviderService;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.RetryPolicy;
import dev.failsafe.RetryPolicyBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.dewarim.cinnamon.api.Constants.TIKA_METASET_NAME;
import static java.net.HttpURLConnection.HTTP_OK;

public class TikaService implements Runnable {
    private static final Logger log = LogManager.getLogger(TikaService.class);

    /**
     * HTTP statuses that say something about the Tika server or whatever stands in front of it,
     * rather than about the document that was sent: not found (a wrong baseUrl, or a path that
     * moved with a Tika upgrade), unauthorized or forbidden (a proxy asking for credentials),
     * request timeout, too many requests, and the gateway statuses of a server that is restarting
     * or unreachable. Tika does not answer any of these for a document it cannot parse, so they are
     * never counted against the document's attempts - see TikaUnavailableException.
     */
    private static final Set<Integer> STATUS_NOT_ABOUT_THE_DOCUMENT = Set.of(401, 403, 404, 408, 429, 502, 503, 504);

    private final CinnamonTikaConfig     config;
    private       Long                   tikaMetasetTypeId;
    private final RetryPolicy<String>    retryPolicy;
    private final ContentProviderService contentProviderService;

    public TikaService(CinnamonTikaConfig config, ContentProviderService contentProviderService) {
        this.config = config;
        this.contentProviderService = contentProviderService;
        // IOException only: a connection that failed says nothing about the document, so it is
        // worth retrying. A document Tika rejects (TikaFailedException) is not - it would fail the
        // same way three times in a row.
        Duration delay    = Duration.ofSeconds(config.getRetryDelayInSeconds());
        Duration maxDelay = Duration.ofSeconds(config.getMaxRetryDelayInSeconds());
        RetryPolicyBuilder<String> policyBuilder = RetryPolicy.<String>builder()
                .handle(IOException.class);
        // withBackoff insists on delay < maxDelay, so a configuration that leaves no room to back
        // off retries at a constant delay rather than failing the server at startup.
        retryPolicy = (maxDelay.compareTo(delay) > 0
                ? policyBuilder.withBackoff(delay, maxDelay)
                : policyBuilder.withDelay(delay)).build();
    }


    private Long getTikaMetasetTypeId() {
        if (tikaMetasetTypeId == null) {
            try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
                Optional<MetasetType> tikaMetasetType = new MetasetTypeDao(sqlSession).list().stream().filter(metasetType -> metasetType.getName().equals(TIKA_METASET_NAME)).findFirst();
                if (tikaMetasetType.isPresent()) {
                    tikaMetasetTypeId = tikaMetasetType.get().getId();
                    log.info("Found tika metaset: {}", tikaMetasetTypeId);
                } else {
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
        while (true) {
            int extracted = 0;
            try {
                extracted = extractPendingMetasets();
            } catch (Exception e) {
                // Nothing may escape into this loop: an exception here would end the thread for the
                // lifetime of the server and no document would ever be extracted again, with no log
                // line saying so. A row that cannot be read, or a database that is briefly
                // unavailable, costs a pause instead. getTikaMetasetTypeId() is called from inside
                // the pass for the same reason: a configuration error is reported on every pass
                // rather than killing the service at startup.
                log.error("A Tika extraction pass failed - trying again after the pause", e);
            }
            long tikaPauseInMillis = extracted == config.getTikaBatchSize()
                    // A full batch came through, so there is probably more waiting: carry on without
                    // pausing. Anything less - an empty queue, or a Tika that is not answering -
                    // and the pause is what keeps this loop from spinning. It has to count the
                    // documents that actually gained a metaset: during an outage the same full batch
                    // is offered on every pass, and counting what was offered would pin the pause
                    // at zero for as long as the outage lasts.
                    ? 0L
                    : config.getTikaPauseInMillis();
            try {
                if (tikaPauseInMillis > 0) {
                    Thread.sleep(tikaPauseInMillis);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    /**
     * One pass over the queue: take up to tikaBatchSize objects that have no tika metaset and try
     * to give them one.
     *
     * @return how many of them actually gained one. This is what the pause decision needs: a pass
     * that extracted nothing must not be repeated immediately.
     */
    public int extractPendingMetasets() {
        List<ObjectSystemData> osds;
        Map<Long, Format>      formats = new HashMap<>();
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            OsdDao    osdDao    = new OsdDao(sqlSession);
            FormatDao formatDao = new FormatDao(sqlSession);
            osds = osdDao.getOsdsMissingTikaMetaset(getTikaMetasetTypeId(), config.getTikaBatchSize(),
                    config.getMaxTikaAttempts());
            for (ObjectSystemData objectSystemData : osds) {
                Long formatId = objectSystemData.getFormatId();
                // a batch usually shares a handful of formats, so ask once per format, not per OSD:
                if (!formats.containsKey(formatId)) {
                    formatDao.getObjectById(formatId).ifPresent(format -> formats.put(formatId, format));
                }
            }
        }
        if (osds.isEmpty()) {
            return 0;
        }
        log.info("TikaService found {} OSDs that need Tika metaset: {}", osds.size(), osds.stream().map(ObjectSystemData::getId).toList());
        int extracted = 0;
        for (ObjectSystemData osd : osds) {
            Format format = formats.get(osd.getFormatId());
            if (format == null) {
                // The queue selects on the format's index mode, so the row is gone or the database
                // is inconsistent. It is specific to this document and has to be counted: left
                // uncounted it would be offered again on every pass forever, and left to throw it
                // would abort the pass at the same document every time, so nothing else would ever
                // be reached.
                log.error("OSD {} refers to format {}, which does not exist - cannot extract.",
                        osd.getId(), osd.getFormatId());
                recordFailure(osd, "format " + osd.getFormatId() + " does not exist");
                continue;
            }
            if (extractMetaset(osd, format)) {
                extracted++;
            }
        }
        return extracted;
    }

    /**
     * Extract one object's metadata, and sort out what to do when that does not work. The three
     * cases are deliberately different, and the difference is the point:
     * <ul>
     *     <li>Tika answered but did not accept the document: counted, and given up on once
     *     maxTikaAttempts is reached.</li>
     *     <li>Tika or the content store could not be reached, or Tika answered with a status that
     *     is about the server rather than the document: not counted. It says nothing about this
     *     document, and counting it would write off every pending object during an outage.</li>
     *     <li>Anything else: counted like a rejection. It is reached once per document, so leaving
     *     it uncounted would mean retrying it on every pass forever.</li>
     * </ul>
     * A failure must never end up as metaset content: an object that has a tika metaset is one this
     * service never looks at again.
     *
     * @return true if the object now has a tika metaset
     */
    public boolean extractMetaset(ObjectSystemData osd, Format format) {
        try {
            ContentProvider contentProvider = contentProviderService.getContentProvider(osd.getContentProvider());
            if (contentProvider.contentUnavailable(osd)) {
                log.error("File {} of OSD {} is unavailable - cannot create TikaMetaset",
                        osd.getContentPath(), osd.getId());
                recordFailure(osd, "content is unavailable at " + osd.getContentPath());
                return false;
            }
            try (InputStream contentStream = contentProvider.getContentStream(osd)) {
                return convertContentToTikaMetaset(osd, contentStream, format);
            }
        }
        catch (TikaFailedException e) {
            log.error("Tika did not return metadata for OSD {}: {}", osd.getId(), e.getMessage());
            recordFailure(osd, e.getMessage());
        }
        catch (TikaUnavailableException | IOException | FailsafeException e) {
            // FailsafeException is what the exhausted retry policy throws.
            log.error("Could not reach Tika or the content of OSD {} - will try again later: {}",
                    osd.getId(), e.getMessage());
        }
        catch (Exception e) {
            log.error("Failed to create TikaMetaset for OSD {} (path: {})", osd.getId(), osd.getContentPath(), e);
            recordFailure(osd, e.toString());
        }
        return false;
    }

    /**
     * Raise this OSD's attempt counter, in a session of its own: this runs on the TikaService
     * thread, which has no request-scoped session, and the row has to survive whatever made the
     * extraction fail.
     */
    private void recordFailure(ObjectSystemData osd, String error) {
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            new TikaFailureDao(sqlSession).recordFailure(osd.getId(), error);
            sqlSession.commit();
        } catch (Exception e) {
            log.error("Failed to record the Tika failure of OSD {}", osd.getId(), e);
        }
    }

    public String parseData(InputStream input, Long contentLength, Format format, Long osdId) throws IOException {
        if (!config.isUseTika()) {
            return "<tikaIsDisabled/>";
        }
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPut httpPut = new HttpPut(config.getBaseUrl() + "/tika");
            httpPut.setEntity(new InputStreamEntity(input, contentLength, ContentType.parseLenient(format.getContentType())));
            return httpclient.execute(httpPut, response -> {
                int status = response.getCode();
                if (status != HTTP_OK) {
                    // Deliberately not a return value: a stored "<tikaFailedToParse/>" document
                    // would count as a Tika metaset, and getOsdsMissingTikaMetaset would never
                    // offer this OSD again - a Tika server that answers 404 after an upgrade or a
                    // misconfigured baseUrl would silently write off everything it was given.
                    //
                    // Which exception it is decides whether the document pays for the failure. Tika
                    // itself answers 400, 413, 415 or 422 for content it will not take, and 500
                    // when a parser throws on it: those are about this document and are counted.
                    // The statuses below are not - they are what a restarting Tika, a wrong baseUrl
                    // or a proxy in front of it answers for every request alike, so counting them
                    // would still write off the whole backlog during an outage.
                    if (STATUS_NOT_ABOUT_THE_DOCUMENT.contains(status)) {
                        throw new TikaUnavailableException("Tika answered with HTTP status " + status);
                    }
                    throw new TikaFailedException("Tika answered with HTTP status " + status);
                }

                // looking for extremely large responses that lead to OOM:
                // (goal is to have logging for those objects so we can see how much RAM to add...)
                InputStream responseStream = response.getEntity().getContent();
                try (BufferedReader bis = new BufferedReader(new InputStreamReader(responseStream), 65536)) {
                    String result = stripWhitespace(bis);
                    if (config.isRemoveNewXmlVersionHeader() && result.startsWith("<?xml version=\"1.1\"")) {
                        result = result.replaceFirst("version=\"1.1\"", "version=\"1.0\"");
                    }
                    return result;
                } catch (Throwable e) {
                    log.error("Failed to parse tika response for OSD {}", osdId, e);
                    throw new TikaFailedException("the response could not be read: " + e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            log.error("Failed to parse tika file of OSD {}: {}; re-try with backoff policy", osdId, e.getMessage());
            throw e;
        }
    }

    private static String stripWhitespace(BufferedReader bis) throws IOException {
        StringBuilder builder         = new StringBuilder();
        String        line;
        int           whitespaceCount = 0;
        while ((line = bis.readLine()) != null) {
            // Tika likes to add an illegal character:
            if (line.contains("&#0;")) {
                line = line.replace("&#0;", "");
            }
            String trimmedLine = line.trim();
            if (trimmedLine.length() < line.length()) {
                whitespaceCount += line.length() - trimmedLine.length();
                builder.append(trimmedLine);
            } else {
                builder.append(trimmedLine);
            }
            builder.append("\n");
        }
        log.debug("removed {} whitespace characters, {} remain", whitespaceCount, builder.length());
        return builder.toString();
    }


    public boolean isEnabled() {
        return config.isUseTika();
    }

    public String parseWithTika(InputStream contentStream, Long contentLength, Format format, Long osdId) {
        // if the content comes from the file system anyway, this is some overhead.
        // if (some day) it comes from an API, the retry mechanism is easier when using a file
        // instead of an inputStream that may have been closed / cannot be reset().
        return Failsafe.with(retryPolicy).get(() -> parseData(contentStream, contentLength, format, osdId));
    }

    /**
     * Ask Tika and store what comes back as the object's tika metaset.
     * <p>
     * The metaset type is fetched through {@link #getTikaMetasetTypeId()} rather than read from the
     * field: a guard on the raw field would make this method a silent no-op whenever it had not been
     * populated yet - no metaset stored, no failure recorded, and the object offered again on every
     * pass forever.
     *
     * @return true if a metaset was stored, false if this object is not a candidate for extraction
     */
    public boolean convertContentToTikaMetaset(ObjectSystemData osd, InputStream contentStream, Format format) throws
            IOException {
        if (!isEnabled() || format.getIndexMode() != IndexMode.TIKA || osd.getContentSize() <= 0) {
            // The queue selects on exactly these properties, so for TikaService itself this is
            // unreachable; a direct caller gets a no-op rather than an empty metaset.
            log.debug("OSD #{} is not a candidate for Tika extraction", osd.getId());
            return false;
        }
        Long metasetTypeId = getTikaMetasetTypeId();
        log.debug("Parse OSD #{} with Tika", osd.getId());
        String tikaMetadata = parseWithTika(contentStream, osd.getContentSize(), format, osd.getId());
        tikaMetadata = tikaMetadata.replace(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
        if (config.isRemoveNewXmlVersionHeader() && tikaMetadata.startsWith("<?xml version=\"1.1\"")) {
            tikaMetadata = tikaMetadata.replaceFirst("version=\"1.1\"", "version=\"1.0\"");
        }
        log.trace("Tika returned: {}", tikaMetadata);
        try (SqlSession sqlSession = CinnamonServer.getSqlSession()) {
            // TODO: do not load existing tikaMetaset (memory consumption)
            OsdMetaDao     osdMetaDao  = new OsdMetaDao(sqlSession);
            Optional<Meta> tikaMetaset = osdMetaDao.listWithoutContentByOsd(osd.getId()).stream().filter(meta -> meta.getTypeId().equals(metasetTypeId)).findFirst();
            if (tikaMetaset.isPresent()) {
                Meta tikaMeta = tikaMetaset.get();
                tikaMeta.setContent(tikaMetadata);
                osdMetaDao.update(List.of(tikaMeta));
            } else {
                Meta tikaMeta = new Meta(osd.getId(), metasetTypeId, tikaMetadata);
                osdMetaDao.create(List.of(tikaMeta));
            }
            // An extraction that worked clears the OSD's failure history, so a document that
            // was rejected while Tika was broken or misconfigured starts over from zero
            // attempts instead of keeping an old count that could later give up on it.
            new TikaFailureDao(sqlSession).deleteByOsdIds(List.of(osd.getId()));
            IndexJobDao indexJobDao = new IndexJobDao(sqlSession);
            indexJobDao.insertIndexJob(new IndexJob(IndexJobType.OSD, osd.getId(), IndexJobAction.UPDATE), false);
            sqlSession.commit();
        }
        return true;
    }

}
