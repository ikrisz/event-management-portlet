package com.pfiks.intelligus.events.model.event;

import org.apache.commons.lang.StringUtils;

public class EventTicket {

    private String ticketId;
    private String name;
    private String quantityAvailable;
    private String quantitySold;
    private String price;
    private String type;

    public String getTicketId() {
	return ticketId;
    }

    public void setTicketId(final String ticketId) {
	this.ticketId = getValFixed(ticketId);
    }

    public String getName() {
	return name;
    }

    public void setName(final String name) {
	this.name = getValFixed(name);
    }

    public String getQuantityAvailable() {
	return quantityAvailable;
    }

    public void setQuantityAvailable(final String quantityAvailable) {
	this.quantityAvailable = getValFixed(quantityAvailable);
    }

    public String getPrice() {
	return price;
    }

    public void setPrice(final String price) {
	this.price = getValFixed(price);
    }

    public String getType() {
	return type;
    }

    public void setType(final String type) {
	this.type = getValFixed(type);
    }

    public String getQuantitySold() {
	return quantitySold;
    }

    public void setQuantitySold(final String quantitySold) {
	this.quantitySold = quantitySold;
    }

    public boolean isNullTicket() {
	return StringUtils.isBlank(type) && StringUtils.isBlank(name) && StringUtils.isBlank(ticketId) && StringUtils.isBlank(price) && StringUtils.isBlank(quantityAvailable);
    }

    private String getValFixed(final String value) {
	String result = value;
	if (StringUtils.endsWith(value, ",")) {
	    result = StringUtils.removeEnd(value, ",");
	}
	return result;
    }

}
