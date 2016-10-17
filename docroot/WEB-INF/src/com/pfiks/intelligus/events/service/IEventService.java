package com.pfiks.intelligus.events.service;

import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.calendar.model.CalEvent;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.exception.EventNotFoundException;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.exception.ValidationException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventOrganizer;
import com.pfiks.intelligus.events.model.event.EventVenue;

/**
 * Main service class to manage events
 *
 * @author Ilenia Zedda
 */
public interface IEventService {

    /**
     * Retrieves events that will be shown in the list view mode.
     *
     * @param themeDisplay
     * @param configuration
	 * @param networkId
     * @param networkGroupids
     * @param searchText
     * @param performSearch
     * @param viewPastEvents
     * @param viewFutureEvents
     * @param currentPage
     * @return A map containing two objects with the following keys: 'EVENTS':
     *         list of EventModel results, 'TOTALS': total number of results
     *         found
     * @throws EventException
     */
	Map<String, Object> getEventsForListView(ThemeDisplay themeDisplay, EventPortletConfiguration configuration, final long networkId, Long[] networkGroupids,
			String searchText, Boolean performSearch, Boolean viewPastEvents, Boolean viewFutureEvents, int currentPage) throws EventException;

    /**
     * Retrieves all the events that are marked as 'featured'
     *
     * @param themeDisplay
     * @param configuration
	 * @param networkId
     * @param networkGroupids
     * @return Collection of EventModel results
     * @throws EventException
     */
	Collection<EventModel> getFeaturedEvents(ThemeDisplay themeDisplay, EventPortletConfiguration configuration, final long networkId, Long[] networkGroupids)
			throws EventException;

    /**
     * Retrieves all the events that fall between the specified startDate and
     * endDate filters, for the calendar view mode
     *
     * @param themeDisplay
     * @param configuration
	 * @param networkId
     * @param networkGroupids
     * @param startDateFilter
     * @param endDateFilter
     * @return Collection of EventModel results
     * @throws EventException
     */
	String getEventsForCalendarView(ThemeDisplay themeDisplay, EventPortletConfiguration configuration, final long networkId, Long[] networkGroupids, String startDateFilter,
			String endDateFilter) throws EventException;

    /**
     * Retrieves the events from EventBrite that are linked to the configured
     * User Key. Importable events are the ones that are live or started and not
     * yet ended.
     *
     * @param configuration
     * @return Collection of EventModel results that can be imported from
     *         EventBrite
     * @throws EventbriteException
     * @throws EventException
     * @throws EventbriteErrorException
     */
	Collection<EventModel> getImportableEventsFromEventbrite(long companyId, EventPortletConfiguration configuration) throws EventbriteException, EventException,
			EventbriteErrorException;

    /**
     * Removes the specified event from Liferay. If EventBrite is enabled and
     * the event is linked to an EventBrite event, the status of the EventBrite
     * event will be set to 'deleted' and he event is therefore removed from
     * EventBrite
     *
     * @param configuration
     * @param eventId
     * @throws EventException
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    void deleteEvent(long companyId, final EventPortletConfiguration configuration, Long eventId) throws EventException, EventbriteException, EventbriteErrorException;

    /**
     * Retrieves all the details for an event. If the event is linked to an
     * EventBrite event, additional fields are returned, such as Tickets
     * details, organizer name, venue id.
     *
     * If the event is linked to an EventBrite event, but the EventBrite event
     * has been cancelled or unpublished, the link will be removed.
     *
     * @param configuration
     * @param themeDisplay
     * @param eventId
     * @param eventUid
     * @return
     * @throws EventException
     * @throws EventbriteException
     * @throws EventNotFoundException
     * @throws EventbriteErrorException
     */
	EventModel getEventFullDetails(EventPortletConfiguration configuration, ThemeDisplay themeDisplay, Long eventId, String eventUid) throws EventException,
			EventbriteException, EventNotFoundException, EventbriteErrorException;

