package com.dewarim.cinnamon.application;


import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.dewarim.cinnamon.api.Constants.CINNAMON_REQUEST_PART;
import static com.dewarim.cinnamon.api.Constants.MULTIPART;

public class CinnamonRequest extends HttpServletRequestWrapper {

    private       CinnamonServletInputStream byteInput;
    private       boolean                    useCopy = false;
    private final boolean                    multiPart;
    private final HttpServletRequest         request;
    private       Part                       cinnamonRequestPart;
    private       FilePart                   filePart;
    private       String                     filename;

    public CinnamonRequest(HttpServletRequest request) {
        super(request);
        this.request = request;
        Optional<String> contentType = Optional.ofNullable(request.getContentType());
        //                .orElseThrow(ErrorCode.NO_CONTENT_TYPE_IN_HEADER.getException());
        multiPart = contentType.map(s -> s.toLowerCase().startsWith(MULTIPART)).orElse(false);
    }

    public void copyInputStream(boolean copyFileContent) throws IOException, ServletException {
        if (multiPart) {
            cinnamonRequestPart = new RequestPart(request.getPart(CINNAMON_REQUEST_PART));
            if (copyFileContent) {
                filePart = new FilePart(request.getPart("file"));
                Path tempFile = Files.createTempFile("cinnamon-upload-", ".data");
                filename = tempFile.toAbsolutePath().toString();
                filePart.write(filename);
            }
        } else {
            byteInput = new CinnamonServletInputStream(new ByteArrayInputStream(getRequest().getInputStream().readAllBytes()));
        }
        useCopy = true;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (useCopy) {
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
        if (useCopy && multiPart && "file".equals(name)) {
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
}
