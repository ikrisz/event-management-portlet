package com.pfiks.intelligus.events.model.event;

import java.util.List;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.util.portlet.PortletProps;

public class EventbriteDetails {
    private static final String defaultCurrencyCode = GetterUtil.getString(PortletProps.get("event.default.currency"), StringPool.BLANK);

    private String eventbriteId;
    private String eventbriteUserApiKey;
    private String url;
    private String status;
    private DateTime modifiedDate;
    private boolean recurrent;
    private boolean multiday;
    private String currency;
    private List<EventTicket> tickets;
    private List<EventAttendee> attendees;

    private PaymentMethod payment;

    private DateTime ticketsStartDate;
    private int ticketsStartMinute;
    private int ticketsStartHour;
    private int ticketsStartDay;
    private int ticketsStartMonth;
    private int ticketsStartYear;

    // End date
    @Expose
    @SerializedName("end")
    private DateTime ticketsEndDate;
    private int ticketsEndMinute;
    private int ticketsEndHour;
    private int ticketsEndDay;
    private int ticketsEndMonth;
    private int ticketsEndYear;

    public EventbriteDetails() {
	currency = defaultCurrencyCode;
	payment = new PaymentMethod();
	tickets = Lists.newArrayList(new EventTicket());
	attendees = Lists.newArrayList();

	final DateTime now = DateTime.now().plusHours(1);
	ticketsStartMinute = now.getMinuteOfHour();
	ticketsStartHour = now.getHourOfDay();
	ticketsStartDay = now.getDayOfMonth();
	ticketsStartMonth = now.getMonthOfYear() - 1;
	ticketsStartYear = now.getYear();

	ticketsEndMinute = now.getMinuteOfHour();
	ticketsEndHour = now.plusHours(1).getHourOfDay();
	ticketsEndDay = now.getDayOfMonth();
	ticketsEndMonth = now.getMonthOfYear() - 1;
	ticketsEndYear = now.getYear();

    }

    public String getEventbriteId() {
	return eventbriteId;
    }

    public void setEventbriteId(final String eventbriteId) {
	this.eventbriteId = eventbriteId;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(final String url) {
	this.url = url;
    }

    public String getCurrency() {
	return currency;
    }

    public void setCurrency(final String currency) {
	this.currency = currency;
    }

    public PaymentMethod getPayment() {
	return payment;
    }

    public void setPayment(final PaymentMethod payment) {
	this.payment = payment;
    }

    public List<EventTicket> getTickets() {
	return tickets;
    }

    public void setTickets(final List<EventTicket> tickets) {
	this.tickets = tickets;
    }

    public List<EventAttendee> getAttendees() {
	return attendees;
    }

    public void setAttendees(List<EventAttendee> attendees) {
	this.attendees = attendees;
    }

    public DateTime getTicketsStartDate() {
	return ticketsStartDate;
    }

    public void setTicketsStartDate(final DateTime ticketStartDate) {
	ticketsStartDate = ticketStartDate;
    }

    public int getTicketsStartMinute() {
	return ticketsStartMinute;
    }

    public void setTicketsStartMinute(final int ticketsStartMinute) {
	this.ticketsStartMinute = ticketsStartMinute;
    }

    public int getTicketsStartHour() {
	return ticketsStartHour;
    }

    public void setTicketsStartHour(final int ticketsStartHour) {
	this.ticketsStartHour = ticketsStartHour;
    }

    public int getTicketsStartDay() {
	return ticketsStartDay;
    }

    public void setTicketsStartDay(final int ticketsStartDay) {
	this.ticketsStartDay = ticketsStartDay;
    }

    public int getTicketsStartMonth() {
	return ticketsStartMonth;
    }

    public void setTicketsStartMonth(final int ticketsStartMonth) {
	this.ticketsStartMonth = ticketsStartMonth;
    }

    public int getTicketsStartYear() {
	return ticketsStartYear;
    }

    public void setTicketsStartYear(final int ticketsStartYear) {
	this.ticketsStartYear = ticketsStartYear;
    }

    public DateTime getTicketsEndDate() {
	return ticketsEndDate;
    }

    public void setTicketsEndDate(final DateTime ticketsEndDate) {
	this.ticketsEndDate = ticketsEndDate;
    }

    public int getTicketsEndMinute() {
	return ticketsEndMinute;
    }

    public void setTicketsEndMinute(final int ticketsEndMinute) {
	this.ticketsEndMinute = ticketsEndMinute;
    }

    public int getTicketsEndHour() {
	return ticketsEndHour;
    }

    public void setTicketsEndHour(final int ticketsEndHour) {
	this.ticketsEndHour = ticketsEndHour;
    }

    public int getTicketsEndDay() {
	return ticketsEndDay;
    }

    public void setTicketsEndDay(final int ticketsEndDay) {
	this.ticketsEndDay = ticketsEndDay;
    }

    public int getTicketsEndMonth() {
	return ticketsEndMonth;
    }

    public void setTicketsEndMonth(final int ticketsEndMonth) {
	this.ticketsEndMonth = ticketsEndMonth;
    }

    public int getTicketsEndYear() {
	return ticketsEndYear;
    }

    public void setTicketsEndYear(final int ticketsEndYear) {
	this.ticketsEndYear = ticketsEndYear;
    }

    public String getEventbriteUserApiKey() {
	return eventbriteUserApiKey;
    }

    public void setEventbriteUserApiKey(final String eventbriteUserApiKey) {
	this.eventbriteUserApiKey = eventbriteUserApiKey;
    }

    public boolean isRecurrent() {
	return recurrent;
    }

    public void setRecurrent(final boolean recurrent) {
	this.recurrent = recurrent;
    }

    public boolean isMultiday() {
	return multiday;
    }

    public void setMultiday(final boolean multiday) {
	this.multiday = multiday;
    }

    public String getStatus() {
	return status;
    }

    public void setStatus(final String status) {
	this.status = status;
    }

    public DateTime getModifiedDate() {
	return modifiedDate;
    }

    public void setModifiedDate(DateTime modifiedDate) {
	this.modifiedDate = modifiedDate;
    }

}
