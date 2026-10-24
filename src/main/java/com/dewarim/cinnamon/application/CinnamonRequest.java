package com.dewarim.cinnamon.application;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.response.CinnamonContentType;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.*;

public class CinnamonRequest extends HttpServletRequestWrapper {

    private static final Logger                     log                  = LoggerFactory.getLogger(CinnamonRequest.class);
    private              CinnamonServletInputStream byteInput;
    private              boolean                    useCopy              = false;
    private final        boolean                    multiPart;
    private final        HttpServletRequest         request;
    private              Part                       cinnamonRequestPart;
    private              FilePart                   filePart;
    private              String                     filename;
    private              boolean                    useCopiedFileContent = false;
    private final        ObjectMapper               mapper;

    public CinnamonRequest(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this.request = request;
        Optional<String> contentTypeOpt = Optional.ofNullable(request.getContentType());
        // Locale.ROOT: HTTP header values are case-insensitive, so a client may send
        // "MULTIPART/form-data". With the default locale this comparison would break on a Turkish
        // system, where "I" lowercases to a dotless "ı" and the prefix no longer matches.
        multiPart = contentTypeOpt.map(s -> s.toLowerCase(Locale.ROOT).startsWith(MULTIPART)).orElse(false);
        CinnamonContentType cinnamonContentType;
        if (multiPart) {
            cinnamonContentType = CinnamonContentType.getByHttpContentType(contentTypeOpt.orElse(CONTENT_TYPE_XML));
        } else {
            cinnamonContentType = CinnamonContentType.getByHttpContentType(request.getContentType());
        }
        mapper = cinnamonContentType.getObjectMapper();
    }

    public void copyInputStream(boolean copyFileContent) throws IOException, ServletException {
        if (multiPart) {
            Part crp = request.getPart(CINNAMON_REQUEST_PART);
            if (crp == null) {
                throw ErrorCode.MISSING_REQUEST_PAYLOAD.exception();
            }
            cinnamonRequestPart = new RequestPart(crp);
            if (copyFileContent) {
                /*
                 We may have createOsd requests that do not have a file.
                 In that case, the MicroserviceChangeTrigger skips adding the file part.
                 */
                Part fp = request.getPart("file");
                if (fp != null) {
                    filePart = new FilePart(fp);
                    filename = filePart.getTempFile().getAbsolutePath();
                }
                useCopiedFileContent = true;
            }
        } else {
            byteInput = new CinnamonServletInputStream(new ByteArrayInputStream(readBodyBounded()));
        }
        useCopy = true;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (useCopy) {
            log.debug("byteInput: '{}'", byteInput.getContent());
        } else {
            byteInput = new CinnamonServletInputStream(new ByteArrayInputStream(readBodyBounded()));
            useCopy = true;
        }
        return byteInput;
    }

    /**
     * Read the (non-multipart) request body into memory, but never more than the configured
     * maxRequestBodySize, so a large body cannot exhaust the heap. A declared Content-Length over the
     * limit is rejected up front; since Content-Length may be absent (chunked) or untruthful, the limit
     * is also enforced while reading.
     */
    private byte[] readBodyBounded() throws IOException {
        long maxBytes = CinnamonServer.getConfig().getHttpLimitsConfig().getMaxRequestBodySize();
        if (maxBytes <= 0) {
            // 0 = no limit
            return getRequest().getInputStream().readAllBytes();
        }
        if (request.getContentLengthLong() > maxBytes) {
            throw ErrorCode.REQUEST_PAYLOAD_TOO_LARGE.exception();
        }
        int     toRead = (int) Math.min(maxBytes + 1, Integer.MAX_VALUE);
        byte[]  data   = getRequest().getInputStream().readNBytes(toRead);
        if (data.length > maxBytes) {
            throw ErrorCode.REQUEST_PAYLOAD_TOO_LARGE.exception();
        }
        return data;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        if (useCopy && multiPart && CINNAMON_REQUEST_PART.equals(name)) {
            return cinnamonRequestPart;
        }
        if (multiPart && "file".equals(name) && useCopiedFileContent) {
            return filePart;
        }
        return super.getPart(name);
    }

    public void deleteTempFile() throws IOException {
        if (filePart != null && useCopy) {
            filePart.delete();
        }
    }

    public CinnamonServletInputStream getByteInput() {
        return byteInput;
    }

    public boolean isMultiPart() {
        return multiPart;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    public Part getCinnamonRequestPart() {
        return cinnamonRequestPart;
    }

    public String getFilename() {
        return filename;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

}
