package com.pfiks.intelligus.events.utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.liferay.portal.kernel.cal.DayAndPosition;
import com.liferay.portal.kernel.cal.TZSRecurrence;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventbriteDetails;
import com.pfiks.intelligus.util.ContentSecurityLevel;

class EventModelBuilder {

    private static final Log LOG = LogFactoryUtil.getLog(EventModelBuilder.class);

    private final EventModel event;
    private final Map<String, DateTime> eventRecurrenceDates = Maps.newHashMap();

    private final DateTimeFormatter databaseFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
    private final SimpleDateFormat simpleDFdatabase = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    
    private DateTimeZone dateTimeZone = DateTimeZone.UTC;

    /**
     * Calculates the endDate based on startDate and allDay/duration
     *
     * @param calEvent
     */
    protected EventModelBuilder withEndDateCalculatedAsDuration(final CalEvent calEvent) {
	DateTime endDateTime = event.getDates().getStartDate();

	if (event.getDates().isAllDay()) {
	    endDateTime = endDateTime.withMinuteOfHour(59);
	    endDateTime = endDateTime.withHourOfDay(23);
	} else {
	    final int durationHour = calEvent.getDurationHour();
	    final int durationMinute = calEvent.getDurationMinute();
	    endDateTime = endDateTime.plusMinutes(durationMinute);
	    endDateTime = endDateTime.plusHours(durationHour);
	    event.getDates().setDurationHour(durationHour);
	    event.getDates().setDurationMinute(durationMinute);
	}
	setEndDate(endDateTime);
	return this;
    }

    protected void addRecurrenceDate(final String uid, final Date date) {
	eventRecurrenceDates.put(uid, getDateTimeFromDate(date));
    }

    private boolean getBooleanValueFromExpando(final ExpandoBridge expandoBridge, final String fieldName) {
	boolean result = false;
	try {
	    final Serializable value = expandoBridge.getAttribute(fieldName);
	    if (value != null) {
		result = (Boolean) value;
	    }
	} catch (final Exception e) {
	    LOG.debug("Exception getting boolean value from expando fieldName: " + fieldName + ", " + e.getMessage());
	}
	return result;
    }

    private String getStringValueFromExpando(final ExpandoBridge expandoBridge, final String fieldName) {
	String result = StringPool.BLANK;
	try {
	    final Serializable attribute = expandoBridge.getAttribute(fieldName);
	    if (Validator.isNotNull(attribute)) {
		result = (String) attribute;
	    }
	} catch (final Exception e) {
	    LOG.debug("Exception getting string value from expando fieldName: " + fieldName + ", " + e.getMessage());
	}
	return result;
    }

    private Collection<Integer> getDaySelectionFromDayAndPositionArray(final DayAndPosition[] values) {
	final Collection<Integer> transform = Lists.newArrayList(Iterables.transform(Lists.newArrayList(values), new Function<DayAndPosition, Integer>() {

	    @Override
	    public Integer apply(final DayAndPosition arg0) {
		return arg0.getDayOfWeek();
	    }
	}));
	return transform;
    }

    private int getMonthValueFromDateTimeVal(final int val) {
	return val - 1;
    }

    private void setEndDate(final DateTime endDateTime) {
	event.getDates().setEndDate(endDateTime);
	event.getDates().setEndMinute(endDateTime.getMinuteOfHour());
	event.getDates().setEndHour(endDateTime.getHourOfDay());
	event.getDates().setEndDay(endDateTime.getDayOfMonth());
	event.getDates().setEndMonth(getMonthValueFromDateTimeVal(endDateTime.getMonthOfYear()));
	event.getDates().setEndYear(endDateTime.getYear());
    }

    private void setMultidayEvent() {
	if (Validator.isNotNull(event.getDates().getStartDate()) && Validator.isNotNull(event.getDates().getEndDate())) {
	    final boolean differentDayValue = event.getDates().getStartDay() != event.getDates().getEndDay();
	    final boolean moreThan24Hours = event.getDates().getDurationHour() >= 24;
	    final boolean multiDay = differentDayValue || moreThan24Hours;
	    event.getDates().setMultiDay(multiDay);
	}
    }

