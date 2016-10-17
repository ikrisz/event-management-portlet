package com.pfiks.intelligus.events.service.impl.eventbrite;

import java.util.List;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.model.event.EventAttendee;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.utils.EventbriteModelUtils;

@Component
public class EventbriteAttendeeService {

    private static final Log LOG = LogFactoryUtil.getLog(EventbriteAttendeeService.class);

    @Resource
    private HttpRequestUtil eventbriteApis;

    @Resource
    private EventbriteModelUtils eventbriteModelUtils;

    private static final String GET_ALL_ATTENDEES = "https://www.eventbrite.com/json/event_list_attendees";

    public List<EventAttendee> getAllEventAttendees(final long companyId, final String eventbriteAppKey, final String userKey, final String eventbriteId, EventModel event)
	    throws EventbriteException, EventbriteErrorException {
	List<EventAttendee> results = Lists.newArrayList();
	final RequestParameters params = new RequestParameters();
	params.addParam("app_key", eventbriteAppKey);
	params.addParam("user_key", userKey);
	params.addParam("id", eventbriteId);
	params.addParam("status", "attending");
	params.addParam("count", "-1");

	final JSONObject jsonResponse = eventbriteApis.executeCall(GET_ALL_ATTENDEES, params);
	if (eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    results = eventbriteModelUtils.getAttendeesList(companyId, jsonResponse, event);
	} else {
	    if (!eventbriteApis.getErrorType(jsonResponse).equalsIgnoreCase("Not Found")) {
		final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
		LOG.info("Unable to retrieve all attendees: " + exceptionMessage.getErrorMessage());
		throw new EventbriteErrorException(exceptionMessage.getErrorLabel());
	    }
	}
	return results;
    }
}
