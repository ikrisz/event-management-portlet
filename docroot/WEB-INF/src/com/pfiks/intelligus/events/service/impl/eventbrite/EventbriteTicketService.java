package com.pfiks.intelligus.events.service.impl.eventbrite;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventTicket;
import com.pfiks.intelligus.events.utils.EventbriteModelUtils;

@Component
public class EventbriteTicketService {

    private static final Log LOG = LogFactoryUtil.getLog(EventbriteTicketService.class);

    @Resource
    private HttpRequestUtil eventbriteApis;

    @Resource
    private EventbriteModelUtils eventbriteModelUtils;

    private static final String CREATE_TICKET = "https://www.eventbrite.com/json/ticket_new";
    private static final String UPDATE_TICKET = "https://www.eventbrite.com/json/ticket_update";

    /**
     * Creates or updates the tickets for the event
     *
     * @param eventbriteAppKey
     * @param userKey
     * @param tickets
     * @param eventbriteId
     * @throws EventbriteException
     */
    public void updateOrCreateTicketsForEvent(final String eventbriteAppKey, final String userKey, final EventModel event, final String eventbriteId) throws EventbriteException,
    EventbriteErrorException {
	final List<EventTicket> tickets = event.getEventbrite().getTickets();
	if (tickets != null) {
	    final DateTime startDate = event.getEventbrite().getTicketsStartDate();
	    final DateTime endDate = event.getEventbrite().getTicketsEndDate();
	    for (final EventTicket ticket : tickets) {
		if (!ticket.isNullTicket()) {
		    if (StringUtils.isBlank(ticket.getTicketId())) {
			createNewTicket(eventbriteAppKey, userKey, ticket, startDate, endDate, eventbriteId);
		    } else {
			updateTicket(eventbriteAppKey, userKey, ticket, startDate, endDate);
		    }
		}
	    }
	}
    }

    /*
     * Creates a new ticket for the given event. Returns the new ticketId
     */
    private String createNewTicket(final String eventbriteAppKey, final String userKey, final EventTicket ticket, final DateTime startDate, final DateTime endDate,
	    final String eventId) throws EventbriteException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);

	params.addParam("event_id", eventId);
	params.addParam("name", ticket.getName());

	params.addParam("is_donation", getTicketType(ticket));
	params.addParam("price", getTicketPrice(ticket));
	final String quantityAvailable = ticket.getQuantityAvailable();
	params.addParam("quantity_available", quantityAvailable);
	params.addParam("max", quantityAvailable);
	params.addParam("min", "1");

	params.addDateParam("start_date", startDate);
	params.addDateParam("end_date", endDate);

	final JSONObject jsonResponse = eventbriteApis.executeCall(CREATE_TICKET, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getIdFromCreateResponse(jsonResponse);
	} else {
	    final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
	    LOG.info("Unable to create ricket: " + exceptionMessage.getErrorMessage());
	    throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	}
    }

    /*
     * Updates the ticket
     */
    private void updateTicket(final String eventbriteAppKey, final String userKey, final EventTicket ticket, final DateTime startDate, final DateTime endDate)
	    throws EventbriteException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);

	params.addParam("id", ticket.getTicketId());
	params.addParam("name", ticket.getName());

	params.addParam("is_donation", getTicketType(ticket));
	params.addParam("price", getTicketPrice(ticket));
	final String quantityAvailable = ticket.getQuantityAvailable();
	params.addParam("quantity_available", quantityAvailable);
	params.addParam("max", quantityAvailable);
	params.addParam("min", "1");

	params.addDateParam("start_date", startDate);
	params.addDateParam("end_date", endDate);

	final JSONObject jsonResponse = eventbriteApis.executeCall(UPDATE_TICKET, params);
	if (!eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
	    LOG.info("Unable to create ricket: " + exceptionMessage.getErrorMessage());
	    throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	}
    }

    private String getTicketPrice(final EventTicket ticket) {
	final String type = ticket.getType();
	String price = ticket.getPrice();
	if (isDonationTicket(type)) {
	    price = null;
	} else if (isFreeTicket(type)) {
	    price = "0.00";
	}
	return price;
    }

    private String getTicketType(final EventTicket ticket) {
	final String type = ticket.getType();
	String ticketType = "0";
	if (isDonationTicket(type)) {
	    ticketType = "1";
	}
	return ticketType;
    }

    private boolean isFreeTicket(final String ticketType) {
	return "free".equalsIgnoreCase(ticketType);
    }

    private boolean isDonationTicket(final String ticketType) {
	return "donation".equalsIgnoreCase(ticketType);
    }
}