    /**
     * Sets eventId, userId, groupId, companyId, user, title, description,
     * allDay, expando fields
     *
     * @param calEvent
     * @param timezone
     */
    protected EventModelBuilder(final CalEvent calEvent, TimeZone timezone) {
	event = new EventModel();
	if(timezone != null){
		dateTimeZone = DateTimeZone.forTimeZone(timezone);
	}
	
	event.setCalEvent(calEvent);
	event.setEventId(calEvent.getEventId());
	event.setUserId(calEvent.getUserId());
	event.setGroupId(calEvent.getGroupId());
	event.setCompanyId(calEvent.getCompanyId());
	event.setUserId(calEvent.getUserId());
	event.setTitle(calEvent.getTitle());
	event.setDescription(calEvent.getDescription());
	event.getDates().setAllDay(calEvent.isAllDay());

	final ExpandoBridge expandoBridge = calEvent.getExpandoBridge();
	if (expandoBridge != null) {
	    event.setSummary(getStringValueFromExpando(expandoBridge, EventExpandoConstants.SUMMARY));

	    event.setLocation(getStringValueFromExpando(expandoBridge, EventExpandoConstants.LOCATION));

	    if (event.getEventbrite() == null) {
		event.setEventbrite(new EventbriteDetails());
	    }
	    event.getEventbrite().setEventbriteId(getStringValueFromExpando(expandoBridge, EventExpandoConstants.EVENTBRITE_ID));
	    event.getEventbrite().setEventbriteUserApiKey(getStringValueFromExpando(expandoBridge, EventExpandoConstants.EVENTBRITE_USER_API));

			final String securityLevelValue = getStringValueFromExpando(expandoBridge, EventExpandoConstants.SECURITY_LEVEL);
			ContentSecurityLevel securityLevel = ContentSecurityLevel.GROUP;
			if (StringUtils.isNotBlank(securityLevelValue)) {
				securityLevel = ContentSecurityLevel.valueOf(securityLevelValue);
			}
			event.setSecurityLevel(securityLevel);

	    event.setFeaturedEvent(getBooleanValueFromExpando(expandoBridge, EventExpandoConstants.FEATURED));

	    final boolean onlineEvent = getBooleanValueFromExpando(expandoBridge, EventExpandoConstants.ONLINE_EVENT);
	    event.getVenue().setOnline(onlineEvent);
	    if (!onlineEvent) {
		event.getVenue().setName(getStringValueFromExpando(expandoBridge, EventExpandoConstants.VENUE));
		event.getVenue().setAddressLineOne(getStringValueFromExpando(expandoBridge, EventExpandoConstants.ADDRESS_1));
		event.getVenue().setAddressLineTwo(getStringValueFromExpando(expandoBridge, EventExpandoConstants.ADDRESS_2));
		event.getVenue().setCity(getStringValueFromExpando(expandoBridge, EventExpandoConstants.CITY));
		event.getVenue().setRegionState(getStringValueFromExpando(expandoBridge, EventExpandoConstants.REGION_STATE));
		event.getVenue().setZip(getStringValueFromExpando(expandoBridge, EventExpandoConstants.ZIP_CODE));
		event.getVenue().setCountry(getStringValueFromExpando(expandoBridge, EventExpandoConstants.COUNTRY));
	    }
	}
    }

