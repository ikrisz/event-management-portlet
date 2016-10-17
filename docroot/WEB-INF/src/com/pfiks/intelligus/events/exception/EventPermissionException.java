package com.pfiks.intelligus.events.exception;

/**
 * Exception used when permission checks fail
 * 
 * @author Ilenia Zedda
 */
public class EventPermissionException extends Exception {

    private static final long serialVersionUID = 1L;

    public EventPermissionException() {
	super();
    }

    public EventPermissionException(final Throwable exception) {
	super(exception);
    }

    public EventPermissionException(final String message) {
	super(message);
    }

    public EventPermissionException(final String message, final Throwable exception) {
	super(message, exception);
    }

}
