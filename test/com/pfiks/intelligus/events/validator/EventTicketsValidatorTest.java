package com.pfiks.intelligus.events.validator;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.model.event.EventTicket;

public class EventTicketsValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_atLeastOneTicketIsRequired() {
	final List<EventTicket> noTickets = Lists.newArrayList();
	event.getEventbrite().setTickets(noTickets);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets-required");
    }

    @Test
    public void testThat_ticketName_alwaysRequired() {
	final EventTicket donationTicket = aDonationTicket();
	donationTicket.setName(StringPool.BLANK);
	final EventTicket freeTicket = aFreeTicket();
	freeTicket.setName(StringPool.BLANK);
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setName(StringPool.BLANK);
	final List<EventTicket> tickets = Lists.newArrayList(donationTicket, freeTicket, paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[1].name-required");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[2].name-required");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].name-required");
    }

    @Test
    public void testThat_ticketName_alwaysMaxLength_75() {
	final String ticketName = StringUtils.rightPad("aTicketName", 76);
	final EventTicket donationTicket = aDonationTicket();
	donationTicket.setName(ticketName);
	final EventTicket freeTicket = aFreeTicket();
	freeTicket.setName(ticketName);
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setName(ticketName);
	final List<EventTicket> tickets = Lists.newArrayList(donationTicket, freeTicket, paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[1].name-too-long");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[2].name-too-long");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].name-too-long");
    }

    @Test
    public void testThat_ticketPrice_forPaidTickets_alwaysRequired() {
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setPrice(StringPool.BLANK);
	final List<EventTicket> tickets = Lists.newArrayList(aDonationTicket(), aFreeTicket(), paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-required");

	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].price-required");
    }

    @Test
    public void testThat_ticketPrice_forPaidTickets_alwaysRequiredAndPositiveNumber() {
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setPrice("-36.36");
	final List<EventTicket> tickets = Lists.newArrayList(aDonationTicket(), aFreeTicket(), paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-invalid");

	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].price-invalid");
    }

    @Test
    public void testThat_ticketQuantity_forPaidAndFreeTickets_alwaysRequired() {
	final EventTicket donationTicket = aDonationTicket();
	donationTicket.setQuantityAvailable(StringPool.BLANK);
	final EventTicket freeTicket = aFreeTicket();
	freeTicket.setQuantityAvailable(StringPool.BLANK);
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setQuantityAvailable(StringPool.BLANK);
	final List<EventTicket> tickets = Lists.newArrayList(donationTicket, freeTicket, paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-required");

	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[2].quantityAvailable-required");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].quantityAvailable-required");
    }

    @Test
    public void testThat_ticketQuantity_forPaidAndFreeTickets_alwaysRequiredAndPositiveNumber() {
	final EventTicket donationTicket = aDonationTicket();
	donationTicket.setQuantityAvailable(StringPool.BLANK);
	final EventTicket freeTicket = aFreeTicket();
	freeTicket.setQuantityAvailable("-5");
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setQuantityAvailable("0");
	final List<EventTicket> tickets = Lists.newArrayList(donationTicket, freeTicket, paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-invalid");

	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[2].quantityAvailable-invalid");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].quantityAvailable-invalid");
    }

    @Test
    public void testThat_ticketQuantity_ifSet_alwaysPositiveNumber() {
	final EventTicket donationTicket = aDonationTicket();
	donationTicket.setQuantityAvailable("-6");
	final EventTicket freeTicket = aFreeTicket();
	freeTicket.setQuantityAvailable("-5");
	final EventTicket paidTicket = aPaidTicket();
	paidTicket.setQuantityAvailable("0");
	final List<EventTicket> tickets = Lists.newArrayList(donationTicket, freeTicket, paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[1].quantityAvailable-invalid");

	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[2].quantityAvailable-invalid");
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.tickets[3].quantityAvailable-invalid");
    }

    @Test
    public void testThat_eventFreeTickets_areValid() {
	final EventTicket freeTicket = aFreeTicket();
	freeTicket.setQuantityAvailable("5");
	final EventTicket anotherFreeTicket = aFreeTicket();
	anotherFreeTicket.setQuantityAvailable("1");
	final List<EventTicket> tickets = Lists.newArrayList(anotherFreeTicket, freeTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].name-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].name-too-long");

	assertThat_allErrors_NeverContain("eventbrite.tickets[2].quantityAvailable-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].quantityAvailable-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].name-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].name-too-long");
    }

    @Test
    public void testThat_eventDonationTickets_areValid() {
	final EventTicket donationTicket = aDonationTicket();
	final EventTicket anotherDonationTicket = aDonationTicket();
	anotherDonationTicket.setQuantityAvailable("1");
	final List<EventTicket> tickets = Lists.newArrayList(anotherDonationTicket, donationTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].name-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].name-too-long");

	assertThat_allErrors_NeverContain("eventbrite.tickets[2].quantityAvailable-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].quantityAvailable-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].name-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].name-too-long");
    }

    @Test
    public void testThat_eventPaidTickets_areValid() {
	final EventTicket paidTicket = aPaidTicket();
	final EventTicket anotherPaidTicket = aPaidTicket();
	anotherPaidTicket.setQuantityAvailable("1");
	anotherPaidTicket.setPrice("0.01");
	final List<EventTicket> tickets = Lists.newArrayList(anotherPaidTicket, paidTicket);
	event.getEventbrite().setTickets(tickets);
	validate();

	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].quantityAvailable-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].name-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[1].name-too-long");

	assertThat_allErrors_NeverContain("eventbrite.tickets[2].quantityAvailable-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].quantityAvailable-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].price-invalid");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].name-required");
	assertThat_allErrors_NeverContain("eventbrite.tickets[2].name-too-long");
    }

}
