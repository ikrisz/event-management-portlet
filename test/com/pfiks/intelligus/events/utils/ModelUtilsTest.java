package com.pfiks.intelligus.events.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portlet.calendar.model.CalEvent;
import com.pfiks.intelligus.events.ClassesConstructorsTest;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.retrieval.IRetrievalResponse;
import com.pfiks.intelligus.retrieval.RetrievalHit;

public class ModelUtilsTest extends ClassesConstructorsTest {

    private final ModelUtils eventModelUtil = new ModelUtils();
    final IRetrievalResponse resp = mock(IRetrievalResponse.class);

    private final Long searchEventId = 1L;
    private final Long searchUserId = 2L;
    private final Long searchGroupId = 3L;
    private final Long searchCompanyId = 4L;

    private final String eventUid = "searchUid";
    private final String title = "eventTitle";
    private final String description = "eventDescription";
    private final String country = "UK";
    private final String countryUS = "US";

    private final String stateText = "stateText";
    private final String stateSelect = "TX";
    private Integer startDay;
    private Integer startMonth;
    private Integer recurrenceEndDay;
    private Integer startYear;
    private Integer[] recurrenceDaysSelection = null;
    private Integer recurrenceEndMonth;
    private Integer recurrenceEndYear;
    private Integer ticketStartDay;
    private Integer ticketStartMonth;
    private Integer ticketStartYear;
    private Integer ticketEndDay;
    private Integer ticketEndMonth;
    private Integer ticketEndYear;

    @Test
    public void testThatEventsAreCreatedFromRetrievalHits() {
	final List<RetrievalHit> searchResultHits = Lists.newLinkedList();
	searchResultHits.add(aRetrievalHitWithValues(1));
	searchResultHits.add(aRetrievalHitWithValues(2));
	when(resp.getHits()).thenReturn(searchResultHits);
	final List<EventModel> eventsFromSearchResults = Lists.newArrayList(eventModelUtil.getEventModelsFromSearchResults(resp));
	assertThat(eventsFromSearchResults, hasSize(2));
	assertThat(eventsFromSearchResults.get(0), is(anEventWithValue(1)));
	assertThat(eventsFromSearchResults.get(1), is(anEventWithValue(2)));
    }

    @Test
    public void testThatJsonStringForEventsHasOnlyExposedFields() {
	final List<EventModel> events = Lists.newLinkedList();
	events.add(anEventWithValue(1));
	events.add(anEventWithValue(2));
	final String jsonString = eventModelUtil.getEventsJsonForCalendarView(events);
	assertThatFieldIsExposedAs(jsonString, "id");
	assertThatFieldIsNotExposedAs(jsonString, "eventId");

	assertThatFieldIsExposedAs(jsonString, "uid");
	assertThatFieldIsNotExposedAs(jsonString, "eventUid");

	assertThatFieldIsExposedAs(jsonString, "title");
	assertThatFieldIsExposedAs(jsonString, "start");
	assertThatFieldIsExposedAs(jsonString, "end");

	assertThatFieldIsNotExposedAs(jsonString, "groupId");
	assertThatFieldIsNotExposedAs(jsonString, "description");
    }

    @Test
    public void testEvent_State_has_valueFromSelect_when_countryIsUS() {
	final EventModel event = anEvent();
	event.getVenue().setCountry(countryUS);
	eventModelUtil.refreshEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, recurrenceDaysSelection,
		recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);

