package com.pfiks.intelligus.events.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.liferay.portlet.social.service.SocialActivityLocalServiceUtil;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.exception.EventNotFoundException;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.exception.ValidationException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.events.model.event.EventAttendee;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventOrganizer;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.model.event.EventbriteDetails;
import com.pfiks.intelligus.events.permissions.EventPermissionChecker;
import com.pfiks.intelligus.events.service.IEventService;
import com.pfiks.intelligus.events.social.CalEventActivityKeys;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;
import com.pfiks.intelligus.events.utils.ModelUtils;
import com.pfiks.intelligus.retrieval.IRetrievalResponse;

@Service
public class EventService implements IEventService {

    private static final Log LOG = LogFactoryUtil.getLog(EventService.class);

    @Resource
    private SearchService searchService;

    @Resource
    private LiferayEventService liferayService;

    @Resource
    private EventbriteService eventbriteService;

    @Resource
    private ModelUtils modelUtils;

    @Resource
    private ConfigurationUtils utils;

    @Resource
    private EventPermissionChecker permissionChecker;

    @Override
	public CalEvent getCalEvent(final long eventId) throws EventException, EventNotFoundException {
	return liferayService.getEvent(eventId);
    }

    @Override
	public Map<String, Object> getEventsForListView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupids, final String searchText, final Boolean performSearch, final Boolean viewPastEvents, final Boolean viewFutureEvents,
			final int currentPage) throws EventException {
	final Map<String, Object> results = Maps.newHashMap();
		final IRetrievalResponse searchResults = searchService.retrieveEventsForListView(themeDisplay, configuration, networkId, networkGroupids, searchText,
				performSearch, viewPastEvents, viewFutureEvents, currentPage);
	final Collection<EventModel> eventResults = modelUtils.getEventModelsFromSearchResults(searchResults);

	setHasEditPermissionOnEvents(themeDisplay, configuration, eventResults);

	results.put("EVENTS", eventResults);
	results.put("TOTALS", searchResults.numOverallHits());
	return results;
    }

    @Override
	public Collection<EventModel> getFeaturedEvents(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupids) throws EventException {
		final IRetrievalResponse searchResults = searchService.retrieveFeaturedEvents(themeDisplay, configuration, networkId, networkGroupids);
	final Collection<EventModel> eventResults = modelUtils.getEventModelsFromSearchResults(searchResults);
	setHasEditPermissionOnEvents(themeDisplay, configuration, eventResults);
	return eventResults;
    }

    private void setHasEditPermissionOnEvents(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final Collection<EventModel> eventResults) {
	for (final EventModel eventModel : eventResults) {
	    final boolean canEventBeModified = permissionChecker.canEventBeModified(themeDisplay, eventModel, configuration);
	    final boolean hasEditPermission = canEventBeModified && permissionChecker.hasEditPermission(themeDisplay, eventModel);
	    eventModel.setHasEditPermission(hasEditPermission);
	}
    }

    @Override
	public String getEventsForCalendarView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId, final Long[] networkGroupids,
			final String startDateFilter, final String endDateFilter) throws EventException {
		final IRetrievalResponse searchResults = searchService.retrieveEventsForCalendarView(themeDisplay, configuration, networkId, networkGroupids, startDateFilter,
				endDateFilter);
	final Collection<EventModel> events = modelUtils.getEventModelsFromSearchResults(searchResults);
	return modelUtils.getEventsJsonForCalendarView(events);
    }

    @Override
	public Collection<EventModel> getImportableEventsFromEventbrite(final long companyId, final EventPortletConfiguration configuration) throws EventbriteException,
			EventException, EventbriteErrorException {
	if (utils.isEventbriteEnabled(companyId)) {
	    final Collection<EventModel> allEvents = eventbriteService.getAllEventsForUser(utils.getEventbriteApplicationKey(companyId),
		    utils.getEventbriteUserKey(companyId, configuration));
	    return filterEventsNotYetImported(companyId, allEvents);
	} else {
	    return Collections.emptyList();
	}
    }

    @Override
	public void deleteEvent(final long companyId, final EventPortletConfiguration configuration, final Long eventId) throws EventException, EventbriteException,
			EventbriteErrorException {
	if (utils.isEventbriteEnabled(companyId)) {
	    final String eventbriteId = getEventbriteIdFromLiferayEventId(companyId, eventId);
	    final String eventUserKey = getEventbriteUserKey(companyId, eventId, configuration);
	    eventbriteService.deleteEvent(utils.getEventbriteApplicationKey(companyId), eventUserKey, eventbriteId);
	}
	liferayService.deleteEvent(eventId);

	try {
	    SocialActivityLocalServiceUtil.deleteActivities(CalEvent.class.getName(), eventId);
		} catch (final NestableException e) {
	    LOG.error(e);
	}
    }

    @Override
    public EventModel getEventForUpdate(final EventPortletConfiguration configuration, final Long eventId, TimeZone timezone) throws EventException, EventbriteException, EventNotFoundException,
	    EventbriteErrorException {
	final CalEvent calEvent = liferayService.getEvent(eventId);
	EventModel event = modelUtils.getLiferayEventModelForUpdate(calEvent, timezone);
	event = addEventbriteFields(calEvent.getCompanyId(), configuration, event, false);
	return event;
    }

    @Override
    public EventModel getEventFullDetails(final EventPortletConfiguration configuration, final ThemeDisplay themeDisplay, final Long eventId, final String eventUid)
	    throws EventException, EventbriteException, EventNotFoundException, EventbriteErrorException {
	final IRetrievalResponse searchResult = searchService.retrieveEvent(themeDisplay, configuration, eventId);
	final CalEvent calEvent = liferayService.getEvent(eventId);
	EventModel event = modelUtils.getEventModelFromSearchResult(searchResult.getHits(), calEvent, eventUid);
	event = addEventbriteFields(themeDisplay.getCompanyId(), configuration, event, true);
	return event;
    }

    @Override
    public EventModel getEventShortDetails(final EventPortletConfiguration configuration, final ThemeDisplay themeDisplay, final Long eventId, final String eventUid)
	    throws EventException, EventNotFoundException {
	final IRetrievalResponse searchResult = searchService.retrieveEvent(themeDisplay, configuration, eventId);
	final CalEvent calEvent = liferayService.getEvent(eventId);
	final EventModel event = modelUtils.getEventModelFromSearchResult(searchResult.getHits(), calEvent, eventUid);

	final String eventbriteId = getEventbriteId(event);
	if (StringUtils.isNotBlank(eventbriteId)) {
	    final String eventUserKey = getEventbriteUserKey(event, configuration);
	    final EventbriteDetails eventbrite = new EventbriteDetails();
	    eventbrite.setEventbriteId(eventbriteId);
	    eventbrite.setEventbriteUserApiKey(eventUserKey);
	    event.setEventbrite(eventbrite);
	}
	return event;
    }

    @Override
    public boolean importEventbriteEvent(final EventPortletConfiguration configuration, final String eventbriteId, final ThemeDisplay themeDisplay) throws EventException,
	    EventbriteException, EventbriteErrorException {
	boolean result = true;
		final long companyId = themeDisplay.getCompanyId();
	if (utils.isEventbriteEnabled(companyId)) {
	    final String eventbriteUserKey = utils.getEventbriteUserKey(companyId, configuration);
	    final EventModel event = eventbriteService.getEventForUser(utils.getEventbriteApplicationKey(companyId), eventbriteUserKey, eventbriteId);
	    if (Validator.isNull(event)) {
		result = false;
	    } else {
		event.getEventbrite().setEventbriteUserApiKey(eventbriteUserKey);
		liferayService.createEventFromEventbriteDetails(event, themeDisplay);
	    }
	}
	return result;
    }

    @Override
    public CalEvent createEvent(final ActionRequest request, final EventPortletConfiguration configuration, final EventModel event, final boolean createInEventbrite)
	    throws EventException, EventbriteException, ValidationException, EventbriteErrorException {
	clearEventDefaultValues(event);
		final long companyId = utils.getCompanyId(request);
	if (createInEventbrite && utils.isEventbriteEnabled(companyId)) {
	    /* Uses api user key from portlet configuration */
	    final String eventbriteUserKey = utils.getEventbriteUserKey(companyId, configuration);
	    final String eventbriteId = eventbriteService.createNewEvent(request, utils.getEventbriteApplicationKey(companyId), eventbriteUserKey, event);
	    event.getEventbrite().setEventbriteId(eventbriteId);
	    event.getEventbrite().setEventbriteUserApiKey(eventbriteUserKey);
	}
		final CalEvent createdEvent = liferayService.createEvent(request, event);
	createEventSocialActivity(createdEvent, CalEventActivityKeys.ADD_EVENT);
	return createdEvent;
    }

    @Override
    public void updateEvent(final ActionRequest request, final EventPortletConfiguration configuration, final EventModel event, final Boolean createInEventbrite,
			final Boolean updateEventbriteTickets) throws EventException, EventbriteException, ValidationException, EventbriteErrorException {
	clearEventDefaultValues(event);
		final long companyId = utils.getCompanyId(request);
	if (createInEventbrite && utils.isEventbriteEnabled(companyId) && StringUtils.isNotBlank(event.getEventbrite().getEventbriteId())) {
	    eventbriteService.updateEvent(request, utils.getEventbriteApplicationKey(companyId), utils.getEventbriteUserKey(companyId, configuration), event,
		    updateEventbriteTickets);
	}
		final CalEvent updatedEvent = liferayService.updateEvent(request, event);
	createEventSocialActivity(updatedEvent, CalEventActivityKeys.UPDATE_EVENT);
    }

	private void createEventSocialActivity(final CalEvent event, final int activityKey) {
	try {
	    // N.B. Activity update has to be outside of transaction otherwise
	    // the notifier may think it doesn't exist yet
	    final JSONObject extraDataJSONObject = JSONFactoryUtil.createJSONObject();
	    extraDataJSONObject.put("title", event.getTitle());
	    SocialActivityLocalServiceUtil.addActivity(event.getUserId(), event.getGroupId(), CalEvent.class.getName(), event.getEventId(), activityKey,
		    extraDataJSONObject.toString(), 0L);
		} catch (final NestableException e) {
	    LOG.error(e);
	}
    }

    private void clearEventDefaultValues(final EventModel event) {
	// Default country
	if (event.getVenue().isOnline()) {
	    event.getVenue().setCountry(StringPool.BLANK);
	}

	// All day if multiday is selected
	if (event.getDates().isMultiDay()) {
	    event.getDates().setAllDay(false);
	}
    }

    @Override
    public Collection<EventOrganizer> getAvailableOrganizers(final PortletRequest request, final EventPortletConfiguration configuration) throws EventbriteException,
	    EventbriteErrorException {
		final long companyId = utils.getCompanyId(request);
	if (utils.isEventbriteEnabled(companyId)) {
			return eventbriteService.getAllOrganizersForUser(request, utils.getEventbriteApplicationKey(companyId),
					utils.getEventbriteUserKey(companyId, configuration));
	}
	return Collections.emptyList();
    }

    @Override
    public Collection<EventVenue> getAvailableLocations(final PortletRequest request, final EventPortletConfiguration configuration) throws EventbriteException,
	    EventbriteErrorException {
		final long companyId = utils.getCompanyId(request);
	if (utils.isEventbriteEnabled(companyId)) {
	    return eventbriteService.getAllVenuesForUser(request, utils.getEventbriteApplicationKey(companyId), utils.getEventbriteUserKey(companyId, configuration));
	}
	return Collections.emptyList();
    }

	private Collection<EventModel> filterEventsNotYetImported(final long companyId, final Collection<EventModel> events) throws EventException {
	final List<String> importedIds = liferayService.getIdsOfImportedEventbriteEvents(companyId);
	final Collection<EventModel> notImportedEvents = Collections2.filter(events, new Predicate<EventModel>() {

	    @Override
	    public boolean apply(final EventModel event) {
		return !importedIds.contains(event.getEventbrite().getEventbriteId());
	    }
	});
	return notImportedEvents;
    }

    private String getEventbriteId(final EventModel model) {
	String result = model.getEventbrite().getEventbriteId();
	if (StringUtils.isBlank(result) && model.getCalEvent() != null) {
	    result = (String) model.getCalEvent().getExpandoBridge().getAttribute(EventExpandoConstants.EVENTBRITE_ID);
	}
	if (StringUtils.isBlank(result)) {
	    result = getEventbriteIdFromLiferayEventId(model.getCompanyId(), model.getEventId());
	}
	return result;
    }

    /*
     * Sets Organizer, Venue, Tickets
     */
	private EventModel addEventbriteFields(final long companyId, final EventPortletConfiguration configuration, EventModel event, final boolean includeAttendees)
			throws EventbriteException, EventbriteErrorException {
	if (utils.isEventbriteEnabled(companyId)) {
	    final String eventbriteId = getEventbriteId(event);
	    if (StringUtils.isNotBlank(eventbriteId)) {
		final String eventUserKey = getEventbriteUserKey(event, configuration);
		final String eventbriteApplicationKey = utils.getEventbriteApplicationKey(companyId);
		final EventModel eventbriteEvent = eventbriteService.getEventForUser(eventbriteApplicationKey, eventUserKey, eventbriteId);
		if (Validator.isNull(eventbriteEvent)) {
		    liferayService.removeEventbriteFieldsFromEvent(companyId, event.getEventId());
		} else {
		    event = modelUtils.updateModelWithEventbriteDetails(event, eventbriteEvent);
		    if (includeAttendees) {
						final List<EventAttendee> allAttendeesForEvent = eventbriteService.getAllAttendeesForEvent(companyId, eventbriteApplicationKey,
								eventUserKey, eventbriteId, event);
			event.getEventbrite().setAttendees(allAttendeesForEvent);
		    }
		}
	    }
	}
	return event;
    }

    private String getEventbriteUserKey(final EventModel model, final EventPortletConfiguration configuration) {
	String result = model.getEventbrite().getEventbriteUserApiKey();
		final long companyId = model.getCompanyId();
	if (StringUtils.isBlank(result) && model.getCalEvent() != null) {
	    result = (String) model.getCalEvent().getExpandoBridge().getAttribute(EventExpandoConstants.EVENTBRITE_USER_API);
	}
	if (StringUtils.isBlank(result)) {
	    result = getEventbriteUserKeyFromLiferayEventId(companyId, model.getEventId());
	}
	if (StringUtils.isBlank(result)) {
	    result = utils.getEventbriteUserKey(companyId, configuration);
	}
	return result;
    }

    private String getEventbriteUserKey(final CalEvent event) {
	String result = (String) event.getExpandoBridge().getAttribute(EventExpandoConstants.EVENTBRITE_USER_API);
		final long companyId = event.getCompanyId();
	if (StringUtils.isBlank(result)) {
	    result = getEventbriteUserKeyFromLiferayEventId(companyId, event.getEventId());
	}
	if (StringUtils.isBlank(result)) {
	    result = utils.getEventbriteUserKey(companyId, null);
	}
	return result;
    }

	private String getEventbriteUserKey(final long companyId, final long liferayEventId, final EventPortletConfiguration configuration) {
	String result = getEventbriteUserKeyFromLiferayEventId(companyId, liferayEventId);
	if (StringUtils.isBlank(result)) {
	    result = utils.getEventbriteUserKey(companyId, configuration);
	}
	return result;
    }

    @SuppressWarnings("unchecked")
	private String getEventbriteIdFromLiferayEventId(final long companyId, final Long liferayEventId) {
	if (utils.isEventbriteEnabled(companyId)) {
	    try {
		final DynamicQuery expandoQuery = utils.getQueryForEventbriteIdExpandoValueData(companyId);
		expandoQuery.add(RestrictionsFactoryUtil.eq("classPK", liferayEventId));
		final List<String> dynamicQuery = ExpandoValueLocalServiceUtil.dynamicQuery(expandoQuery);
		if (dynamicQuery != null && !dynamicQuery.isEmpty()) {
		    return dynamicQuery.get(0);
		}
	    } catch (final NestableException e) {
		LOG.warn("Exception retrieving imported eventbriteId: " + e.getMessage());
	    }
	}
	return StringPool.BLANK;
    }

    @SuppressWarnings("unchecked")
	private String getEventbriteUserKeyFromLiferayEventId(final long companyId, final Long liferayEventId) {
	if (utils.isEventbriteEnabled(companyId)) {
	    try {
		final DynamicQuery expandoQuery = utils.getQueryForEventbriteUserApiKeyExpandoValueData(companyId);
		expandoQuery.add(RestrictionsFactoryUtil.eq("classPK", liferayEventId));
		final List<String> dynamicQuery = ExpandoValueLocalServiceUtil.dynamicQuery(expandoQuery);
		if (dynamicQuery != null && !dynamicQuery.isEmpty()) {
		    return dynamicQuery.get(0);
		}
	    } catch (final NestableException e) {
		LOG.warn("Exception retrieving imported eventbrite userApiKey: " + e.getMessage());
	    }
	}
	return StringPool.BLANK;
    }

    @Override
	public String syncEventFromEventbrite(final CalEvent calEvent, final String eventBriteId) throws EventbriteException, EventbriteErrorException, EventException {
	LOG.info("Synchonizing  calEvent with eventId: " + calEvent.getEventId() + " and eventbriteId: " + eventBriteId);
	String errorMessage = StringPool.BLANK;
	final String eventbriteUserKey = getEventbriteUserKey(calEvent);
	final EventModel event = eventbriteService.getEventForSync(utils.getEventbriteApplicationKey(calEvent.getCompanyId()), eventbriteUserKey, eventBriteId);
	if (Validator.isNull(event)) {
	    LOG.info("No eventbrite event found");
	    liferayService.deleteEvent(calEvent.getEventId());
	    errorMessage = "Eventbrite event not found; Linked event in Liferay has been removed";
	} else if (eventbriteEventHasBeenRemoved(event.getEventbrite().getStatus())) {
	    liferayService.deleteEvent(calEvent.getEventId());
	    errorMessage = "Eventbrite event had status= " + event.getEventbrite().getStatus() + "; Linked event in Liferay has been removed.";
	} else if (event.getEventbrite().isRecurrent()) {
	    LOG.info("Unable to sync. Eventbrite event is recurrent");
	    errorMessage = "Unbale to sync recurrent event; Liferay event is now out of date.";
	} else if (event.getEventbrite().isMultiday()) {
	    LOG.info("Unable to sync. Eventbrite event is multiday");
	    errorMessage = "Unbale to sync multiday event; Liferay event is now out of date.";
	} else {
	    if (eventWasLastModifiedInEventbrite(calEvent, event)) {
		event.getEventbrite().setEventbriteUserApiKey(eventbriteUserKey);
		liferayService.syncEventFromEventbrite(calEvent, event);
		LOG.info("Event correctly synchronized");
	    } else {
		LOG.info("No changes to sync");
	    }
	}
	return errorMessage;
    }

	private boolean eventbriteEventHasBeenRemoved(final String status) {
	return status.equalsIgnoreCase("canceled") || status.equalsIgnoreCase("unpublished") || status.equalsIgnoreCase("draft") || status.equalsIgnoreCase("deleted");
    }

	private boolean eventWasLastModifiedInEventbrite(final CalEvent calEvent, final EventModel event) {
	// Buffer of 5 minutes for when an event is updated in liferay and then
	// sync to eventbrite
	final DateTime liferayModifiedDate = new DateTime(calEvent.getModifiedDate()).plusMinutes(5);
	final DateTime eventbriteModifiedDate = event.getEventbrite().getModifiedDate();
	return eventbriteModifiedDate.isAfter(liferayModifiedDate);
    }

}