    /**
     * Retrieves only some of the event details
     *
     * If the event is linked to an EventBrite event, but the EventBrite event
     * has been cancelled or unpublished, the link will be removed.
     *
     * @param configuration
     * @param themeDisplay
     * @param eventId
     * @param eventUid
     * @return
     * @throws EventException
     * @throws EventNotFoundException
     */
    EventModel getEventShortDetails(EventPortletConfiguration configuration, ThemeDisplay themeDisplay, Long eventId, String eventUid) throws EventException,
    EventNotFoundException;

    /**
     * Creates a new event based on the information from the selected EventBrite
     * event
     *
     * @param configuration
     * @param eventbriteId
     * @param themeDisplay
     * @return true if the event has been successfully imported, false otherwise
     * @throws EventException
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    boolean importEventbriteEvent(EventPortletConfiguration configuration, String eventbriteId, ThemeDisplay themeDisplay) throws EventException, EventbriteException,
    EventbriteErrorException;

    /**
     * Creates a new event. If createEventbrite is true, the event will be
     * created in the EventBrite website and published.
     *
     * @param request
     * @param configuration
     * @param event
     * @param createEventbrite
     * @return CalEvent created
     * @throws EventException
     * @throws EventbriteException
     * @throws ValidationException
     * @throws EventbriteErrorException
     */
	CalEvent createEvent(ActionRequest request, EventPortletConfiguration configuration, EventModel event, boolean createEventbrite) throws EventException,
			EventbriteException, ValidationException, EventbriteErrorException;

    /**
     * Retrieves all the organizers for the configured User Key. The results are
     * saved in the portlet session, to avoid multiple calls.
     *
     * @param request
     * @param configuration
     * @return Collection of EventOrganizers
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    Collection<EventOrganizer> getAvailableOrganizers(PortletRequest request, EventPortletConfiguration configuration) throws EventbriteException, EventbriteErrorException;

    /**
     * Retrieves all the venues for the configured User Key. The results are
     * saved in the portlet session, to avoid multiple calls.
     *
     * @param request
     * @param configuration
     * @return Collection of EventVenues
     * @throws EventbriteException
     * @throws EventbriteErrorException
     */
    Collection<EventVenue> getAvailableLocations(PortletRequest request, EventPortletConfiguration configuration) throws EventbriteException, EventbriteErrorException;

    /**
     * Updates the specified event. If updateEventbrite is true, the event
     * details will be updated in the EventBrite website.
     *
     * @param request
     * @param configuration
     * @param event
     * @param updateEventbrite
     * @throws EventException
     * @throws EventbriteException
     * @throws ValidationException
     * @throws EventbriteErrorException
     */
    void updateEvent(ActionRequest request, EventPortletConfiguration configuration, EventModel event, Boolean updateEventbrite, Boolean updateEventbriteTickets)
	    throws EventException, EventbriteException, ValidationException, EventbriteErrorException;

    /**
     * Retrieves all the event details for the update page.
     *
     * @param configuration
     * @param eventId
     * @param timeZone 
     * @return
     * @throws EventException
     * @throws EventbriteException
     * @throws EventNotFoundException
     * @throws EventbriteErrorException
     */
	EventModel getEventForUpdate(EventPortletConfiguration configuration, Long eventId, TimeZone timeZone) throws EventException, EventbriteException, EventNotFoundException,
    EventbriteErrorException;

    CalEvent getCalEvent(long eventId) throws EventException, EventNotFoundException;

    /**
     * Sync a liferay event with details from the linked Eventbrite event, if
     * possible
     *
     * @param calEvent
     * @param eventBriteId
     * @return empty message if sync completed ok, otherwise error message
     * @throws EventbriteException
     * @throws EventbriteErrorException
     * @throws EventException
     */
    String syncEventFromEventbrite(CalEvent calEvent, String eventBriteId) throws EventbriteException, EventbriteErrorException, EventException;

}