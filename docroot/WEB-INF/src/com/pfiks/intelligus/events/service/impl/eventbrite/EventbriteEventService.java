package com.pfiks.intelligus.events.service.impl.eventbrite;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Resource;

import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.exception.ValidationException;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.utils.EventbriteModelUtils;

@Component
public class EventbriteEventService {

    private static final Log LOG = LogFactoryUtil.getLog(EventbriteEventService.class);

    @Resource
    private HttpRequestUtil eventbriteApis;

    @Resource
    private EventbriteModelUtils eventbriteModelUtils;

    private static final String USER_KEY_PARAM = "user_key";
    private static final String APP_KEY_PARAM = "app_key";

    private static final String GET_ALL_EVENTS = "https://www.eventbrite.com/json/user_list_events";
    private static final String GET_EVENT = "https://www.eventbrite.com/json/event_get";
    private static final String CREATE_EVENT = "https://www.eventbrite.com/json/event_new";
    private static final String UPDATE_EVENT = "https://www.eventbrite.com/json/event_update";

    /**
     * Updates the specified event details
     *
     * @throws ValidationException
     *             if errors with organizer or venue, or if the currency
     *             specified is not supported by eventbrite
     * @throws EventbriteErrorException
     */
    public void updateEvent(final String eventbriteAppKey, final String userKey, final EventModel event) throws EventbriteException, ValidationException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);

	params.addParam("id", event.getEventbrite().getEventbriteId());
	params.addParam("title", event.getTitle());
	params.addParam("organizer_id", event.getOrganizer().getOrganizerId());
	params.addParam("venue_id", event.getVenue().getVenueId());
	params.addParam("status", "live");
	params.addParamAlways("description", event.getDescription());
	params.addBooleanParam("privacy", event.isSecurityLevelPublic());

	params.addParam("timezone", getTimeZoneCode(event));
	params.addDateParam("start_date", event.getDates().getStartDate());
	params.addDateParam("end_date", event.getDates().getEndDate());

	final JSONObject jsonResponse = eventbriteApis.executeCall(UPDATE_EVENT, params);
	if (!eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    final String errorType = eventbriteApis.getErrorType(jsonResponse);
	    if (errorType.equalsIgnoreCase("Venue error") || errorType.equalsIgnoreCase("Organizer error")) {
		throw new ValidationException(errorType);
	    } else if (errorType.equalsIgnoreCase("Currency error") && eventbriteApis.errorMessageMatches(jsonResponse, "The given currency is invalid or not supported")) {
		throw new ValidationException("event.currency.eventbrite-invalid");
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to update event: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    private String getTimeZoneCode(final EventModel event) {
	final DateTimeZone timeZone = event.getDates().getTimeZone();
	String id = timeZone.getID();
	if (id.equals("UTC")) {
	    id = "Etc/GMT";
	}
	return id;
    }

    /**
     * Return the event for the specified id. Null if no event with the id is
     * found for the given user Sets the event details, plus the organizer,
     * tickets and venue
     *
     * @throws EventbriteErrorException
     */
    public EventModel getEventForUser(final String eventbriteAppKey, final String userKey, final String eventbriteEventId) throws EventbriteException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);
	params.addParam("id", eventbriteEventId);

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_EVENT, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getEventbriteDetails(jsonResponse, true, true, false, true);
	} else {
	    final String errorType = eventbriteApis.getErrorType(jsonResponse);
	    if (errorType.equalsIgnoreCase("Event error") || errorType.equalsIgnoreCase("Not Found") || errorType.equalsIgnoreCase("Authentication Error")
		    && eventbriteApis.errorMessageMatches(jsonResponse, "You are not authorized on this event")) {
		return null;
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve event: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    /**
     * Return the event for the specified id. Any event status accepted Null if
     * no event with the id is found for the given user Does not Set tickets or
     * Organizer details as they're not kept in sync. Only sets the event title,
     * description, start & end date, venue details, and if the event is
     * multiday, recurrent and the modified date.
     *
     * @throws EventbriteErrorException
     */
    public EventModel getEventForSync(final String eventbriteAppKey, final String userKey, final String eventbriteEventId) throws EventbriteException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);
	params.addParam("id", eventbriteEventId);

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_EVENT, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getEventbriteDetails(jsonResponse, false, false, true, false);
	} else {
	    final String errorType = eventbriteApis.getErrorType(jsonResponse);
	    if (errorType.equalsIgnoreCase("Event error") || errorType.equalsIgnoreCase("Not Found") || errorType.equalsIgnoreCase("Authentication Error")
		    && eventbriteApis.errorMessageMatches(jsonResponse, "You are not authorized on this event")) {
		return null;
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve event: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    /**
     * Returns all the events for the specified user. Empty list if no events
     * are found for the user.
     *
     * @throws EventbriteErrorException
     */
    public Collection<EventModel> getAllEventsForUser(final String eventbriteAppKey, final String userKey) throws EventbriteException, EventbriteErrorException {
	Collection<EventModel> results = Collections.emptyList();
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);
	// Only events that are live and not yet ended.
	params.addParam("event_statuses", "live,started");

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_ALL_EVENTS, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    results = eventbriteModelUtils.getEventbriteEventsList(jsonResponse);
	} else {
	    if (!eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Not Found")) {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve all events: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
	return results;
    }

    /**
     * Creates a new event for the given user
     * Event status is set to "Draft"
     *
     * @return the new eventbrite eventId
     * @throws ValidationException
     *             if errors with organizer or venue
     * @throws EventbriteErrorException
     */
    public String createNewEvent(final String eventbriteAppKey, final String userKey, final EventModel event) throws EventbriteException, ValidationException,
	    EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);

	params.addParam("title", event.getTitle());
	params.addParam("organizer_id", event.getOrganizer().getOrganizerId());
	params.addParam("venue_id", event.getVenue().getVenueId());
	params.addParam("status", "draft");
	params.addParamAlways("description", event.getDescription());
	params.addBooleanParam("privacy", event.isSecurityLevelPublic());
	params.addParam("currency", event.getEventbrite().getCurrency());

	params.addParam("timezone", getTimeZoneCode(event));
	params.addDateParam("start_date", event.getDates().getStartDate());
	params.addDateParam("end_date", event.getDates().getEndDate());

	final JSONObject jsonResponse = eventbriteApis.executeCall(CREATE_EVENT, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getIdFromCreateResponse(jsonResponse);
	} else {
	    final String errorType = eventbriteApis.getErrorType(jsonResponse);
	    if (errorType.equalsIgnoreCase("Venue error") || errorType.equalsIgnoreCase("Organizer error")) {
		throw new ValidationException(errorType);
	    } else if (errorType.equalsIgnoreCase("Currency error") && eventbriteApis.errorMessageMatches(jsonResponse, "The given currency is invalid or not supported")) {
		throw new ValidationException("eventbrite.currency-invalid");
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to create event: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    /**
     * Updates the event status
     *
     * @throws EventbriteErrorException
     */
    public void updateEventStatus(final String eventbriteAppKey, final String userKey, final String eventbriteId, final String eventStatus) throws EventbriteException,
	    EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);
	params.addParam("id", eventbriteId);
	params.addParam("status", eventStatus);
	final JSONObject jsonResponse = eventbriteApis.executeCall(UPDATE_EVENT, params);
	if (!eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
	    LOG.info("Unable to update event status to " + eventStatus + ". " + exceptionMessage);
	    throw new EventbriteErrorException("Unable to update event status in eventbrite: " + exceptionMessage);
	}
    }

}
