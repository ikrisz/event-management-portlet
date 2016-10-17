package com.pfiks.intelligus.events.service.impl.eventbrite;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.exception.ValidationException;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.utils.EventbriteModelUtils;

@Component
public class EventbriteVenueService {

    private static final Log LOG = LogFactoryUtil.getLog(EventbriteVenueService.class);

    @Resource
    private HttpRequestUtil eventbriteApis;

    @Resource
    private EventbriteModelUtils eventbriteModelUtils;

    private static final String GET_ALL_VENUES = "https://www.eventbrite.com/json/user_list_venues";
    private static final String GET_VENUE = "https://www.eventbrite.com/json/venue_get";
    private static final String CREATE_VENUE = "https://www.eventbrite.com/json/venue_new";

    /**
     * Creates a new venue for the given user.
     * 
     * @return the new venueId
     * @throws ValidationException
     *             is venue name already exists for the same organizerId
     * @throws EventbriteErrorException
     */

    public String createNewVenue(final String eventbriteAppKey, final String userKey, final EventVenue venue, final String organizerId) throws EventbriteException,
	    ValidationException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);
	params.addParam("organizer_id", organizerId);
	params.addParam("name", venue.getName());
	params.addParam("address", venue.getAddressLineOne());
	params.addParamAlways("address_2", venue.getAddressLineTwo());
	params.addParam("city", venue.getCity());
	params.addParam("region", venue.getRegionState());
	params.addParam("postal_code", venue.getZip());
	params.addParam("country_code", venue.getCountry());

	final JSONObject jsonResponse = eventbriteApis.executeCall(CREATE_VENUE, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getIdFromCreateResponse(jsonResponse);
	} else {
	    if (eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Venue error")
		    && eventbriteApis.errorMessageMatches(jsonResponse, "The specified venue name already exists")) {
		throw new ValidationException("venue.eventbrite.duplicate-invalid");
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to create venue: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    /**
     * Return the venue for the specified id. Null if no venue with the id is
     * found for the given user
     * 
     * @throws EventbriteErrorException
     */

    public EventVenue getVenueForUser(final String eventbriteAppKey, final String userKey, final String venueId) throws EventbriteException, EventbriteErrorException {
	EventVenue result = null;
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);
	params.addParam("id", venueId);

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_VENUE, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    result = eventbriteModelUtils.getVenueDetails(jsonResponse);
	} else {
	    if (!eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Venue error")) {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve venue: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
	return result;
    }

    /**
     * Returns all the venues for the specified user. Emtpy list if no venues
     * are found for the user.
     * 
     * @throws EventbriteErrorException
     */

    public Collection<EventVenue> getAllVenuesForUser(final String eventbriteAppKey, final String userKey) throws EventbriteException, EventbriteErrorException {
	Collection<EventVenue> results = Collections.emptyList();
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_ALL_VENUES, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    results = eventbriteModelUtils.getVenuesList(jsonResponse);
	} else {
	    if (!eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Not Found")) {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve all venues: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
	return results;
    }

}
