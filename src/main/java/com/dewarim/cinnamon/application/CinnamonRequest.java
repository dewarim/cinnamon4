package com.dewarim.cinnamon.application;


import com.dewarim.cinnamon.ErrorCode;
import com.dewarim.cinnamon.model.response.CinnamonContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final        CinnamonContentType        cinnamonContentType;
    private final        ObjectMapper               mapper;

    public CinnamonRequest(HttpServletRequest request, HttpServletResponse response) {
        super(request);
        this.request = request;
        Optional<String> contentTypeOpt = Optional.ofNullable(request.getContentType());
        multiPart = contentTypeOpt.map(s -> s.toLowerCase().startsWith(MULTIPART)).orElse(false);
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
            byteInput = new CinnamonServletInputStream(new ByteArrayInputStream(getRequest().getInputStream().readAllBytes()));
        }
        useCopy = true;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
//        log.debug("reading input stream, useCopy: {}",useCopy);
        if (useCopy) {
            log.debug("byteInput: '{}'", byteInput.getContent());
            return byteInput;
        } else {
            return request.getInputStream();
        }
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
