package com.dewarim.cinnamon.client;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class wraps the ClassicHttpResponse and consumes its content, so the httpClient can
 * safely close the connection.
 * <br/>
 * It would be better if we could consume the response without wrapping, but the migration to
 * httpClient5 is quite complex (sendStandardRequest is used in more than 130 places, which would all need refactoring)
 */
public class StandardResponse implements ClassicHttpResponse {

    private static final IllegalStateException READ_ONLY = new IllegalStateException("read-only object");
    private final        ClassicHttpResponse   response;
    private final        StandardEntity        entity;

    public StandardResponse(ClassicHttpResponse response) throws IOException {
        this.response = response;
        this.entity = new StandardEntity(response.getEntity());
    }

    @Override
    public void close() throws IOException {
        response.close();
    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    public static class StandardEntity implements HttpEntity {

        private final HttpEntity httpEntity;
        private final byte[]     content;

        public StandardEntity(HttpEntity httpEntity) throws IOException {
            this.httpEntity = httpEntity;
            content = httpEntity.getContent().readAllBytes();
        }

        @Override
        public boolean isRepeatable() {
            return httpEntity.isRepeatable();
        }

        @Override
        public InputStream getContent() throws IOException, UnsupportedOperationException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void writeTo(OutputStream outStream) throws IOException {
            outStream.write(content);
        }

        @Override
        public boolean isStreaming() {
            return false;
        }

        @Override
        public Supplier<List<? extends Header>> getTrailers() {
            return httpEntity.getTrailers();
        }

        @Override
        public void close() throws IOException {
            httpEntity.close();
        }

        @Override
        public long getContentLength() {
            return httpEntity.getContentLength();
        }

        @Override
        public String getContentType() {
            return httpEntity.getContentType();
        }

        @Override
        public String getContentEncoding() {
            return httpEntity.getContentEncoding();
        }

        @Override
        public boolean isChunked() {
            return httpEntity.isChunked();
        }

        @Override
        public Set<String> getTrailerNames() {
            return httpEntity.getTrailerNames();
        }
    }

    @Override
    public void setEntity(HttpEntity entity) {
        throw READ_ONLY;
    }

    @Override
    public int getCode() {
        return response.getCode();
    }

    @Override
    public void setCode(int code) {
        throw READ_ONLY;

    }

    @Override
    public String getReasonPhrase() {
        return response.getReasonPhrase();
    }

    @Override
    public void setReasonPhrase(String reason) {
        throw READ_ONLY;

    }

    @Override
    public Locale getLocale() {
        return response.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        throw READ_ONLY;

    }

    @Override
    public void setVersion(ProtocolVersion version) {
        throw READ_ONLY;

    }

    @Override
    public ProtocolVersion getVersion() {
        return response.getVersion();
    }

    @Override
    public void addHeader(Header header) {
        throw READ_ONLY;

    }

    @Override
    public void addHeader(String name, Object value) {
        throw READ_ONLY;

    }

    @Override
    public void setHeader(Header header) {
        throw READ_ONLY;

    }

    @Override
    public void setHeader(String name, Object value) {
        throw READ_ONLY;

    }

    @Override
    public void setHeaders(Header... headers) {
        throw READ_ONLY;

    }

    @Override
    public boolean removeHeader(Header header) {
        throw READ_ONLY;
    }

    @Override
    public boolean removeHeaders(String name) {
        throw READ_ONLY;
    }

    @Override
    public boolean containsHeader(String name) {
        return response.containsHeader(name);
    }

    @Override
    public int countHeaders(String name) {
        return response.countHeaders(name);
    }

    @Override
    public Header getFirstHeader(String name) {
        return response.getFirstHeader(name);
    }

    @Override
    public Header getHeader(String name) throws ProtocolException {
        return response.getHeader(name);
    }

    @Override
    public Header[] getHeaders() {
        return response.getHeaders();
    }

    @Override
    public Header[] getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public Header getLastHeader(String name) {
        return response.getLastHeader(name);
    }

    @Override
    public Iterator<Header> headerIterator() {
        return response.headerIterator();
    }

    @Override
    public Iterator<Header> headerIterator(String name) {
        return response.headerIterator(name);
    }
}
