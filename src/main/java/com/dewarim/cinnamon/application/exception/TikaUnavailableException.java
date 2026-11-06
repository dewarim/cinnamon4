package com.dewarim.cinnamon.application.exception;

/**
 * Tika answered, but with a status that says nothing about the document: a gateway or proxy status,
 * a wrong baseUrl, a server that is restarting, or a request that was refused before any parser saw
 * the content.
 * <p>
 * The distinction from {@link TikaFailedException} decides who pays for the failure. A rejection is
 * counted against the document's attempts and eventually gives up on it; this one is not counted at
 * all, because every pending document would fail the same way for as long as the outage lasts and
 * counting it would write off the whole backlog. The document stays in the queue and is tried again
 * on the next pass.
 */
public class TikaUnavailableException extends CinnamonException {

    public TikaUnavailableException(String message) {
        super(message);
    }
}
