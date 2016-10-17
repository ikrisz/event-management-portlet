package com.pfiks.intelligus.events.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Before;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventDates;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventOrganizer;
import com.pfiks.intelligus.events.model.event.EventTicket;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.model.event.EventbriteDetails;
import com.pfiks.intelligus.events.model.event.PaymentMethod;

public class EventValidatorTest {

    protected final EventValidator validator = new EventValidator();
    protected Set<String> eventbriteEventErrors;
    protected Set<String> liferayEventErrors;

    protected EventModel event;
    protected final String ticketsToRemoveIndexes = "";

    @Before
    public void setUp() {
	event = aValidEventModel();
	event.setTitle("titleValue");
	liferayEventErrors = Sets.newHashSet();
	eventbriteEventErrors = Sets.newHashSet();
    }

    protected void validate() {
	liferayEventErrors = validator.validateEvent(event);
	eventbriteEventErrors = validator.validateEventbriteEvent(event, ticketsToRemoveIndexes);
    }

    protected void validate_onlyEventbrite() {
	eventbriteEventErrors = validator.validateEventbriteEvent(event, ticketsToRemoveIndexes);
    }

    protected void assertThat_EventbriteErrors_AlwaysContain(final String errorMessage) {
	assertThat(liferayEventErrors, not(hasItem(errorMessage)));
	assertThat(eventbriteEventErrors, hasItem(errorMessage));
    }

    protected void assertThat_EventbriteErrors_NeverContain(final String errorMessage) {
	assertThat(eventbriteEventErrors, not(hasItem(errorMessage)));
    }

    protected void assertThat_EventErrors_AlwaysContain(final String errorMessage) {
	assertThat(liferayEventErrors, hasItem(errorMessage));
	assertThat(eventbriteEventErrors, not(hasItem(errorMessage)));
    }

    protected void assertThat_EventErrors_NeverContain(final String errorMessage) {
	assertThat(liferayEventErrors, not(hasItem(errorMessage)));
    }

    protected void assertThat_allErrors_AlwaysContain(final String errorMessage) {
	assertThat(liferayEventErrors, hasItem(errorMessage));
	assertThat(eventbriteEventErrors, hasItem(errorMessage));
    }

    protected void assertThat_allErrors_NeverContain(final String errorMessage) {
	assertThat(liferayEventErrors, not(hasItem(errorMessage)));
	assertThat(eventbriteEventErrors, not(hasItem(errorMessage)));
    }

    private EventModel aValidEventModel() {
	final EventModel eventModel = new EventModel();
	eventModel.setTitle("anEventTitle");
	eventModel.setDescription("anEventDescription");
	eventModel.setFeaturedEvent(true);
	eventModel.setOrganizer(aNewOrganizer());
	eventModel.setEventbrite(anEventbriteDetailsModel());
	eventModel.setPublicEvent(true);
	eventModel.setVenue(anOnlineVenue());

	eventModel.setDates(someValidEventDates());

	return eventModel;
    }

    protected EventDates someValidEventDates() {
	final EventDates dates = new EventDates();
	dates.setAllDay(false);
	final DateTime startDate = aDate();
	dates.setStartDate(startDate);
	dates.setEndDate(startDate.plusDays(1).plusHours(2).plusMinutes(7));
	return dates;
    }

    protected EventDates someNotRecurrentEventDates() {
	final EventDates dates = new EventDates();
	dates.setAllDay(false);
	final DateTime startDate = aDate();
	dates.setStartDate(startDate);
	dates.setEndDate(startDate.plusDays(1).plusHours(2).plusMinutes(7));
	dates.setRecurrenceType(RecurrenceTypes.NONE);
	return dates;
    }

    protected EventDates aDailyRecurrentEventDates() {
	final EventDates dates = new EventDates();
	dates.setAllDay(false);
	final DateTime startDate = aDate();
	dates.setStartDate(startDate);
	dates.setEndDate(startDate.plusDays(1).plusHours(2).plusMinutes(7));

	dates.setRecurrenceEndDate(dates.getEndDate().plusMonths(4));

	dates.setRecurrenceType(RecurrenceTypes.DAILY);
	dates.setRecurrenceDayInterval(2);
	return dates;
    }

