package com.pfiks.intelligus.events.exception;

/**
 * Exception used to wrap all exceptions that occur when calling eventbrite APIs
 *
 * @author Ilenia Zedda
 */
public class EventbriteException extends Exception {

    private static final long serialVersionUID = 1L;

    public EventbriteException() {
	super();
    }

    public EventbriteException(final Throwable exception) {
	super(exception);
    }

    public EventbriteException(final String message) {
	super(message);
    }

    public EventbriteException(final String message, final Throwable exception) {
	super(message, exception);
    }

}