	assertThat(event.getVenue().getRegionState(), is(stateSelect));
    }

    @Test
    public void testEventBrite_State_has_valueFromSelect_when_countryIsUS() {
	final EventModel event = anEvent();
	event.getVenue().setCountry(countryUS);
	eventModelUtil.refreshEventbriteEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, ticketStartDay,
		ticketStartMonth, ticketStartYear, ticketEndDay, ticketEndMonth, ticketEndYear);
	assertThat(event.getVenue().getRegionState(), is(stateSelect));
    }

    @Test
    public void testEvent_State_has_valueFromText_when_countryIsNotUS() {
	final EventModel event = anEvent();
	event.getVenue().setCountry(country);
	eventModelUtil.refreshEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, recurrenceDaysSelection,
		recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);
	assertThat(event.getVenue().getRegionState(), is(stateText));
    }

    @Test
    public void testEventBrite_State_has_valueFromText_when_countryIsNotUS() {
	final EventModel event = anEvent();
	event.getVenue().setCountry(country);
	eventModelUtil.refreshEventbriteEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, ticketStartDay,
		ticketStartMonth, ticketStartYear, ticketEndDay, ticketEndMonth, ticketEndYear);
	assertThat(event.getVenue().getRegionState(), is(stateText));
    }

    @Test
    public void testEvent_DatesAndDurationsAreCorrectWhenAllDayEvent() {
	final EventBuilder eb = new EventBuilder().withStartDate(2013, 5, 10, 10, 25).withEndDate(15, 35);
	final EventModel event = eb.getEvent();
	event.getDates().setAllDay(true);
	eventModelUtil.refreshEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, recurrenceDaysSelection,
		recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);

	assertThat(event.getDates().getDurationHour(), is(24));
	assertThat(event.getDates().getDurationMinute(), is(0));

	assertThatDateHasHourAndMinutes(event.getDates().getStartDate(), 0, 0);
	assertThatDateHasHourAndMinutes(event.getDates().getEndDate(), 23, 59);
    }

    @Test
    public void testEventbrite_DatesAndDurationsAreCorrectWhenAllDayEvent() {
	final EventBuilder eb = new EventBuilder().withStartDate(2013, 5, 10, 10, 25).withEndDate(15, 35);
	final EventModel event = eb.getEvent();
	event.getDates().setAllDay(true);
	eventModelUtil.refreshEventbriteEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, ticketStartDay,
		ticketStartMonth, ticketStartYear, ticketEndDay, ticketEndMonth, ticketEndYear);

	assertThat(event.getDates().getDurationHour(), is(24));
	assertThat(event.getDates().getDurationMinute(), is(0));

	assertThatDateHasHourAndMinutes(event.getDates().getStartDate(), 0, 0);
	assertThatDateHasHourAndMinutes(event.getDates().getEndDate(), 23, 50);
    }

    @Test
    public void testEvent_DatesAndDurationsAreCorrectWhenEventIsNotAllDay() {
	final EventBuilder eb = new EventBuilder().withStartDate(2013, 5, 10, 10, 25).withEndDate(13, 40);
	final EventModel event = eb.getEvent();
	event.getDates().setAllDay(false);
	eventModelUtil.refreshEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, recurrenceDaysSelection,
		recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);

	assertThat(event.getDates().getDurationHour(), is(3));
	assertThat(event.getDates().getDurationMinute(), is(15));

	assertThat(event.getDates().getStartDate(), is(aDate(2013, 6, 10, 10, 25)));
	assertThat(event.getDates().getEndDate(), is(aDate(2013, 6, 10, 13, 40)));
    }

    @Test
    public void testEventbrite_DatesAndDurationsAreCorrectWhenEventIsNotAllDay() {
	final EventBuilder eb = new EventBuilder().withStartDate(2013, 5, 10, 10, 25).withEndDate(13, 40);
	final EventModel event = eb.getEvent();
	event.getDates().setAllDay(false);
	eventModelUtil.refreshEventbriteEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, ticketStartDay,
		ticketStartMonth, ticketStartYear, ticketEndDay, ticketEndMonth, ticketEndYear);

	assertThat(event.getDates().getDurationHour(), is(3));
	assertThat(event.getDates().getDurationMinute(), is(15));

	assertThat(event.getDates().getStartDate(), is(aDate(2013, 6, 10, 10, 25)));
	assertThat(event.getDates().getEndDate(), is(aDate(2013, 6, 10, 13, 40)));
    }

    @Test
    public void testEvent_RecurrenceSettings_daysInterval_AreUpdated() {
	final EventModel event = anEvent();
	recurrenceDaysSelection = new Integer[] { 1, 2 };
	eventModelUtil.refreshEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, recurrenceDaysSelection,
		recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);
	assertThat(event.getDates().getRecurrenceDaySelectionInterval().toArray(new Integer[recurrenceDaysSelection.length]),
		is(recurrenceDaysSelection));
    }

    @Test
    public void testEvent_RecurrenceSettingsAreUpdated() {
	final EventBuilder eb = new EventBuilder().withRecurrenceEndDate(2013, 5, 3);
	final EventModel event = eb.getEvent();
	event.getDates().setRecurrenceLabel(RecurrenceTypes.DAILY.getLabel());
	eventModelUtil.refreshEventValuesForUpdate(event, stateSelect, stateText, startDay, startMonth, startYear, recurrenceDaysSelection,
		recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);

	assertThat(event.getDates().getRecurrenceType(), is(RecurrenceTypes.DAILY));
	final DateTime recurrenceEndDate = event.getDates().getRecurrenceEndDate();
	assertThatDateHasHourAndMinutes(recurrenceEndDate, 23, 59);
	assertThat(recurrenceEndDate, is(aDate(2013, 6, 3, 23, 59)));
    }

    @Test
    public void testEventDetailsWithRecurrentDatesAreCorrect() throws ParseException {
	final String matchingUid = eventUid + "1";
	final RetrievalHit hitWithMatchingUid = new RetrievalHitBuilder(1).withDate(2014, 1, 15).getHit();
	final RetrievalHit pastHit = new RetrievalHitBuilder(3).withDate(2011, 4, 23).getHit();
	final RetrievalHit recurrentHit = new RetrievalHitBuilder(2).withDate(2014, 2, 10).getHit();
	final RetrievalHit anotherRecurrentHit = new RetrievalHitBuilder(4).withDate(2014, 4, 17).getHit();

	final List<RetrievalHit> searchResultHits = Lists.newArrayList(pastHit, recurrentHit, anotherRecurrentHit, hitWithMatchingUid);

	final EventModel event = eventModelUtil.getEventModelFromSearchResult(searchResultHits, mock(CalEvent.class), matchingUid);

	assertThatDateHasYearMonthDay(event.getDates().getStartDate(), 2014, 1, 15);
	assertThat(event.getDates().getRecurrenceDates().entrySet(), hasSize(2));

    }

    private void assertThatDateHasHourAndMinutes(final DateTime dt, final int hour, final int min) {
	assertThat(dt.getHourOfDay(), is(hour));
	assertThat(dt.getMinuteOfHour(), is(min));
    }

    private void assertThatFieldIsNotExposedAs(final String jsonString, final String fieldName) {
	assertThat(jsonString, not(containsString(fieldName)));
    }

    private void assertThatFieldIsExposedAs(final String jsonString, final String fieldName) {
	assertThat(jsonString, containsString(fieldName));
    }

    private class RetrievalHitBuilder {

	private final RetrievalHit hit;
	private final DateTimeFormatter searchFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

	RetrievalHitBuilder(final int val) {
	    hit = aRetrievalHitWithValues(val);
	}

	RetrievalHitBuilder withDate(final int year, final int month, final int day) throws ParseException {
	    final DateTime aDate = aDate(year, month, day, 10, 20);
	    hit.addField(SearchConstants.START_DATE, aDate.toString(searchFormatter));
	    hit.addField(SearchConstants.END_DATE, aDate.plusHours(2).toString(searchFormatter));
	    return this;
	}

	RetrievalHit getHit() {
	    return hit;
	}
    }

    private void assertThatDateHasYearMonthDay(final DateTime dt, final int year, final int month, final int day) {
	assertThat(dt.getYear(), is(year));
	assertThat(dt.getMonthOfYear(), is(month));
	assertThat(dt.getDayOfMonth(), is(day));
    }

    private RetrievalHit aRetrievalHitWithValues(final int addValue) {
	final RetrievalHit hit = new RetrievalHit();
	final String addValueText = String.valueOf(addValue);
	hit.addField(Field.ENTRY_CLASS_PK, String.valueOf(searchEventId + addValue));
	hit.addField(Field.USER_ID, String.valueOf(searchUserId + addValue));
	hit.addField(Field.GROUP_ID, String.valueOf(searchGroupId + addValue));
	hit.addField(Field.COMPANY_ID, String.valueOf(searchCompanyId + addValue));
	hit.addField(Field.UID, eventUid + addValueText);
	hit.addField(Field.TITLE, title + addValueText);
	return hit;
    }

    private class EventBuilder {

	private final EventModel event;

	EventBuilder() {
	    event = anEvent();
	}

	EventBuilder withStartDate(final int year, final int month, final int day, final int hour, final int minute) {
	    event.getDates().setStartYear(year);
	    event.getDates().setStartMonth(month);
	    event.getDates().setStartDay(day);
	    event.getDates().setStartHour(hour);
	    event.getDates().setStartMinute(minute);
	    return this;
	}

	EventBuilder withEndDate(final int hour, final int minute) {
	    event.getDates().setEndHour(hour);
	    event.getDates().setEndMinute(minute);
	    return this;
	}

	EventBuilder withRecurrenceEndDate(final int year, final int month, final int day) {
	    event.getDates().setRecurrenceEndYear(year);
	    event.getDates().setRecurrenceEndMonth(month);
	    event.getDates().setRecurrenceEndDay(day);
	    return this;
	}

	EventModel getEvent() {
	    return event;
	}

    }

    private EventModel anEvent() {
	return anEventWithValue(0);
    }

    private EventModel anEventWithValue(final int addValue) {
	final String addValueText = String.valueOf(addValue);
	final EventModel event = new EventModel();
	event.setEventId(searchEventId + addValue);
	event.setUserId(searchUserId + addValue);
	event.setGroupId(searchGroupId + addValue);
	event.setCompanyId(searchCompanyId + addValue);
	event.setEventUid(eventUid + addValueText);
	event.setTitle(title + addValueText);
	event.setDescription(description);
	event.getDates().setStartDate(aDate(2013, 3, 5, 10, 35));
	event.getDates().setEndDate(aDate(2013, 3, 5, 17, 35));
	return event;
    }

    private DateTime aDate(final int year, final int month, final int day, final int hour, final int minute) {
	return new DateTime(year, month, day, hour, minute, DateTimeZone.UTC);
    }

}
