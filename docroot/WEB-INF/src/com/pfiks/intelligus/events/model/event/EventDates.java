package com.pfiks.intelligus.events.model.event;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfiks.intelligus.events.model.RecurrenceTypes;

public class EventDates implements Serializable {
    private static final long serialVersionUID = 1L;

    @Expose
    @SerializedName("allDay")
    private boolean allDay;

    private boolean multiDay;

    private int durationHour;
    private int durationMinute;

    // Start date
    @Expose
    @SerializedName("start")
    private DateTime startDate;

    private int startMinute;
    private int startHour;
    private int startDay;
    private int startMonth;
    private int startYear;

    // End date
    @Expose
    @SerializedName("end")
    private DateTime endDate;

    private int endMinute;
    private int endHour;
    private int endDay;
    private int endMonth;
    private int endYear;

    // Recurrence
    private int recurrenceEndDay;
    private int recurrenceEndMonth;
    private int recurrenceEndYear;
    private DateTime recurrenceEndDate;

    private int recurrenceDayInterval;
    private int recurrenceWeekInterval;
    private int recurrenceMonthInterval;
    private Collection<Integer> recurrenceDaySelectionInterval;

    //
    private String recurrenceLabel;
    private RecurrenceTypes recurrenceType;

    private Map<String, DateTime> recurrenceDates;

    private DateTimeZone timeZone;

    public EventDates() {
	final DateTime now = DateTime.now().plusHours(1);
	startMinute = now.getMinuteOfHour();
	startHour = now.getHourOfDay();
	startDay = now.getDayOfMonth();
	startMonth = now.getMonthOfYear() - 1;
	startYear = now.getYear();

	endMinute = now.getMinuteOfHour();
	endHour = now.plusHours(1).getHourOfDay();
	endDay = startDay;
	endMonth = startMonth;
	endYear = startYear;

	recurrenceEndDay = startDay;
	recurrenceEndMonth = startMonth;
	recurrenceEndYear = startYear;
    }

    public boolean isAllDay() {
	return allDay;
    }

    public void setAllDay(final boolean allDay) {
	this.allDay = allDay;
    }

    public boolean isMultiDay() {
	return multiDay;
    }

    public void setMultiDay(boolean multiDay) {
	this.multiDay = multiDay;
    }

    public int getDurationHour() {
	return durationHour;
    }

    public void setDurationHour(final int durationHour) {
	this.durationHour = durationHour;
    }

    public int getDurationMinute() {
	return durationMinute;
    }

    public void setDurationMinute(final int durationMinute) {
	this.durationMinute = durationMinute;
    }

    public DateTime getStartDate() {
	return startDate;
    }

    public void setStartDate(final DateTime startDate) {
	this.startDate = startDate;
    }

    public DateTime getEndDate() {
	return endDate;
    }

    public void setEndDate(final DateTime endDate) {
	this.endDate = endDate;
    }

    public DateTime getRecurrenceEndDate() {
	return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(final DateTime recurrenceEndDate) {
	this.recurrenceEndDate = recurrenceEndDate;
    }

    public int getRecurrenceDayInterval() {
	return recurrenceDayInterval;
    }

    public void setRecurrenceDayInterval(final int recurrenceDayInterval) {
	this.recurrenceDayInterval = recurrenceDayInterval;
    }

    public int getRecurrenceWeekInterval() {
	return recurrenceWeekInterval;
    }

    public void setRecurrenceWeekInterval(final int recurrenceWeekInterval) {
	this.recurrenceWeekInterval = recurrenceWeekInterval;
    }

    public int getRecurrenceMonthInterval() {
	return recurrenceMonthInterval;
    }

    public void setRecurrenceMonthInterval(final int recurrenceMonthInterval) {
	this.recurrenceMonthInterval = recurrenceMonthInterval;
    }

    public Collection<Integer> getRecurrenceDaySelectionInterval() {
	return recurrenceDaySelectionInterval;
    }

    public void setRecurrenceDaySelectionInterval(final Collection<Integer> recurrenceDaySelectionInterval) {
	this.recurrenceDaySelectionInterval = recurrenceDaySelectionInterval;
    }

    public String getRecurrenceLabel() {
	return recurrenceLabel;
    }

    public void setRecurrenceLabel(final String recurrenceLabel) {
	this.recurrenceLabel = recurrenceLabel;
    }

    public RecurrenceTypes getRecurrenceType() {
	return recurrenceType;
    }

    public void setRecurrenceType(final RecurrenceTypes recurrenceType) {
	this.recurrenceType = recurrenceType;
    }

    public Map<String, DateTime> getRecurrenceDates() {
	return recurrenceDates;
    }

    public void setRecurrenceDates(final Map<String, DateTime> recurrenceDates) {
	this.recurrenceDates = recurrenceDates;
    }

    public int getStartMinute() {
	return startMinute;
    }

    public int getStartHour() {
	return startHour;
    }

    public int getStartDay() {
	return startDay;
    }

    public int getStartMonth() {
	return startMonth;
    }

    public int getStartYear() {
	return startYear;
    }

    public int getEndMinute() {
	return endMinute;
    }

    public int getEndHour() {
	return endHour;
    }

    public int getRecurrenceEndDay() {
	return recurrenceEndDay;
    }

    public int getRecurrenceEndMonth() {
	return recurrenceEndMonth;
    }

    public int getRecurrenceEndYear() {
	return recurrenceEndYear;
    }

    public void setStartMinute(final int startMinute) {
	this.startMinute = startMinute;
    }

    public void setStartHour(final int startHour) {
	this.startHour = startHour;
    }

    public void setStartDay(final int startDay) {
	this.startDay = startDay;
    }

    public void setStartMonth(final int startMonth) {
	this.startMonth = startMonth;
    }

    public void setStartYear(final int startYear) {
	this.startYear = startYear;
    }

    public void setEndMinute(final int endMinute) {
	this.endMinute = endMinute;
    }

    public void setEndHour(final int endHour) {
	this.endHour = endHour;
    }

    public void setEndDay(int endDay) {
	this.endDay = endDay;
    }

    public void setEndMonth(int endMonth) {
	this.endMonth = endMonth;
    }

    public void setEndYear(int endYear) {
	this.endYear = endYear;
    }

    public int getEndDay() {
	return endDay;
    }

    public int getEndMonth() {
	return endMonth;
    }

    public int getEndYear() {
	return endYear;
    }

    public void setRecurrenceEndDay(final int recurrenceEndDay) {
	this.recurrenceEndDay = recurrenceEndDay;
    }

    public void setRecurrenceEndMonth(final int recurrenceEndMonth) {
	this.recurrenceEndMonth = recurrenceEndMonth;
    }

    public void setRecurrenceEndYear(final int recurrenceEndYear) {
	this.recurrenceEndYear = recurrenceEndYear;
    }

    public DateTimeZone getTimeZone() {
	return timeZone;
    }

    public void setTimeZone(TimeZone tz) {
	timeZone = DateTimeZone.forTimeZone(tz);
    }

}
