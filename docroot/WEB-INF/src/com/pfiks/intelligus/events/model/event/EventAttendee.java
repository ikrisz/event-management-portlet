package com.pfiks.intelligus.events.model.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;

public class EventAttendee {

    private String firstName;
    private String lastName;
    private String emailAddress;
    private User user;
    private String portraitUrl;

    List<EventTicket> ticketsPurchased;

    public EventAttendee() {
	ticketsPurchased = Lists.newArrayList();
    }

    public String getFirstName() {
	return firstName;
    }

    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public void setLastName(String lastName) {
	this.lastName = lastName;
    }

    public String getEmailAddress() {
	return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
	this.emailAddress = emailAddress;
    }

    public User getUser() {
	return user;
    }

    public void setUser(User liferayUser) {
	user = liferayUser;
    }

    public boolean isLiferayUser() {
	return Validator.isNotNull(user) && user.isActive();
    }

    public List<EventTicket> getTicketsPurchased() {
	return ticketsPurchased;
    }

    public void setTicketsPurchased(List<EventTicket> ticketsPurchased) {
	this.ticketsPurchased = ticketsPurchased;
    }

    public void addTicketsPurchased(List<EventTicket> ticketsToAdd) {
	if (ticketsPurchased == null) {
	    ticketsPurchased = Lists.newArrayList();
	}
	if (ticketsToAdd != null && !ticketsToAdd.isEmpty()) {
	    ticketsPurchased.addAll(ticketsToAdd);
	}
    }

    public void addTicketPurchased(EventTicket ticket) {
	if (ticketsPurchased == null) {
	    ticketsPurchased = Lists.newArrayList();
	}
	if (ticket != null) {
	    ticketsPurchased.add(ticket);
	}
    }

    public String getPortraitUrl() {
	return portraitUrl;
    }

}