	protected EventModelBuilder withRecurrenceDetails(final CalEvent calEvent, final boolean calculateDates) {
	RecurrenceTypes eventRecurrence = null;
	if (calEvent.isRepeating()) {
	    final TZSRecurrence recurrence = calEvent.getRecurrenceObj();

	    eventRecurrence = RecurrenceTypes.withType(recurrence.getFrequency());
	    
	    final DateTime recurrenceEndDate = new DateTime(recurrence.getUntil(), DateTimeZone.UTC);
	    event.getDates().setRecurrenceEndDate(recurrenceEndDate);
	    event.getDates().setRecurrenceEndDay(event.getDates().getRecurrenceEndDate().getDayOfMonth());
	    event.getDates().setRecurrenceEndMonth(getMonthValueFromDateTimeVal(event.getDates().getRecurrenceEndDate().getMonthOfYear()));
	    event.getDates().setRecurrenceEndYear(event.getDates().getRecurrenceEndDate().getYear());

	    final int interval = recurrence.getInterval();
	    if (eventRecurrence.equals(RecurrenceTypes.DAILY)) {
		event.getDates().setRecurrenceDayInterval(interval);

	    } else if (eventRecurrence.equals(RecurrenceTypes.WEEKLY)) {
		event.getDates().setRecurrenceWeekInterval(interval);
		event.getDates().setRecurrenceDaySelectionInterval(getDaySelectionFromDayAndPositionArray(recurrence.getByDay()));

	    } else if (eventRecurrence.equals(RecurrenceTypes.MONTHLY)) {
		event.getDates().setRecurrenceMonthInterval(interval);
		final int[] byMonthDay = recurrence.getByMonthDay();
		event.getDates().setRecurrenceDayInterval(byMonthDay[0]);
	    }
	    if (calculateDates) {
		calculateEventRecurrenceDatesFromDatabase(recurrence, eventRecurrence, event.getDates().getStartDate(), recurrenceEndDate);
	    }
	} else {
	    eventRecurrence = RecurrenceTypes.NONE;
	}
	// Is actually recurrent
	event.getDates().setRecurrenceType(eventRecurrence);
	event.getDates().setRecurrenceLabel(eventRecurrence.getLabel());
	return this;
    }

	private void calculateEventRecurrenceDatesFromDatabase(final TZSRecurrence recurrence, final RecurrenceTypes eventRecurrenceType, final DateTime dateToUpdate,
			final DateTime recurrenceEndDate) {
	Collection<DateTime> recurrentDates = Lists.newArrayList();
	if (eventRecurrenceType.equals(RecurrenceTypes.DAILY)) {
	    recurrentDates = dailyRecurrence(recurrence, dateToUpdate, recurrenceEndDate);

	} else if (eventRecurrenceType.equals(RecurrenceTypes.WEEKLY)) {
	    recurrentDates = weeklyRecurrence(recurrence, dateToUpdate, recurrenceEndDate);

	} else if (eventRecurrenceType.equals(RecurrenceTypes.MONTHLY)) {
	    recurrentDates = montlyRecurrence(recurrence, dateToUpdate, recurrenceEndDate);
	}
	for (final DateTime dateTime : recurrentDates) {
	    eventRecurrenceDates.put(String.valueOf(dateTime.getMillis()), dateTime);
	}
    }

	private Collection<DateTime> montlyRecurrence(final TZSRecurrence recurrence, DateTime dateToUpdate, final DateTime recurrenceEndDate) {
	final Collection<DateTime> results = Lists.newArrayList();
	// Every x months
	final int monthsInterval = recurrence.getInterval();
	// This is the day number of when is recurring, e.g. on the 15th of
	// every month
	final int[] byMonthDay = recurrence.getByMonthDay();
	final int dayNumberOfMonth = byMonthDay[0];

	while (dateToUpdate.isBefore(recurrenceEndDate)) {
	    dateToUpdate = dateToUpdate.plusMonths(monthsInterval);
	    final int maxDayOfMonth = dateToUpdate.dayOfMonth().getMaximumValue();
	    if (maxDayOfMonth >= dayNumberOfMonth) {
		dateToUpdate = dateToUpdate.withDayOfMonth(dayNumberOfMonth);
		if (dateToUpdate.isBefore(recurrenceEndDate)) {
		    results.add(dateToUpdate);
		}
	    }
	}
	return results;
    }

