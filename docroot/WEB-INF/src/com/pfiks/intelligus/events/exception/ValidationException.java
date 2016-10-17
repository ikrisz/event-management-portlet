package com.pfiks.intelligus.events.exception;

/**
 * Exception used to wrap exceptions that are managed as validation errors
 *
 * @author Ilenia Zedda
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidationException() {
	super();
    }

    public ValidationException(final Throwable exception) {
	super(exception);
    }

    public ValidationException(final String message) {
	super(message);
    }

    public ValidationException(final String message, final Throwable exception) {
	super(message, exception);
    }

}
