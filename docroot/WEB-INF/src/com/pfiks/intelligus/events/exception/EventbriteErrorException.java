package com.pfiks.intelligus.events.exception;

/**
 * Exception used to wrap all exceptions that occur when calling eventbrite
 * APIs, but that are managed as application errors and mapped in file
 * eventbrite-api-errors.xml
 *
 * @author Ilenia Zedda
 */
public class EventbriteErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    public EventbriteErrorException() {
	super();
    }

    public EventbriteErrorException(final Throwable exception) {
	super(exception);
    }

    public EventbriteErrorException(final String message) {
	super(message);
    }

    public EventbriteErrorException(final String message, final Throwable exception) {
	super(message, exception);
    }

}
