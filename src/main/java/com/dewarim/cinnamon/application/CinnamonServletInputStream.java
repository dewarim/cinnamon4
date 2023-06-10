package com.dewarim.cinnamon.application;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CinnamonServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream byteInput;

    public CinnamonServletInputStream(ByteArrayInputStream byteInput) {
        this.byteInput = byteInput;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new IllegalStateException("ReadListener not implemented on CinnamonServletInputStream.");
    }

    @Override
    public int read() throws IOException {
        return byteInput.read();
    }

    public String getContent() {
        return new String(byteInput.readAllBytes());
    }
}
