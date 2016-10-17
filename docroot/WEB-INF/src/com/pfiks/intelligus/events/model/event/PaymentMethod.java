package com.pfiks.intelligus.events.model.event;

public class PaymentMethod {

    // This is always true since API don't allow to specify bank account
    // details, and without a paypal tickets cannot be sold
    private boolean paypalAccepted = true;
    private String paypalEmail;

    private boolean checkAccepted;
    private String checkInstructions;

    private boolean cashAccepted;
    private String cashInstructions;

    private boolean invoiceAccepted;
    private String invoiceInstructions;

    public boolean isPaypalAccepted() {
	return paypalAccepted;
    }

    public void setPaypalAccepted(final boolean paypalAccepted) {
	this.paypalAccepted = paypalAccepted;
    }

    public String getPaypalEmail() {
	return paypalEmail;
    }

    public void setPaypalEmail(final String paypalEmail) {
	this.paypalEmail = paypalEmail;
    }

    public boolean isCheckAccepted() {
	return checkAccepted;
    }

    public void setCheckAccepted(final boolean checkAccepted) {
	this.checkAccepted = checkAccepted;
    }

    public String getCheckInstructions() {
	return checkInstructions;
    }

    public void setCheckInstructions(final String checkInstructions) {
	this.checkInstructions = checkInstructions;
    }

    public boolean isCashAccepted() {
	return cashAccepted;
    }

    public void setCashAccepted(final boolean cashAccepted) {
	this.cashAccepted = cashAccepted;
    }

    public String getCashInstructions() {
	return cashInstructions;
    }

    public void setCashInstructions(final String cashInstructions) {
	this.cashInstructions = cashInstructions;
    }

    public boolean isInvoiceAccepted() {
	return invoiceAccepted;
    }

    public void setInvoiceAccepted(final boolean invoiceAccepted) {
	this.invoiceAccepted = invoiceAccepted;
    }

    public String getInvoiceInstructions() {
	return invoiceInstructions;
    }

    public void setInvoiceInstructions(final String invoidInstructions) {
	invoiceInstructions = invoidInstructions;
    }

}
