package com.pfiks.intelligus.events.exception;

/**
 * Exception used to wrap all Exceptions that may occur in the application,
 * apart from exceptions related to permissions and evenbrite integration
 *
 * @author Ilenia Zedda
 */
public class EventException extends Exception {

    private static final long serialVersionUID = 1L;

    public EventException() {
	super();
    }

    public EventException(final Throwable exception) {
	super(exception);
    }

    public EventException(final String message) {
	super(message);
    }

    public EventException(final String message, final Throwable exception) {
	super(message, exception);
    }

}