	private Collection<DateTime> weeklyRecurrence(final TZSRecurrence recurrence, DateTime dateToUpdate, final DateTime recurrenceEndDate) {
	final Collection<DateTime> results = Lists.newArrayList();
	// Every x weeks
	final int weeksInterval = recurrence.getInterval();
	// This is when is recurring. e.g. M, T, W, T, F, S, S
	final DayAndPosition[] byDay = recurrence.getByDay();
	while (dateToUpdate.isBefore(recurrenceEndDate)) {
	    dateToUpdate = dateToUpdate.plusWeeks(weeksInterval);
	    for (final DayAndPosition dayAndPosOfWeek : byDay) {
		// Offset between DateTime and Date
		int dayOfWeek = dayAndPosOfWeek.getDayOfWeek() - 1;
		if (dayOfWeek <= 0) {
		    dayOfWeek = 7;
		}
		dateToUpdate = dateToUpdate.withDayOfWeek(dayOfWeek);
		if (dateToUpdate.isBefore(recurrenceEndDate)) {
		    results.add(dateToUpdate);
		}
	    }
	}
	return results;
    }

	private Collection<DateTime> dailyRecurrence(final TZSRecurrence recurrence, DateTime dateToUpdate, final DateTime recurrenceEndDate) {
	final Collection<DateTime> results = Lists.newArrayList();
	final int daysInterval = recurrence.getInterval();

	while (dateToUpdate.isBefore(recurrenceEndDate)) {
	    dateToUpdate = dateToUpdate.plusDays(daysInterval);
	    if (dateToUpdate.isBefore(recurrenceEndDate)) {
		results.add(dateToUpdate);
	    }
	}
	return results;
    }

    protected EventModelBuilder withEndDate(final Date endDate) {
	if (Validator.isNotNull(endDate)) {
	    final DateTime endDateTime = getDateTimeFromDate(endDate);
	    setEndDate(endDateTime);
	}
	return this;
    }

    protected EventModelBuilder withStartDate(final Date startDate) {
	if (Validator.isNotNull(startDate)) {
	    final DateTime startDateTime = getDateTimeFromDate(startDate);
	    event.getDates().setStartDate(startDateTime);
	    event.getDates().setStartMinute(startDateTime.getMinuteOfHour());
	    event.getDates().setStartHour(startDateTime.getHourOfDay());
	    event.getDates().setStartDay(startDateTime.getDayOfMonth());
	    event.getDates().setStartMonth(getMonthValueFromDateTimeVal(startDateTime.getMonthOfYear()));
	    event.getDates().setStartYear(startDateTime.getYear());
	}
	return this;
    }

    protected EventModel getEventModel() {
	if (event.getDates().getRecurrenceDates() == null || event.getDates().getRecurrenceDates().isEmpty()) {
	    final DateTime startDate = event.getDates().getStartDate();
	    final Map<String, DateTime> futureDates = Maps.filterValues(eventRecurrenceDates, new Predicate<DateTime>() {

		@Override
		public boolean apply(final DateTime arg0) {
		    return arg0.isAfter(startDate);
		}
	    });

	    final Ordering<String> orderByDateAsc = Ordering.natural().nullsLast().onResultOf(Functions.forMap(futureDates, null)).compound(Ordering.natural());
	    event.getDates().setRecurrenceDates(ImmutableSortedMap.copyOf(futureDates, orderByDateAsc));
	}
	setDuration();
	setMultidayEvent();
	return event;
    }

    private void setDuration() {
	if (Validator.isNotNull(event.getDates().getStartDate()) && Validator.isNotNull(event.getDates().getEndDate())) {
	    if (event.getDates().getDurationHour() == 0 && event.getDates().getDurationMinute() == 0) {
		final int durationHour = Hours.hoursBetween(event.getDates().getStartDate(), event.getDates().getEndDate()).getHours();
		event.getDates().setDurationHour(durationHour);
		final int durationMinute = Minutes.minutesBetween(event.getDates().getStartDate(), event.getDates().getEndDate()).getMinutes() % 60;
		event.getDates().setDurationMinute(durationMinute);
	    }
	}
    }

    protected DateTime getDateTimeFromDate(final Date date) {
	DateTime result = null;
	if (Validator.isNotNull(date)) {
	    try {
		final String format = simpleDFdatabase.format(date);
		result = databaseFormatter.parseDateTime(format);
		result = result.withZone(dateTimeZone);
	    } catch (final IllegalArgumentException e) {
		LOG.info("Exception formatting date: " + e.getMessage());
	    }
	}
	return result;
    }

}