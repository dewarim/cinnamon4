package com.dewarim.cinnamon.application.exception;

/**
 * The Tika server was reached but did not return usable metadata for this document: an HTTP status
 * other than 200, or a response that could not be read.
 * <p>
 * This is deliberately not an IOException: TikaService retries IOExceptions (a connection that
 * failed says nothing about the document), while a rejection is counted against the document's
 * attempts and eventually gives up on it. It must never be turned into metaset content - a stored
 * error document would look like a successful extraction to TikaService' query and the OSD would
 * never be looked at again.
 */
public class TikaFailedException extends CinnamonException {

    public TikaFailedException(String message) {
        super(message);
    }

    public TikaFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
