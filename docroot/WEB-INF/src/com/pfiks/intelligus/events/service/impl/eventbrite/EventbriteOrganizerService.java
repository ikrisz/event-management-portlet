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
import com.pfiks.intelligus.events.model.event.EventOrganizer;
import com.pfiks.intelligus.events.utils.EventbriteModelUtils;

@Component
public class EventbriteOrganizerService {

    private static final Log LOG = LogFactoryUtil.getLog(EventbriteOrganizerService.class);

    @Resource
    private HttpRequestUtil eventbriteApis;

    @Resource
    private EventbriteModelUtils eventbriteModelUtils;

    private static final String GET_ALL_ORGANIZERS = "https://www.eventbrite.com/json/user_list_organizers";
    private static final String GET_ORGANIZER = "https://www.eventbrite.com/json/organizer_get";
    private static final String CREATE_ORGANIZER = "https://www.eventbrite.com/json/organizer_new";

    /**
     * Creates a new organizer for the given user.
     * 
     * @return the new organizerId
     * @throws ValidationException
     *             is organizer name already exists
     * @throws EventbriteErrorException
     */
    public String createNewOrganizer(final String eventbriteAppKey, final String userKey, final EventOrganizer organizer) throws EventbriteException, ValidationException,
	    EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);
	params.addParam("name", organizer.getName());

	final JSONObject jsonResponse = eventbriteApis.executeCall(CREATE_ORGANIZER, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getIdFromCreateResponse(jsonResponse);
	} else {
	    if (eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Organizer error")
		    && eventbriteApis.errorMessageMatches(jsonResponse, "This organizer name already exists")) {
		throw new ValidationException("organizer.eventbrite.duplicate-invalid");
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to create organizer: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    /**
     * Return the organizer for the specified id. Null if no organizer with the
     * id is found for the given user
     * 
     * @throws EventbriteErrorException
     */
    public EventOrganizer getOrganizerForUser(final String eventbriteAppKey, final String userKey, final String organizerId) throws EventbriteException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);
	params.addParam("id", organizerId);

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_ORGANIZER, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getOrganizerDetails(jsonResponse);
	} else {
	    if (eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Organizer error")) {
		return null;
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve organizer: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

    /**
     * Returns all the organizers for the specified user. Emtpy list if no
     * organizers are found for the user.
     * 
     * @throws EventbriteErrorException
     */
    public Collection<EventOrganizer> getAllOrganizersForUser(final String eventbriteAppKey, final String userKey) throws EventbriteException, EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_ALL_ORGANIZERS, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    return eventbriteModelUtils.getOrganizersList(jsonResponse);
	} else {
	    if (eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Not Found")) {
		return Collections.emptyList();
	    } else {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve all organizers: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
    }

}
