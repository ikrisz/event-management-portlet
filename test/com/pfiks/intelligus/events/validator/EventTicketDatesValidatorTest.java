package com.pfiks.intelligus.events.validator;

import org.joda.time.DateTime;
import org.junit.Test;

import com.pfiks.intelligus.events.model.event.EventbriteDetails;

public class EventTicketDatesValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_eventTicketsStartDate_alwaysMadatory() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setTicketsStartDate(null);
	event.setEventbrite(eventbrite);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets.startDate-required");
    }

    @Test
    public void testThat_eventTicketsEndDate_alwaysMadatory() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setTicketsEndDate(null);
	event.setEventbrite(eventbrite);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets.endDate-required");
    }

    @Test
    public void testThat_eventTicketsEndDate_alwaysAfter_eventTicketsStartDate() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setTicketsEndDate(eventbrite.getTicketsStartDate().minusDays(5));
	event.setEventbrite(eventbrite);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets.endDate-invalid");
    }

    @Test
    public void testThat_eventTicketsStartDate_alwaysFuture() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	final DateTime now = DateTime.now().minusDays(5);
	eventbrite.setTicketsStartDate(now);
	event.setEventbrite(eventbrite);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets.startDate-invalid");
    }

    @Test
    public void testThat_eventTicketsStartDate_alwaysBefore_eventEndDate() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setTicketsStartDate(event.getDates().getEndDate().plusDays(5));
	event.setEventbrite(eventbrite);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets.startDate-invalid");
    }

    @Test
    public void testThat_eventTicketsEndDate_alwaysBefore_eventEndDate() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setTicketsEndDate(event.getDates().getEndDate().plusDays(5));
	event.setEventbrite(eventbrite);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets.endDate-invalid");
    }

    @Test
    public void testThat_eventTicketsDates_areValid() {
	validate();
	assertThat_allErrors_NeverContain("eventbrite.tickets.startDate-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets.endDate-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets.endDate-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets.startDate-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets.startDate-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets.endDate-invalid");
    }

}
