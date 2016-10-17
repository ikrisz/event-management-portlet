package com.pfiks.intelligus.events.service.impl;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.exception.ValidationException;
import com.pfiks.intelligus.events.model.event.EventAttendee;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventOrganizer;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.service.impl.eventbrite.EventbriteAttendeeService;
import com.pfiks.intelligus.events.service.impl.eventbrite.EventbriteEventService;
import com.pfiks.intelligus.events.service.impl.eventbrite.EventbriteOrganizerService;
import com.pfiks.intelligus.events.service.impl.eventbrite.EventbritePaymentService;
import com.pfiks.intelligus.events.service.impl.eventbrite.EventbriteTicketService;
import com.pfiks.intelligus.events.service.impl.eventbrite.EventbriteVenueService;

@Component
public class EventbriteService {
    private static final Log LOG = LogFactoryUtil.getLog(EventbriteService.class);

    private static final String SESSION_ORGANIZERS = "organizersListInSession";
    private static final String SESSION_VENUES = "venuesListInSession";

    @Resource
    private EventbriteEventService eventbriteEventService;

    @Resource
    private EventbritePaymentService paymentService;

    @Resource
    private EventbriteOrganizerService organizerService;

    @Resource
    private EventbriteVenueService venueService;

    @Resource
    private EventbriteTicketService ticketService;

    @Resource
    private EventbriteAttendeeService attendeeService;

    @SuppressWarnings("unchecked")
    public Collection<EventOrganizer> getAllOrganizersForUser(final PortletRequest request, final String eventbriteAppKey, final String userKey) throws EventbriteException,
	    EventbriteErrorException {

	Collection<EventOrganizer> results = Lists.newArrayList();
	final Collection<EventOrganizer> attribute = (Collection<EventOrganizer>) request.getPortletSession().getAttribute(SESSION_ORGANIZERS);
	if (Validator.isNotNull(attribute) && !attribute.isEmpty()) {
	    results = attribute;
	} else {
	    results = organizerService.getAllOrganizersForUser(eventbriteAppKey, userKey);
	    request.getPortletSession().setAttribute(SESSION_ORGANIZERS, results);
	}
	return results;
    }

    @SuppressWarnings("unchecked")
    public Collection<EventVenue> getAllVenuesForUser(final PortletRequest request, final String eventbriteAppKey, final String userKey) throws EventbriteException,
	    EventbriteErrorException {

	Collection<EventVenue> results = Lists.newArrayList();

	final Collection<EventVenue> attribute = (Collection<EventVenue>) request.getPortletSession().getAttribute(SESSION_VENUES);
	if (Validator.isNotNull(attribute) && !attribute.isEmpty()) {
	    results = attribute;
	} else {
	    results = venueService.getAllVenuesForUser(eventbriteAppKey, userKey);
	    request.getPortletSession().setAttribute(SESSION_VENUES, results);
	}

	return results;
    }

    /**
     * Only sets event details and venue details. Does not set organizers or
     * tickets
     *
     * @param eventbriteAppKey
     * @param userKey
     * @return
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    public Collection<EventModel> getAllEventsForUser(final String eventbriteAppKey, final String userKey) throws EventbriteException, EventbriteErrorException {
	return eventbriteEventService.getAllEventsForUser(eventbriteAppKey, userKey);
    }

    /**
     * Sets event details and venue details as well as organizers and tickets
     * details
     *
     * @param eventbriteAppKey
     * @param userKey
     * @return
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    public EventModel getEventForUser(final String eventbriteAppKey, final String userKey, final String eventbriteEventId) throws EventbriteException, EventbriteErrorException {
	return eventbriteEventService.getEventForUser(eventbriteAppKey, userKey, eventbriteEventId);
    }

    /**
     * Only sets event details and venue details. Does not set organizers or
     * tickets
     *
     * @param eventbriteAppKey
     * @param userKey
     * @return
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    public EventModel getEventForSync(String eventbriteApplicationKey, String eventbriteUserKey, String eventBriteId) throws EventbriteException, EventbriteErrorException {
	return eventbriteEventService.getEventForSync(eventbriteApplicationKey, eventbriteUserKey, eventBriteId);
    }

    public void deleteEvent(final String eventbriteAppKey, final String userKey, final String eventbriteId) throws EventbriteException, EventbriteErrorException {
	if (StringUtils.isNotBlank(eventbriteId)) {
	    eventbriteEventService.updateEventStatus(eventbriteAppKey, userKey, eventbriteId, "deleted");
	}
    }

    /**
     * Will create a new event If no organizerId is selected, a new organizer
     * will be created If no venueId is selected, a new venue will be created
     * for the organizer New tickets will be created for the event
     * @throws ValidationException
     * @throws EventbriteException
     *
     * @throws EventbriteErrorException
     */
    public String createNewEvent(final ActionRequest request, final String eventbriteAppKey, final String userKey, final EventModel event) throws EventbriteException,
    ValidationException, EventbriteErrorException {

	manageOrganizer(request, eventbriteAppKey, userKey, event);
	manageVenue(request, eventbriteAppKey, userKey, event);

	String eventbriteId = "";
	try {
	    eventbriteId = eventbriteEventService.createNewEvent(eventbriteAppKey, userKey, event);
	    paymentService.createPaymentMethod(eventbriteAppKey, userKey, event, eventbriteId);
	    ticketService.updateOrCreateTicketsForEvent(eventbriteAppKey, userKey, event, eventbriteId);
	    eventbriteEventService.updateEventStatus(eventbriteAppKey, userKey, eventbriteId, "live");
	} catch (final EventbriteException e) {
	    rollBackEventbriteEvent(eventbriteAppKey, userKey, eventbriteId);
	    throw e;
	} catch (final ValidationException e) {
	    rollBackEventbriteEvent(eventbriteAppKey, userKey, eventbriteId);
	    throw e;
	} catch (final EventbriteErrorException e) {
	    rollBackEventbriteEvent(eventbriteAppKey, userKey, eventbriteId);
	    throw e;
	}
	return eventbriteId;
    }

