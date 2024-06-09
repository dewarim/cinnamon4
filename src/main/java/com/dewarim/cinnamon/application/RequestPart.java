package com.dewarim.cinnamon.application;

import jakarta.servlet.http.Part;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class RequestPart implements Part {

    private final Part                 part;
    private final ByteArrayInputStream bytePart;

    public RequestPart(Part part) throws IOException {
        this.part = part;
        bytePart = new ByteArrayInputStream(part.getInputStream().readAllBytes());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        bytePart.reset();
        return bytePart;
    }

    @Override
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public String getName() {
        return part.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return part.getSubmittedFileName();
    }

    @Override
    public long getSize() {
        return part.getSize();
    }

    @Override
    public void write(String fileName) throws IOException {
        throw new IllegalStateException("Writing cinnamonRequest to disk is not implemented");
    }

    @Override
    public void delete() throws IOException {
        throw new IllegalStateException("file I/O on cinnamonRequest is not supported");
    }

    @Override
    public String getHeader(String name) {
        return part.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return part.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return part.getHeaderNames();
    }
}
