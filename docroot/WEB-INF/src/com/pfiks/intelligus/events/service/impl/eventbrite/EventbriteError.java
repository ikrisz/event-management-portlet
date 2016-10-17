package com.pfiks.intelligus.events.service.impl.eventbrite;

public class EventbriteError {

    private String errorMessage;
    private String errorLabel;

    public String getErrorMessage() {
	return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
	this.errorMessage = errorMessage;
    }

    public String getErrorLabel() {
	return errorLabel;
    }

    public void setErrorLabel(final String errorLabel) {
	this.errorLabel = errorLabel;
    }

}
