package com.dewarim.cinnamon.application;

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;

public class FilePart implements Part {


    private final Part part;
    private final File tempFile;

    public FilePart(Part part) throws IOException {
        this.part = part;
        this.tempFile = Files.createTempFile("cinnamon-file-upload-", ".binary").toFile();
        part.write(tempFile.getAbsolutePath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(tempFile);
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
        // already done in constructor.
        throw new IllegalStateException("Cannot write part to disk twice.");
    }

    @Override
    public void delete() throws IOException {
        part.delete();
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
