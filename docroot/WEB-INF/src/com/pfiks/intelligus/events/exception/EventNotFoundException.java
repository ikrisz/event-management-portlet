package com.pfiks.intelligus.events.exception;

/**
 * Exception used to wrap all exceptions that occur when calling eventbrite APIs
 *
 * @author Ilenia Zedda
 */
public class EventNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public EventNotFoundException() {
	super();
    }

    public EventNotFoundException(final Throwable exception) {
	super(exception);
    }

    public EventNotFoundException(final String message) {
	super(message);
    }

    public EventNotFoundException(final String message, final Throwable exception) {
	super(message, exception);
    }

}