    private void rollBackEventbriteEvent(final String eventbriteAppKey, final String userKey, String eventbriteId) {
	try {
	    deleteEvent(eventbriteAppKey, userKey, eventbriteId);
	} catch (final Exception e) {
	    LOG.warn("Exception while deleting draft event during rollback: ", e);
	}
    }

    public void updateEvent(final ActionRequest request, final String eventbriteAppKey, final String userKey, final EventModel event, Boolean updateEventbriteTickets)
	    throws EventbriteException, ValidationException, EventbriteErrorException {

	manageOrganizer(request, eventbriteAppKey, userKey, event);
	manageVenue(request, eventbriteAppKey, userKey, event);

	eventbriteEventService.updateEvent(eventbriteAppKey, userKey, event);
	if (updateEventbriteTickets) {
	    ticketService.updateOrCreateTicketsForEvent(eventbriteAppKey, userKey, event, event.getEventbrite().getEventbriteId());
	}
    }

    /*
     * Creates a new organizer if event.getOrganizer().getOrganizerId() is empty
     * Adds the new organizer to the list in session Returns organizerId
     */
    private void manageOrganizer(final ActionRequest request, final String eventbriteAppKey, final String userKey, final EventModel event) throws EventbriteException,
	    ValidationException, EventbriteErrorException {
	String organizerId = event.getOrganizer().getOrganizerId();
	if (StringUtils.isBlank(organizerId)) {
	    organizerId = organizerService.createNewOrganizer(eventbriteAppKey, userKey, event.getOrganizer());
	    addOrganizerInSession(request, event.getOrganizer());
	}
	event.getOrganizer().setOrganizerId(organizerId);
    }

    /*
     * If the event is NOT online Creates a new venue if
     * event.getVenue().getVenueId() is empty OR if it is different from the
     * previous selected id Adds the new venue to the list in session Returns
     * venueId
     */
    private void manageVenue(final ActionRequest request, final String eventbriteAppKey, final String userKey, final EventModel event) throws EventbriteException,
	    ValidationException, EventbriteErrorException {
	if (!event.getVenue().isOnline()) {
	    String selectedVenueId = event.getVenue().getVenueId();
	    if (StringUtils.isBlank(selectedVenueId)) {
		selectedVenueId = venueService.createNewVenue(eventbriteAppKey, userKey, event.getVenue(), event.getOrganizer().getOrganizerId());
		event.getVenue().setVenueId(selectedVenueId);
		addVenueInSession(request, event.getVenue());
	    }
	    event.setVenue(getVenue(request, selectedVenueId));
	}
    }

    @SuppressWarnings("unchecked")
    private EventVenue getVenue(final ActionRequest request, final String selectedVenueId) {
	final Collection<EventVenue> venues = (Collection<EventVenue>) request.getPortletSession().getAttribute(SESSION_VENUES);
	final Optional<EventVenue> tryFind = Iterables.tryFind(venues, new Predicate<EventVenue>() {

	    @Override
	    public boolean apply(final EventVenue arg0) {
		return arg0.getVenueId().equalsIgnoreCase(selectedVenueId);
	    }
	});
	return tryFind.get();
    }

    @SuppressWarnings("unchecked")
    private void addOrganizerInSession(final PortletRequest request, final EventOrganizer organizer) {
	Collection<EventOrganizer> results = (Collection<EventOrganizer>) request.getPortletSession().getAttribute(SESSION_ORGANIZERS);
	if (Validator.isNotNull(results) && !results.isEmpty()) {
	    results.add(organizer);
	} else {
	    results = Lists.newArrayList(organizer);
	    request.getPortletSession().setAttribute(SESSION_ORGANIZERS, results);
	}
    }

    @SuppressWarnings("unchecked")
    private void addVenueInSession(final PortletRequest request, final EventVenue venue) {
	Collection<EventVenue> results = (Collection<EventVenue>) request.getPortletSession().getAttribute(SESSION_VENUES);
	if (Validator.isNotNull(results) && !results.isEmpty()) {
	    results.add(venue);
	} else {
	    results = Lists.newArrayList(venue);
	    request.getPortletSession().setAttribute(SESSION_VENUES, results);
	}
    }

    public List<EventAttendee> getAllAttendeesForEvent(final long companyId, String eventbriteApplicationKey, String eventUserKey, String eventbriteId, EventModel eventModel)
	    throws EventbriteException, EventbriteErrorException {
	return attendeeService.getAllEventAttendees(companyId, eventbriteApplicationKey, eventUserKey, eventbriteId, eventModel);
    }

}
