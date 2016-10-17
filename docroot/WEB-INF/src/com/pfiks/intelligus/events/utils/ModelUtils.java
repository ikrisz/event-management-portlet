package com.pfiks.intelligus.events.utils;

import java.lang.reflect.Type;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.calendar.NoSuchEventException;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.retrieval.IRetrievalResponse;
import com.pfiks.intelligus.retrieval.RetrievalHit;

@Component
public class ModelUtils {

    private static final Log LOG = LogFactoryUtil.getLog(ModelUtils.class);

    private static final String SOLR_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String LUCENE_DATE_FORMAT = "yyyyMMddHHmmss";

    /**
     * Returns all the available countries as <Country Code, Country Label>
     * based on the Locale and saves them in portlet session
     *
     * @param request
     * @param locale
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getCountries(final PortletRequest request, final Locale locale) {
	Map<String, String> countries = (Map<String, String>) request.getPortletSession().getAttribute("event_session_country");
	if (Validator.isNull(countries) || countries.isEmpty()) {
	    final String[] locales = Locale.getISOCountries();
	    final Map<String, String> countriesToRetrieve = Maps.newHashMap();
	    for (final String countryCode : locales) {
		final Locale obj = new Locale("", countryCode);
		countriesToRetrieve.put(obj.getCountry(), obj.getDisplayCountry(locale));
	    }
	    final Ordering<String> orderByDateAsc = Ordering.natural().nullsLast().onResultOf(Functions.forMap(countriesToRetrieve, null));
	    countries = ImmutableSortedMap.copyOf(countriesToRetrieve, orderByDateAsc);

	    request.getPortletSession().setAttribute("event_session_country", countries);
	}
	return countries;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getCurrencies(final PortletRequest request) {
	Map<String, String> currencies = (Map<String, String>) request.getPortletSession().getAttribute("event_session_currency");
	if (Validator.isNull(currencies) || currencies.isEmpty()) {
	    final Map<String, String> currenciesToRetrieve = Maps.newHashMap();
	    final Locale[] locales = Locale.getAvailableLocales();
	    for (final Locale loc : locales) {
		try {
		    final Currency cur = Currency.getInstance(loc);
		    final String key = cur.getCurrencyCode();
		    String val = cur.getSymbol();
		    if (key.equals(val)) {
			val = "";
		    }
		    currenciesToRetrieve.put(key, val);
		} catch (final Exception exc) {
		    // Locale not found
		}
	    }
	    final Ordering<String> orderByDateAsc = Ordering.natural().nullsLast();
	    currencies = ImmutableSortedMap.copyOf(currenciesToRetrieve, orderByDateAsc);
	    request.getPortletSession().setAttribute("event_session_currency", currencies);
	}
	return currencies;
    }

    /**
     * Returns the days based on the Locale, as <Day Index, Day Label> and saves
     * it in the user session
     *
     * @param request
     * @param locale
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, String> getDays(final PortletRequest request, final Locale locale) {
	Map<Integer, String> daysMap = (Map<Integer, String>) request.getPortletSession().getAttribute("event_session_day");
	if (Validator.isNull(daysMap) || daysMap.isEmpty()) {
	    daysMap = Maps.newHashMap();
	    final DateFormatSymbols objDaySymbol = new DateFormatSymbols(locale);
	    final String symbolDayNames[] = objDaySymbol.getWeekdays();
	    for (int countDayname = 0; countDayname < symbolDayNames.length; countDayname++) {
		if (StringUtils.isNotBlank(symbolDayNames[countDayname])) {
		    daysMap.put(countDayname, symbolDayNames[countDayname]);
		}
	    }
	    request.getPortletSession().setAttribute("event_session_day", daysMap);
	}
	return daysMap;
    }

    public EventModel updateModelWithEventbriteDetails(final EventModel model, final EventModel eventbriteEvent) {
	if (Validator.isNotNull(eventbriteEvent)) {
	    final String eventbriteUserApiKey = model.getEventbrite().getEventbriteUserApiKey();
	    model.setEventbrite(eventbriteEvent.getEventbrite());
	    model.getEventbrite().setEventbriteUserApiKey(eventbriteUserApiKey);
	    model.setOrganizer(eventbriteEvent.getOrganizer());
	    if (Validator.isNotNull(eventbriteEvent.getVenue())) {
		model.getVenue().setVenueId(eventbriteEvent.getVenue().getVenueId());
	    }
	}
	return model;
    }

    public EventModel getLiferayEventModelForUpdate(final CalEvent calEvent, TimeZone timezone) {
	final EventModelBuilder eventModel = new EventModelBuilder(calEvent, timezone);
	eventModel.withRecurrenceDetails(calEvent, false).withStartDate(calEvent.getStartDate()).withEndDateCalculatedAsDuration(calEvent);
	return eventModel.getEventModel();
    }

    public Collection<EventModel> getEventModelsFromSearchResults(final IRetrievalResponse searchResults) throws EventException {
	final Collection<EventModel> events = Lists.newArrayList();
	final List<RetrievalHit> hits = searchResults.getHits();
	final Map<Long, User> eventUsers = Maps.newHashMap();
	for (final RetrievalHit hit : hits) {
	    final long eventId = hit.getLong(Field.ENTRY_CLASS_PK);
	    final CalEvent calEvent = getCalEvent(eventId);
	    if (Validator.isNotNull(calEvent)) {
		final EventModelBuilder eventModel = new EventModelBuilder(calEvent, null).withStartDate(getDateFromHitResult(hit, SearchConstants.START_DATE)).withEndDate(
			getDateFromHitResult(hit, SearchConstants.END_DATE));
		final EventModel event = eventModel.getEventModel();
		setCreator(eventUsers, event);
		event.setEventUid(hit.getString(Field.UID));
		events.add(event);
	    }
	}
	return Lists.newArrayList(Iterables.filter(events, Predicates.notNull()));
    }

    private void setCreator(final Map<Long, User> eventUsers, final EventModel event) {
	final Long userId = event.getUserId();
	User creator = null;
	if (eventUsers.containsKey(userId)) {
	    creator = eventUsers.get(userId);
	} else {
	    try {
		creator = UserLocalServiceUtil.fetchUser(userId);
		eventUsers.put(userId, creator);
	    } catch (final NestableException e) {
		LOG.warn("Exception retrieving creator of event " + e.getMessage());
	    }
	}
	event.setUser(creator);
    }

    private void setCreator(final EventModel event) {
	final Long userId = event.getUserId();
	User creator = null;
	try {
	    creator = UserLocalServiceUtil.fetchUser(userId);
	} catch (final NestableException e) {
	    LOG.warn("Exception retrieving creator of event " + e.getMessage());
	}
	event.setUser(creator);
    }

    public String getEventsJsonForCalendarView(final Collection<EventModel> events) {
	final GsonBuilder gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
	gson.registerTypeAdapter(DateTime.class, new DateTimeSerializer());
	return gson.create().toJson(events);
    }

    public EventModel getEventModelFromSearchResult(final List<RetrievalHit> hits, final CalEvent calEvent, final String eventUid) {
	final EventModelBuilder eventModel = new EventModelBuilder(calEvent, null);
	if (StringUtils.isNotBlank(eventUid)) {
	    for (final RetrievalHit hit : hits) {
		final String currentUid = hit.getString(Field.UID);
		final Date hitStartDate = getDateFromHitResult(hit, SearchConstants.START_DATE);

		if (currentUid.equalsIgnoreCase(eventUid)) {
		    eventModel.withStartDate(hitStartDate).withEndDate(getDateFromHitResult(hit, SearchConstants.END_DATE));
		} else {
		    eventModel.addRecurrenceDate(currentUid, hitStartDate);
		}
	    }
	}
	EventModel event = eventModel.getEventModel();
	if (Validator.isNull(event.getDates().getStartDate())) {
	    eventModel.withStartDate(calEvent.getStartDate()).withEndDateCalculatedAsDuration(calEvent).withRecurrenceDetails(calEvent, true);
	}
	event = eventModel.getEventModel();
	event.setEventUid(eventUid);
	setCreator(event);
	return event;
    }

    public void refreshEventValuesForUpdate(final EventModel event, TimeZone timeZone, final String venueRegionStateSelect, final String venueRegionStateText,
	    final Integer startDay, final Integer startMonth, final Integer startYear, final Integer endDay, final Integer endMonth, final Integer endYear,
	    final Integer[] recurrenceDaysSelection, final Integer recurrenceEndDay, final Integer recurrenceEndMonth, final Integer recurrenceEndYear) {
	updateEventVenue(event, venueRegionStateSelect, venueRegionStateText);
	updateEventDates(event, timeZone, startDay, startMonth, startYear, endDay, endMonth, endYear);
	updateEventRecurrence(event, recurrenceDaysSelection, recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);
    }

    public void refreshEventbriteEventValuesForUpdate(final EventModel event, TimeZone timeZone, final String venueRegionStateSelect, final String venueRegionStateText,
	    final Integer startDay, final Integer startMonth, final Integer startYear, final Integer ticketStartDay, final Integer ticketStartMonth, final Integer ticketStartYear,
	    final Integer ticketEndDay, final Integer ticketEndMonth, final Integer ticketEndYear) {
	updateEventVenue(event, venueRegionStateSelect, venueRegionStateText);
	updateEventDates(event, timeZone, startDay, startMonth, startYear, null, null, null);
	updateEventTicketDates(event, ticketStartDay, ticketStartMonth, ticketStartYear, ticketEndDay, ticketEndMonth, ticketEndYear);
    }

    private void updateEventTicketDates(final EventModel event, final Integer ticketStartDay, final Integer ticketStartMonth, final Integer ticketStartYear,
	    final Integer ticketEndDay, final Integer ticketEndMonth, final Integer ticketEndYear) {
	try {
	    if (isNotDefaultDateVal(ticketStartDay)) {
		event.getEventbrite().setTicketsStartDay(ticketStartDay);
	    }
	    if (isNotDefaultDateVal(ticketStartMonth)) {
		event.getEventbrite().setTicketsStartMonth(ticketStartMonth);
	    }
	    if (isNotDefaultDateVal(ticketStartYear)) {
		event.getEventbrite().setTicketsStartYear(ticketStartYear);
	    }

	    final DateTime dateTime = new DateTime(event.getEventbrite().getTicketsStartYear(), getMonthValueFromCalendarVal(event.getEventbrite().getTicketsStartMonth()), event
		    .getEventbrite().getTicketsStartDay(), event.getEventbrite().getTicketsStartHour(), event.getEventbrite().getTicketsStartMinute(), event.getDates().getTimeZone());

	    event.getEventbrite().setTicketsStartDate(dateTime);

	    if (isNotDefaultDateVal(ticketEndDay)) {
		event.getEventbrite().setTicketsEndDay(ticketEndDay);
	    }
	    if (isNotDefaultDateVal(ticketEndMonth)) {
		event.getEventbrite().setTicketsEndMonth(ticketEndMonth);
	    }
	    if (isNotDefaultDateVal(ticketEndYear)) {
		event.getEventbrite().setTicketsEndYear(ticketEndYear);
	    }

	    final DateTime dateTimeEnd = new DateTime(event.getEventbrite().getTicketsEndYear(), getMonthValueFromCalendarVal(event.getEventbrite().getTicketsEndMonth()), event
		    .getEventbrite().getTicketsEndDay(), event.getEventbrite().getTicketsEndHour(), event.getEventbrite().getTicketsEndMinute(), event.getDates().getTimeZone());

	    event.getEventbrite().setTicketsEndDate(dateTimeEnd);

	} catch (final Exception e) {
	    LOG.debug("Error setting eventDates values: " + e.getMessage());
	}
    }

    private void updateEventRecurrence(final EventModel event, final Integer[] recurrenceDayVals, final Integer recurrenceEndDay, final Integer recurrenceEndMonth,
	    final Integer recurrenceEndYear) {
	try {

	    if (isNotDefaultDateVal(recurrenceEndDay)) {
		event.getDates().setRecurrenceEndDay(recurrenceEndDay);
	    }
	    if (isNotDefaultDateVal(recurrenceEndMonth)) {
		event.getDates().setRecurrenceEndMonth(recurrenceEndMonth);
	    }
	    if (isNotDefaultDateVal(recurrenceEndYear)) {
		event.getDates().setRecurrenceEndYear(recurrenceEndYear);
	    }
	    if (ArrayUtils.isNotEmpty(ArrayUtils.nullToEmpty(recurrenceDayVals))) {
		event.getDates().setRecurrenceDaySelectionInterval(Lists.newArrayList(recurrenceDayVals));
	    }
	    if (StringUtils.isNotBlank(event.getDates().getRecurrenceLabel())) {
		event.getDates().setRecurrenceType(RecurrenceTypes.valueOf(event.getDates().getRecurrenceLabel().toUpperCase()));
		event.getDates().setRecurrenceEndDate(
			new DateTime(event.getDates().getRecurrenceEndYear(), getMonthValueFromCalendarVal(event.getDates().getRecurrenceEndMonth()), event.getDates()
				.getRecurrenceEndDay(), 23, 59, event.getDates().getTimeZone()));
	    }
	} catch (final Exception e) {
	    LOG.debug("Error setting event recurrence values: " + e.getMessage());
	}
    }

    private boolean isNotDefaultDateVal(final Integer val) {
	return Validator.isNotNull(val) && val != -3;
    }

    private void updateEventDates(final EventModel event, TimeZone timeZone, final Integer startDay, final Integer startMonth, final Integer startYear, final Integer endDay,
	    final Integer endMonth, final Integer endYear) {
	try {
	    event.getDates().setTimeZone(timeZone);
	    DateTimeZone dt = event.getDates().getTimeZone();
	    // Set start date values
	    if (isNotDefaultDateVal(startDay)) {
		event.getDates().setStartDay(startDay);
	    }
	    if (isNotDefaultDateVal(startMonth)) {
		event.getDates().setStartMonth(startMonth);
	    }
	    if (isNotDefaultDateVal(startYear)) {
		event.getDates().setStartYear(startYear);
	    }

	    if (event.getDates().isMultiDay()) {
		// Set end date values
		if (isNotDefaultDateVal(endDay)) {
		    event.getDates().setEndDay(endDay);
		}
		if (isNotDefaultDateVal(endMonth)) {
		    event.getDates().setEndMonth(endMonth);
		}
		if (isNotDefaultDateVal(endYear)) {
		    event.getDates().setEndYear(endYear);
		}

		if (event.getDates().isAllDay()) {
		    event.getDates().setStartHour(0);
		    event.getDates().setStartMinute(0);
		    event.getDates().setEndHour(23);
		    event.getDates().setEndMinute(59);
		}

		final DateTime startDateTime = new DateTime(event.getDates().getStartYear(), getMonthValueFromCalendarVal(event.getDates().getStartMonth()), event.getDates()
			.getStartDay(), event.getDates().getStartHour(), event.getDates().getStartMinute(), dt);
		event.getDates().setStartDate(startDateTime);

		final DateTime endDateTime = new DateTime(event.getDates().getEndYear(), getMonthValueFromCalendarVal(event.getDates().getEndMonth()),
			event.getDates().getEndDay(), event.getDates().getEndHour(), event.getDates().getEndMinute(), dt);
		event.getDates().setEndDate(endDateTime);

		final int durationHour = Hours.hoursBetween(startDateTime, endDateTime).getHours();
		event.getDates().setDurationHour(durationHour);
		final int durationMinute = Minutes.minutesBetween(startDateTime, endDateTime).getMinutes() % 60;
		event.getDates().setDurationMinute(durationMinute);
	    } else if (event.getDates().isAllDay()) {
		final DateTime startDateTime = new DateTime(event.getDates().getStartYear(), getMonthValueFromCalendarVal(event.getDates().getStartMonth()), event.getDates()
			.getStartDay(), 0, 0, dt);

		event.getDates().setStartDate(startDateTime.withHourOfDay(0).withMinuteOfHour(0));
		event.getDates().setEndDate(startDateTime.withHourOfDay(23).withMinuteOfHour(59));

		event.getDates().setDurationHour(24);
		event.getDates().setDurationMinute(0);
	    } else {
		// Calculate end date from startDate + duration
		final DateTime startDateTime = new DateTime(event.getDates().getStartYear(), getMonthValueFromCalendarVal(event.getDates().getStartMonth()), event.getDates()
			.getStartDay(), event.getDates().getStartHour(), event.getDates().getStartMinute(), dt);

		event.getDates().setStartDate(startDateTime);

		final DateTime endDateTime = startDateTime.withHourOfDay(event.getDates().getEndHour()).withMinuteOfHour(event.getDates().getEndMinute());
		event.getDates().setEndDate(endDateTime);

		final int durationHour = Hours.hoursBetween(startDateTime, endDateTime).getHours();
		event.getDates().setDurationHour(durationHour);
		final int durationMinute = Minutes.minutesBetween(startDateTime, endDateTime).getMinutes() % 60;
		event.getDates().setDurationMinute(durationMinute);
	    }
	} catch (final Exception e) {
	    LOG.debug("Error setting eventDates values: " + e.getMessage());
	}
    }

    private void updateEventVenue(final EventModel event, final String venueRegionStateSelect, final String venueRegionStateText) {
	if (StringUtils.isNotBlank(event.getVenue().getCountry()) && event.getVenue().getCountry().equals("US")) {
	    event.getVenue().setRegionState(venueRegionStateSelect);
	} else {
	    event.getVenue().setRegionState(venueRegionStateText);
	}
    }

    private int getMonthValueFromCalendarVal(final int val) {
	return val + 1;
    }

    private class DateTimeSerializer implements JsonSerializer<DateTime> {

	@Override
	public JsonElement serialize(final DateTime arg0, final Type arg1, final JsonSerializationContext arg2) {
	    final DateTimeFormatter databaseFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
	    return new JsonPrimitive(arg0.toString(databaseFormatter));
	}
    }

    private CalEvent getCalEvent(long eventId) throws EventException {
	try {
	    return CalEventLocalServiceUtil.getEvent(eventId);
	} catch (final NoSuchEventException e) {
	    LOG.error("NoSuchEventException with eventId: " + eventId);
	    return null;
	} catch (final NestableException e) {
	    LOG.error("Exception retrieving event with eventId: " + eventId + ". " + Throwables.getRootCause(e));
	    throw new EventException(e);
	}
    }

    private Date getDateFromHitResult(final RetrievalHit hit, final String dateFieldName) {
	Date result = null;
	try {
	    result = hit.getDate(dateFieldName, SOLR_DATE_FORMAT);
	    // This is for Lucene
	    if (Validator.isNull(result)) {
		result = hit.getDate(dateFieldName, LUCENE_DATE_FORMAT);
	    }
	} catch (final ParseException e) {
	    LOG.warn("Exception parsing date from retrievalHit: " + e.getMessage());
	}
	return result;
    }

}