    protected EventDates aWeeklyRecurrentEventDates() {
	final EventDates dates = new EventDates();
	dates.setAllDay(false);
	final DateTime startDate = aDate();
	dates.setStartDate(startDate);
	dates.setEndDate(startDate.plusDays(1).plusHours(2).plusMinutes(7));

	dates.setRecurrenceEndDate(dates.getEndDate().plusMonths(4));

	dates.setRecurrenceType(RecurrenceTypes.WEEKLY);
	dates.setRecurrenceWeekInterval(2);
	dates.setRecurrenceDaySelectionInterval(Lists.newArrayList(1, 2));
	return dates;
    }

    protected EventDates aMonthlyRecurrentEventDates() {
	final EventDates dates = new EventDates();
	dates.setAllDay(false);
	final DateTime startDate = aDate();
	dates.setStartDate(startDate);
	dates.setEndDate(startDate.plusDays(1).plusHours(2).plusMinutes(7));

	dates.setRecurrenceEndDate(dates.getEndDate().plusMonths(4));

	dates.setRecurrenceType(RecurrenceTypes.MONTHLY);
	dates.setRecurrenceMonthInterval(2);
	dates.setRecurrenceDayInterval(2);
	return dates;
    }

    protected DateTime aDate() {
	return DateTime.now().plusHours(4);
    }

    protected EventVenue aNewVenue() {
	final EventVenue venue = new EventVenue();
	venue.setOnline(false);
	venue.setAddressLineOne("aVenueAddressOne");
	venue.setAddressLineTwo("aVenueAddressTwo");
	venue.setCity("aVenueCity");
	venue.setCountry("IT");
	venue.setName("aVenueName");
	venue.setRegionState("aVenueRegion");
	venue.setZip("aVenueZip");
	return venue;
    }

    protected EventVenue anExistingVenue() {
	final EventVenue venue = new EventVenue();
	venue.setVenueId("aVenueId");
	return venue;
    }

    protected EventVenue anOnlineVenue() {
	final EventVenue venue = new EventVenue();
	venue.setOnline(true);
	return venue;
    }

    protected EventbriteDetails anEventbriteDetailsModel() {
	final EventbriteDetails eventbriteDetails = new EventbriteDetails();
	eventbriteDetails.setCurrency("USD");
	eventbriteDetails.setTickets(someEventTickets());
	eventbriteDetails.setEventbriteId("anEventbriteId");
	eventbriteDetails.setPayment(aPaymentMethod());

	final DateTime startDate = DateTime.now().plusHours(3);
	eventbriteDetails.setTicketsStartDate(startDate);
	eventbriteDetails.setTicketsEndDate(startDate.plusDays(1).plusHours(2).plusMinutes(7));

	return eventbriteDetails;
    }

    protected PaymentMethod aPaymentMethod() {
	final PaymentMethod payment = new PaymentMethod();
	payment.setPaypalAccepted(true);
	payment.setPaypalEmail("test@email.com");

	payment.setCashAccepted(true);
	payment.setCashInstructions("someCashInstructions");

	payment.setCheckAccepted(true);
	payment.setCheckInstructions("someCheckInstructions");

	payment.setInvoiceAccepted(true);
	payment.setInvoiceInstructions("someInvoiceInstructions");

	return payment;
    }

    protected List<EventTicket> someEventTickets() {
	return Lists.newArrayList(aFreeTicket(), aDonationTicket(), aPaidTicket());
    }

    protected EventTicket aFreeTicket() {
	final EventTicket ticket = new EventTicket();
	ticket.setName("aFreeTicketName");
	ticket.setQuantityAvailable("10");
	ticket.setType("free");
	return ticket;
    }

    protected EventTicket aDonationTicket() {
	final EventTicket ticket = new EventTicket();
	ticket.setName("aDonationTicketName");
	ticket.setType("donation");
	return ticket;
    }

    protected EventTicket aPaidTicket() {
	final EventTicket ticket = new EventTicket();
	ticket.setName("aPaidTicketName");
	ticket.setQuantityAvailable("10");
	ticket.setPrice("25.30");
	ticket.setType("paid");
	return ticket;
    }

    protected EventOrganizer anExistingOrganizer() {
	final EventOrganizer organizer = new EventOrganizer();
	organizer.setOrganizerId("anOrganizerId");
	return organizer;
    }

    protected EventOrganizer aNewOrganizer() {
	final EventOrganizer organizer = new EventOrganizer();
	organizer.setName("anOrganizerName");
	return organizer;
    }

}
