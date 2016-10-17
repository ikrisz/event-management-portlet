package com.pfiks.intelligus.events.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.WindowState;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.liferay.portal.kernel.configuration.Filter;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletURL;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.model.PortletApp;
import com.liferay.portal.service.PortletLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;
import com.liferay.portlet.messageboards.model.MBMessage;
import com.liferay.portlet.messageboards.service.MBMessageServiceUtil;
import com.liferay.portlet.social.service.SocialActivityLocalServiceUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.exception.EventNotFoundException;
import com.pfiks.intelligus.events.exception.EventPermissionException;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.exception.ValidationException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.events.model.Pagination;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.permissions.EventPermissionChecker;
import com.pfiks.intelligus.events.service.IEventService;
import com.pfiks.intelligus.events.service.INotificationService;
import com.pfiks.intelligus.events.social.CalEventActivityKeys;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;
import com.pfiks.intelligus.events.utils.ModelUtils;
import com.pfiks.intelligus.events.validator.EventValidator;
import com.pfiks.intelligus.networks.portal.model.NetworkGroup;
import com.pfiks.intelligus.networks.portal.service.NetworkGroupLocalServiceUtil;
import com.pfiks.intelligus.networks.portal.service.NetworkLocalServiceUtil;
import com.pfiks.intelligus.util.ContentSecurityLevel;

/**
 * Main controller class
 *
 * @author Ilenia Zedda
 *
 */
@Controller
@RequestMapping(value = "VIEW")
@SessionAttributes(types = EventModel.class, value = "event")
public class EventsController {

	private static final Log LOG = LogFactoryUtil.getLog(EventsController.class);

	@Resource
	private EventPermissionChecker permissionChecker;

	@Resource
	private IEventService eventService;

	@Resource
	private ConfigurationUtils configUtils;

	@Resource
	private ModelUtils modelUtils;

	@Resource
	private EventValidator validator;

	@Resource
	private INotificationService notificationService;

	/* Renders the default view */
	@RenderMapping
	public String view(final RenderRequest request, final RenderResponse response, final Model model, final SessionStatus sessionStatus,
			@RequestParam(value = "viewMode", required = false, defaultValue = "") String viewMode,
			@RequestParam(value = "searchText", required = false, defaultValue = "") final String searchText,
			@RequestParam(value = "currentPage", required = false, defaultValue = "1") final int currentPage,
			@RequestParam(value = "performSearch", required = false, defaultValue = "false") final Boolean performSearch,
			@RequestParam(value = "viewPastEvents", required = false, defaultValue = "false") final Boolean viewPastEvents,
			@RequestParam(value = "viewFutureEvents", required = false, defaultValue = "true") final Boolean viewFutureEvents) throws EventException,
			EventbriteException, NestableException {

		clearSessionObject(sessionStatus);

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		final EventPortletConfiguration configuration = getConfiguration(request);
		final boolean isGuestGroup = configUtils.isGuestGroup(themeDisplay);
		if (isGuestGroup && configuration.getConfiguredScope().equals(ConfigurationConstants.SCOPE_GROUP)) {
			// Invalid portlet configuration.
			model.addAttribute("invalidConfiguration", true);
		} else {
			if (StringUtils.isBlank(viewMode)) {
				viewMode = configuration.getConfiguredViewMode();
			}
			final boolean isListView = viewMode.equalsIgnoreCase(ConfigurationConstants.VIEW_LIST);
			if (isListView) {
				final Long[] networkGroupids = getNetworkGroupIds(isGuestGroup, request);
				final long networkId = getCurrentNetworkId(isGuestGroup, request);
				if (configuration.getIncludeFeaturedEvents()) {
					model.addAttribute("featuredEvents", eventService.getFeaturedEvents(themeDisplay, configuration, networkId, networkGroupids));
				}

				final Map<String, Object> searchResults = eventService.getEventsForListView(themeDisplay, configuration, networkId, networkGroupids, searchText,
						performSearch, viewPastEvents, viewFutureEvents, currentPage);
				model.addAttribute("events", searchResults.get("EVENTS"));
				model.addAttribute("pagination", new Pagination(currentPage, (Integer) searchResults.get("TOTALS"), configuration.getMaxEventsToShow()));
				model.addAttribute("viewPastEvents", viewPastEvents);
				model.addAttribute("viewFutureEvents", viewFutureEvents);
				model.addAttribute("performSearch", performSearch);
				model.addAttribute("searchText", searchText);
			}
			addCommonViewModelAttributes(model, themeDisplay);
			model.addAttribute("hasAddPermission", permissionChecker.hasAddPermission(themeDisplay));
			model.addAttribute("hasAddEventbritePermission", permissionChecker.hasAddEventbritePermission(themeDisplay));
			model.addAttribute("customEventbriteKey", configuration.getCustomEventbriteUserKey());
			model.addAttribute("isGuestGroup", isGuestGroup);
			model.addAttribute("liferayCreationDisabled", configuration.getLiferayCreationDisabled());
			model.addAttribute("isMonthView", !isListView);
			clearSessionObject(sessionStatus);
		}
		return "view";
	}

	/* Retrieves a list of events for the calendar view */
	@ResourceMapping(value = "calendarListEvents")
	public void getCalendarListEvents(final ResourceRequest request, final ResourceResponse response, @RequestParam(value = "startDateFilter") final String startDateFilter,
			@RequestParam(value = "endDateFilter") final String endDateFilter) throws EventException, IOException, NestableException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		final boolean isGuestGroup = configUtils.isGuestGroup(themeDisplay);
		final long networkId = getCurrentNetworkId(isGuestGroup, request);
		final Long[] networkGroupids = getNetworkGroupIds(isGuestGroup, request);

		final String searchResult = eventService.getEventsForCalendarView(themeDisplay, getConfiguration(request), networkId, networkGroupids, startDateFilter,
				endDateFilter);
		response.getWriter().write(searchResult);
	}

	/* Actions that performs a text-based search */
	@ActionMapping(params = "action=searchEvent")
	public void searchEventAction(final ActionRequest request, final ActionResponse response,
			@RequestParam(value = "searchText", required = false, defaultValue = "") final String searchText) {

		if (StringUtils.isNotBlank(searchText)) {
			response.setRenderParameter("performSearch", "true");
			response.setRenderParameter("searchText", searchText);
		}
		response.setRenderParameter("currentPage", "1");
		response.setRenderParameter("viewMode", ConfigurationConstants.VIEW_LIST);
	}

	/* Renders the page that lists the importable events from eventbrite */
	@RenderMapping(params = { "view=importableEventbriteEvents" })
	public String importableEventbriteEvents(final RenderRequest request, final Model model) throws EventbriteException, EventException, EventbriteErrorException {
		final Collection<EventModel> importableEvents = eventService.getImportableEventsFromEventbrite(configUtils.getCompanyId(request), getConfiguration(request));
		final Function<EventModel, Boolean> notImportableLast = new Function<EventModel, Boolean>() {

			@Override
			public Boolean apply(final EventModel model) {
				return Validator.isNotNull(model.getEventbrite()) && (model.getEventbrite().isRecurrent() || model.getEventbrite().isMultiday());
			}
		};
		final List<EventModel> sortedCopy = Ordering.natural().nullsLast().onResultOf(notImportableLast).sortedCopy(importableEvents);
		model.addAttribute("eventbriteEvents", sortedCopy);
		return "event/import_eventbrite_event";
	}

	/* Imports an event from eventbrite into liferay */
	@ResourceMapping(value = "importEventbriteEvent")
	public void importEventbriteEvent(final ResourceRequest request, final ResourceResponse response, @RequestParam(value = "eventbriteId") final String eventbriteId)
			throws JSONException, IOException, EventException, EventbriteErrorException {
		final JSONObject result = new JSONObject();
		boolean eventImported = true;
		try {
			eventImported = eventService.importEventbriteEvent(getConfiguration(request), eventbriteId, getThemeDisplay(request));
		} catch (final EventbriteException e) {
			eventImported = false;
		}
		result.put("success", Boolean.toString(eventImported).toLowerCase());
		response.setContentType("text/javascript");
		final PrintWriter writer = response.getWriter();
		writer.write(result.toString());
	}

	/*
	 * Renders the read-only popup shown when clicking on an event in the
	 * calendar view mode.
	 */
	@RenderMapping(params = { "view=eventShortDetails" })
	public String viewEventShortDetailsView(final RenderRequest request, final Model model, @RequestParam(value = "eventId") final Long eventId,
			@RequestParam(value = "eventUid", required = false, defaultValue = "") final String eventUid) throws EventException, EventException, EventbriteException,
			EventNotFoundException, EventbriteErrorException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		final EventPortletConfiguration configuration = getConfiguration(request);

		final EventModel event = eventService.getEventShortDetails(getConfiguration(request), themeDisplay, eventId, eventUid);
		model.addAttribute("eventView", event);
		addCommonViewModelAttributes(model, themeDisplay);
		final boolean canEventBeModified = permissionChecker.canEventBeModified(themeDisplay, event, configuration);
		final boolean hasEditPermission = canEventBeModified && permissionChecker.hasEditPermission(themeDisplay, event);
		model.addAttribute("manageEventbriteEvent", isEventbriteEvent(event));
		model.addAttribute("hasEditPermission", hasEditPermission);
		return "event/view_event_short_details";
	}

	/* Renders the read-only details for the specified event */
	@RenderMapping(params = { "view=eventFullDetails" })
	public String viewEventFullDetailsView(final RenderRequest request, final Model model, @RequestParam(value = "eventId") final Long eventId,
			@RequestParam(value = "eventUid", required = false, defaultValue = "") final String eventUid,
			@RequestParam(value = "emailAddressesToInviteInvalid", required = false, defaultValue = "") final String emailAddressesToInviteInvalid,
			@RequestParam(value = "emailAddressesToInvite", required = false, defaultValue = "") final String emailAddressesToInvite,
			@RequestParam(value = "emailAddressesToInviteErrors", required = false, defaultValue = "false") final String emailAddressesToInviteErrors)
			throws EventException, EventException, EventbriteException, EventNotFoundException, EventbriteErrorException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		final EventPortletConfiguration configuration = getConfiguration(request);
		final EventModel event = eventService.getEventFullDetails(getConfiguration(request), themeDisplay, eventId, eventUid);

		final boolean canEventBeModified = permissionChecker.canEventBeModified(themeDisplay, event, configuration);
		final boolean hasEditPermission = canEventBeModified && permissionChecker.hasEditPermission(themeDisplay, event);
		final boolean hasDeletePermission = canEventBeModified && permissionChecker.hasDeletePermission(themeDisplay, event);

		final boolean relatedContentEnabled = GetterUtil.getBoolean(PropsUtil.get("enterprise.search.enabled", new Filter(CalEvent.class.getName())), false);

		addCommonViewModelAttributes(model, themeDisplay);
		model.addAttribute("emailAddressesToInviteErrors", emailAddressesToInviteErrors);
		model.addAttribute("emailAddressesToInviteInvalid", emailAddressesToInviteInvalid);
		model.addAttribute("emailAddressesToInvite", emailAddressesToInvite);
		model.addAttribute("eventView", event);
		model.addAttribute("manageEventbriteEvent", isEventbriteEvent(event));
		model.addAttribute("hasEditPermission", hasEditPermission);
		model.addAttribute("hasDeletePermission", hasDeletePermission);
		model.addAttribute("relatedContentEnabled", relatedContentEnabled);
		return "event/view_event_full_details";
	}

	/* Renders the popup shown to contact an event attendee */
	@RenderMapping(params = { "view=contactUser" })
	public String contactUserView(final RenderRequest request, final Model model, @RequestParam(value = "eventId") final Long eventId,
			@RequestParam(value = "emailAddress") final String emailAddress) {
		model.addAttribute("receiverEmailAddress", emailAddress);
		model.addAttribute("eventId", eventId);
		return "event/view_send_message_to_user";
	}

	/* Deletes the specified event */
	@ActionMapping(params = "action=contactUser")
	public void contactUserAction(final ActionRequest request, final ActionResponse response, @RequestParam(value = "eventId") final Long eventId,
			@RequestParam(value = "receiverEmailAddress", required = false, defaultValue = "") final String receiverEmailAddress,
			@RequestParam(value = "emailBody", required = false, defaultValue = "") final String emailBody) throws Exception {

		if (StringUtils.isBlank(receiverEmailAddress) || StringUtils.isBlank(emailBody)) {
			SessionErrors.add(request, "user-message-invalid");
		} else {
			notificationService.sendMessageToUser(receiverEmailAddress, emailBody, getThemeDisplay(request));
			SessionMessages.add(request, "user-message-sent");
		}
		redirectToView(request, response, "eventId", String.valueOf(eventId), "view", "eventFullDetails");
	}

	/* Renders the page to add a new event */
	@RenderMapping(params = { "view=addEvent" })
	public String addEventView(final RenderRequest request, final RenderResponse response, final SessionStatus sessionStatus, final Model model)
			throws EventPermissionException, EventbriteException, SystemException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (!permissionChecker.hasAddPermission(themeDisplay)) {
			throw new EventPermissionException("User does not have permissions to ADD event");
		}
		addModelAttributesForEditEventView(request, model, themeDisplay);
		model.addAttribute("actionName", "addEvent");
		return "event/edit_event";
	}

	/* Renders the page to edit the selected event */
	@RenderMapping(params = { "view=updateEvent" })
	public String updateEventView(final RenderRequest request, final RenderResponse response, final Model model, @ModelAttribute("event") EventModel event,
			@RequestParam(value = "eventId", required = false) final Long eventId) throws EventException, EventbriteException, EventNotFoundException,
			EventbriteErrorException, SystemException {
		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (eventId != null) {
			event = eventService.getEventForUpdate(getConfiguration(request), eventId, themeDisplay.getTimeZone());
			model.addAttribute("event", event);
		}
		addModelAttributesForEditEventView(request, model, themeDisplay);
		model.addAttribute("hasDeletePermission", permissionChecker.hasDeletePermission(themeDisplay, event));
		model.addAttribute("actionName", "updateEvent");
		return "event/edit_event";
	}

	/* Renders the page to add a new EventBrite event */
	@RenderMapping(params = { "view=addEventbriteEvent" })
	public String addEventbriteEventView(final RenderRequest request, final RenderResponse response, final SessionStatus sessionStatus, final Model model)
			throws EventPermissionException, EventbriteException, EventbriteErrorException, SystemException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (!permissionChecker.hasAddPermission(themeDisplay)) {
			throw new EventPermissionException("User does not have permissions to ADD event");
		}
		addModelAttributesForEditEventbriteEventView(request, model, themeDisplay, true);
		model.addAttribute("actionName", "addEventbriteEvent");
		model.addAttribute("isAddAction", "true");
		return "event/edit_eventbrite_event";
	}

	/* Renders the page to edit the selected Eventbrite event */
	@RenderMapping(params = { "view=updateEventbriteEvent" })
	public String updateEventbriteEventView(final RenderRequest request, final RenderResponse response, final Model model, @ModelAttribute("event") EventModel event,
			@RequestParam(value = "eventId", required = false) final Long eventId,
			@RequestParam(value = "showUpdateTickets", required = false, defaultValue = "false") final Boolean showUpdateTickets) throws EventException,
			EventbriteException, EventNotFoundException, EventbriteErrorException, SystemException {
		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (eventId != null) {
			event = eventService.getEventForUpdate(getConfiguration(request), eventId, themeDisplay.getTimeZone());
			model.addAttribute("event", event);
		}
		addModelAttributesForEditEventbriteEventView(request, model, themeDisplay, false);
		model.addAttribute("showUpdateTickets", showUpdateTickets);
		model.addAttribute("hasDeletePermission", permissionChecker.hasDeletePermission(themeDisplay, event));
		model.addAttribute("actionName", "updateEventbriteEvent");
		return "event/edit_eventbrite_event";
	}

	/* Creates a new event */
	@ActionMapping(params = "action=addEvent")
	public void addEventAction(final ActionRequest request, final ActionResponse response, final SessionStatus sessionStatus,
			@RequestParam(value = "recurrenceDaysSelection", required = false) final Integer[] recurrenceDaysSelection,
			@RequestParam(value = "regionStateSelect", required = false) final String regionStateSelect,
			@RequestParam(value = "regionStateText", required = false) final String regionStateText,
			@RequestParam(value = "startDay", required = false) final Integer startDay, @RequestParam(value = "startMonth", required = false) final Integer startMonth,
			@RequestParam(value = "startYear", required = false) final Integer startYear, @RequestParam(value = "endDay", required = false) final Integer endDay,
			@RequestParam(value = "endMonth", required = false) final Integer endMonth, @RequestParam(value = "endYear", required = false) final Integer endYear,
			@RequestParam(value = "recurrenceEndDay", required = false) final Integer recurrenceEndDay,
			@RequestParam(value = "recurrenceEndMonth", required = false) final Integer recurrenceEndMonth,
			@RequestParam(value = "recurrenceEndYear", required = false) final Integer recurrenceEndYear, @ModelAttribute("event") final EventModel event)
			throws EventPermissionException, EventbriteException, EventException, ValidationException, EventbriteErrorException, NestableException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (!permissionChecker.hasAddPermission(themeDisplay)) {
			throw new EventPermissionException("User does not have permissions to ADD event");
		}
		modelUtils.refreshEventValuesForUpdate(event, themeDisplay.getUser().getTimeZone(), regionStateSelect, regionStateText, startDay, startMonth, startYear, endDay,
				endMonth, endYear, recurrenceDaysSelection, recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);
		final Set<String> errors = validator.validateEvent(event);
		if (!errors.isEmpty()) {
			LOG.debug("Validation errors while creating new event");
			setSessionErrors(request, errors);
			response.setRenderParameter("view", "addEvent");

			addTagsToRequest(request, response);
		} else {
			final CalEvent createdEvent = eventService.createEvent(request, getConfiguration(request), event, false);
			clearSessionObject(sessionStatus);
			SessionMessages.add(request, "event-created");
			redirectToView(request, response, "view", "eventFullDetails", "eventId", String.valueOf(createdEvent.getEventId()));
		}
	}

	@ActionMapping(params = "action=updateEvent")
	public void updateEventAction(final ActionRequest request, final ActionResponse response, final SessionStatus sessionStatus,
			@RequestParam(value = "recurrenceDaysSelection", required = false) final Integer[] recurrenceDaysSelection,
			@RequestParam(value = "regionStateSelect", required = false) final String regionStateSelect,
			@RequestParam(value = "regionStateText", required = false) final String regionStateText,
			@RequestParam(value = "startDay", required = false) final Integer startDay, @RequestParam(value = "startMonth", required = false) final Integer startMonth,
			@RequestParam(value = "startYear", required = false) final Integer startYear, @RequestParam(value = "endDay", required = false) final Integer endDay,
			@RequestParam(value = "endMonth", required = false) final Integer endMonth, @RequestParam(value = "endYear", required = false) final Integer endYear,
			@RequestParam(value = "recurrenceEndDay", required = false) final Integer recurrenceEndDay,
			@RequestParam(value = "recurrenceEndMonth", required = false) final Integer recurrenceEndMonth,
			@RequestParam(value = "recurrenceEndYear", required = false) final Integer recurrenceEndYear, @ModelAttribute("event") final EventModel event)
			throws EventPermissionException, EventbriteException, EventException, ValidationException, EventbriteErrorException, NestableException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (!permissionChecker.hasEditPermission(themeDisplay, event)) {
			throw new EventPermissionException("User does not have permissions to EDIT event");
		}
		modelUtils.refreshEventValuesForUpdate(event, themeDisplay.getUser().getTimeZone(), regionStateSelect, regionStateText, startDay, startMonth, startYear, endDay,
				endMonth, endYear, recurrenceDaysSelection, recurrenceEndDay, recurrenceEndMonth, recurrenceEndYear);
		final Set<String> errors = validator.validateEvent(event);
		if (!errors.isEmpty()) {
			LOG.debug("Validation errors while updating event event");
			setSessionErrors(request, errors);
			response.setRenderParameter("view", "updateEvent");
		} else {
			eventService.updateEvent(request, getConfiguration(request), event, false, false);
			SessionMessages.add(request, "event-updated");
			clearSessionObject(sessionStatus);
			redirectToView(request, response);
		}
	}

	/* Creates a new Eventbrite event */
	@ActionMapping(params = "action=addEventbriteEvent")
	public void addEventbriteEventAction(final ActionRequest request, final ActionResponse response, final SessionStatus sessionStatus,
			@RequestParam(value = "regionStateSelect", required = false) final String regionStateSelect,
			@RequestParam(value = "regionStateText", required = false) final String regionStateText,
			@RequestParam(value = "startDay", required = false) final Integer startDay, @RequestParam(value = "startMonth", required = false) final Integer startMonth,
			@RequestParam(value = "startYear", required = false) final Integer startYear,
			@RequestParam(value = "ticketsToRemove", required = false, defaultValue = "") final String ticketsToRemove,
			@RequestParam(value = "ticketsStartDay", required = false) final Integer ticketsStartDay,
			@RequestParam(value = "ticketsStartMonth", required = false) final Integer ticketsStartMonth,
			@RequestParam(value = "ticketsStartYear", required = false) final Integer ticketsStartYear,
			@RequestParam(value = "ticketsEndDay", required = false) final Integer ticketsEndDay,
			@RequestParam(value = "ticketsEndMonth", required = false) final Integer ticketsEndMonth,
			@RequestParam(value = "ticketsEndYear", required = false) final Integer ticketsEndYear, @ModelAttribute("event") final EventModel event)
			throws EventPermissionException, EventbriteException, EventException, EventbriteErrorException, NestableException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (!permissionChecker.hasAddPermission(themeDisplay)) {
			throw new EventPermissionException("User does not have permissions to ADD event");
		}
		modelUtils.refreshEventbriteEventValuesForUpdate(event, themeDisplay.getUser().getTimeZone(), regionStateSelect, regionStateText, startDay, startMonth, startYear,
				ticketsStartDay, ticketsStartMonth, ticketsStartYear, ticketsEndDay, ticketsEndMonth, ticketsEndYear);
		final Set<String> errors = validator.validateEventbriteEvent(event, ticketsToRemove, true);
		if (!errors.isEmpty()) {
			LOG.debug("Validation errors while creating new Eventbrite event");
			setSessionErrors(request, errors);
			response.setRenderParameter("view", "addEventbriteEvent");
			addTagsToRequest(request, response);
		} else {
			try {
				final CalEvent createdEvent = eventService.createEvent(request, getConfiguration(request), event, true);
				clearSessionObject(sessionStatus);
				SessionMessages.add(request, "event-created");
				// Redirect to view events details page
				redirectToView(request, response, "view", "eventFullDetails", "eventId", String.valueOf(createdEvent.getEventId()));
			} catch (final ValidationException e) {
				SessionErrors.add(request, e.getMessage());
				response.setRenderParameter("view", "addEventbriteEvent");
				addTagsToRequest(request, response);
			}
		}
	}

	private void addTagsToRequest(final ActionRequest request, final ActionResponse response) {
		final String[] parameterValues = ParamUtil.getParameterValues(request, "assetTagNames");
		if (parameterValues != null) {
			final String join = Joiner.on(",").skipNulls().join(parameterValues);
			response.setRenderParameter("assetTagNames", join);
		}
	}

	@ActionMapping(params = "action=updateEventbriteEvent")
	public void updateEventbriteEventAction(final ActionRequest request, final ActionResponse response, final SessionStatus sessionStatus,
			@RequestParam(value = "updateTickets", required = false, defaultValue = "false") final Boolean updateTickets,
			@RequestParam(value = "regionStateSelect", required = false) final String regionStateSelect,
			@RequestParam(value = "regionStateText", required = false) final String regionStateText,
			@RequestParam(value = "startDay", required = false) final Integer startDay, @RequestParam(value = "startMonth", required = false) final Integer startMonth,
			@RequestParam(value = "startYear", required = false) final Integer startYear,
			@RequestParam(value = "ticketsToRemove", required = false, defaultValue = "") final String ticketsToRemove,
			@RequestParam(value = "ticketsStartDay", required = false) final Integer ticketsStartDay,
			@RequestParam(value = "ticketsStartMonth", required = false) final Integer ticketsStartMonth,
			@RequestParam(value = "ticketsStartYear", required = false) final Integer ticketsStartYear,
			@RequestParam(value = "ticketsEndDay", required = false) final Integer ticketsEndDay,
			@RequestParam(value = "ticketsEndMonth", required = false) final Integer ticketsEndMonth,
			@RequestParam(value = "ticketsEndYear", required = false) final Integer ticketsEndYear, @ModelAttribute("event") final EventModel event)
			throws EventPermissionException, EventbriteException, EventException, EventbriteErrorException, NestableException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);
		if (!permissionChecker.hasEditPermission(themeDisplay, event)) {
			throw new EventPermissionException("User does not have permissions to EDIT event");
		}
		modelUtils.refreshEventbriteEventValuesForUpdate(event, themeDisplay.getUser().getTimeZone(), regionStateSelect, regionStateText, startDay, startMonth, startYear,
				ticketsStartDay, ticketsStartMonth, ticketsStartYear, ticketsEndDay, ticketsEndMonth, ticketsEndYear);
		final Set<String> errors = validator.validateEventbriteEvent(event, ticketsToRemove, updateTickets);
		if (!errors.isEmpty()) {
			LOG.debug("Validation errors while updating Eventbrite event");
			setSessionErrors(request, errors);
			response.setRenderParameter("showUpdateTickets", String.valueOf(updateTickets));
			response.setRenderParameter("view", "updateEventbriteEvent");
		} else {
			try {
				eventService.updateEvent(request, getConfiguration(request), event, true, updateTickets);
				SessionMessages.add(request, "event-updated");
				clearSessionObject(sessionStatus);
				redirectToView(request, response);
			} catch (final ValidationException e) {
				SessionErrors.add(request, e.getMessage());
				response.setRenderParameter("view", "updateEventbriteEvent");
			}
		}
	}

	/* Deletes the specified event */
	@ActionMapping(params = "action=deleteEvent")
	public void deleteEventAction(final ActionRequest request, final ActionResponse response, @RequestParam(value = "eventId") final Long eventId) throws EventException,
			EventbriteException, EventbriteErrorException, NestableException {

		eventService.deleteEvent(configUtils.getCompanyId(request), getConfiguration(request), eventId);
		SessionMessages.add(request, "event-deleted");
		redirectToView(request, response);
	}

	/* Renders the popup shown to invite members to the evenrbrite event. */
	@ActionMapping(params = "action=invitePeople")
	public void invitePeopleAction(final ActionRequest request, final ActionResponse response, @RequestParam(value = "eventId") final Long eventId,
			@RequestParam(value = "emailAddressesToInvite", required = false, defaultValue = "") String emailAddressesToInvite) throws Exception {

		boolean valid = false;
		String invalidEmailsString = "";
		if (StringUtils.isNotBlank(emailAddressesToInvite)) {
			final Iterable<String> emailsToCheck = Splitter.on(StringPool.COMMA).omitEmptyStrings().split(emailAddressesToInvite);
			final List<String> validEmails = Lists.newArrayList();
			final List<String> invalidEmails = Lists.newArrayList();
			for (String email : emailsToCheck) {
				email = StringUtils.trimToEmpty(email);
				if (Validator.isEmailAddress(email)) {
					validEmails.add(email);
				} else {
					invalidEmails.add(email);
				}
			}

			if (!validEmails.isEmpty() && invalidEmails.isEmpty()) {
				ThemeDisplay themeDisplay = getThemeDisplay(request);
				final EventModel event = eventService.getEventForUpdate(getConfiguration(request), eventId, themeDisplay.getTimeZone());
				notificationService.sendEventInvite(validEmails, event, themeDisplay);
				SessionMessages.add(request, "people-invited");
				emailAddressesToInvite = "";
				valid = true;
			} else {
				if (!invalidEmails.isEmpty()) {
					SessionErrors.add(request, "people-invite-email-invalid");
				}
				if (validEmails.isEmpty()) {
					SessionErrors.add(request, "people-invite-email-required");
				}
				invalidEmailsString = Joiner.on(StringPool.COMMA).skipNulls().join(invalidEmails);
			}
		} else {
			SessionErrors.add(request, "people-invite-email-required");
		}

		redirectToView(request, response, "view", "eventFullDetails", "emailAddressesToInviteErrors", String.valueOf(!valid), "emailAddressesToInvite",
				emailAddressesToInvite, "emailAddressesToInviteInvalid", invalidEmailsString, "eventId", String.valueOf(eventId));
	}

	@ActionMapping(params = "action=updateEventDiscussionMessage")
	public void updateEventDiscussionMessage(final ActionRequest request, final ActionResponse response) throws NestableException, JSONException {

		final ThemeDisplay themeDisplay = getThemeDisplay(request);

		if (themeDisplay.isSignedIn()) {
			final String cmd = ParamUtil.getString(request, Constants.CMD);

			final long groupId = PortalUtil.getScopeGroupId(request);
			final String className = ParamUtil.getString(request, "className");
			final long classPK = ParamUtil.getLong(request, "classPK");
			final long messageId = ParamUtil.getLong(request, "messageId");
			final long threadId = ParamUtil.getLong(request, "threadId");
			final long parentMessageId = ParamUtil.getLong(request, "parentMessageId");
			final String subject = ParamUtil.getString(request, "subject");
			final String body = ParamUtil.getString(request, "body");

			final ServiceContext serviceContext = ServiceContextFactory.getInstance(MBMessage.class.getName(), request);

			if (cmd.equals(Constants.DELETE)) {
				MBMessageServiceUtil.deleteDiscussionMessage(groupId, className, classPK, className, classPK, themeDisplay.getUserId(), messageId);
			} else {
				if (messageId <= 0) {
					final MBMessage mess = MBMessageServiceUtil.addDiscussionMessage(groupId, className, classPK, className, classPK, themeDisplay.getUserId(),
							threadId, parentMessageId, subject, body, serviceContext);

					if (className.equals(CalEvent.class.getName()) && parentMessageId != 0) {

						final CalEvent entry = CalEventLocalServiceUtil.getCalEvent(classPK);

						final JSONObject extraData = new JSONObject();
						extraData.put("messageId", mess.getMessageId());
						SocialActivityLocalServiceUtil.addActivity(themeDisplay.getUserId(), entry.getGroupId(), CalEvent.class.getName(), classPK,
								CalEventActivityKeys.ADD_COMMENT, extraData.toString(), entry.getUserId());
					}
				} else {
					MBMessageServiceUtil.updateDiscussionMessage(className, classPK, className, classPK, themeDisplay.getUserId(), messageId, subject, body,
							serviceContext);
				}
			}
		}
	}

	private void addCommonViewModelAttributes(final Model model, final ThemeDisplay themeDisplay) {
		model.addAttribute("calEventClassName", CalEvent.class.getName());
		model.addAttribute("isTeamworxxDeployed", isTeamworxxDeployed());
		if (themeDisplay.isSignedIn()) {
			model.addAttribute("loggedInUser", themeDisplay.getUser());
		}
		model.addAttribute("eventbriteEnabled", configUtils.isEventbriteEnabled(themeDisplay.getCompanyId()));
	}

	private long getCurrentNetworkId(final boolean isGuestGroup, final PortletRequest request) {
		if (isGuestGroup) {
			return NetworkLocalServiceUtil.getCurrentNetworkId(request);
		}
		return 0;
	}

	private Long[] getNetworkGroupIds(final boolean isGuestGroup, final PortletRequest request) {
		if (isGuestGroup) {
			try {
				return NetworkGroupLocalServiceUtil.getCurrentNetworkGroupIds(request);
			} catch (final NestableException e) {
				LOG.warn("Exception retrieving network group ids", e);
			}
		}
		return null;
	}

	private boolean isEventbriteEvent(final EventModel event) {
		return configUtils.isEventbriteEnabled(event.getCompanyId()) && StringUtils.isNotBlank(StringUtils.trimToNull(event.getEventbrite().getEventbriteId()));
	}

	private boolean isTeamworxxDeployed() {
		boolean result = false;
		try {
			final PortletApp portletApp = PortletLocalServiceUtil.getPortletApp("teamworxx-portlet");
			result = !portletApp.getPortlets().isEmpty();
		} catch (final Exception e) {
			LOG.warn("Exception determining if teamworxx-portlet is deployed", e);
		}
		return result;
	}

	private void redirectToView(final ActionRequest request, final ActionResponse response) throws NestableException {
		try {
			final ServiceContext context = ServiceContextFactory.getInstance(request);
			final ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			final LiferayPortletURL redirectUrl = PortletURLFactoryUtil.create(request, context.getPortletId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
			redirectUrl.setWindowState(WindowState.NORMAL);

			response.sendRedirect(redirectUrl.toString());
		} catch (final Exception e) {
			LOG.error("Exception creating default render url", e);
			throw new NestableException(e);
		}
	}

	private void redirectToView(final ActionRequest request, final ActionResponse response, final String... requestParams) throws NestableException {
		try {
			final ServiceContext context = ServiceContextFactory.getInstance(request);
			final ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			final LiferayPortletURL redirectUrl = PortletURLFactoryUtil.create(request, context.getPortletId(), themeDisplay.getPlid(), PortletRequest.RENDER_PHASE);
			redirectUrl.setWindowState(WindowState.NORMAL);

			if (requestParams.length % 2 != 0) {
				throw new IllegalArgumentException("Invalid number of request parameters. Current length is " + requestParams.length);
			}
			for (int i = 0; i < requestParams.length; i += 2) {
				final String name = requestParams[i];
				final String value = requestParams[i + 1];
				redirectUrl.setParameter(name, value);
				LOG.debug("Added request parameter " + name + " with value " + value);
			}

			response.sendRedirect(redirectUrl.toString());
		} catch (final Exception e) {
			LOG.error("Exception creating default render url", e);
			throw new NestableException(e);
		}
	}

	/* Adds the attributes required for the add and edit event page. */
	private void addModelAttributesForEditEventView(final PortletRequest request, final Model model, final ThemeDisplay themeDisplay) throws EventbriteException,
			SystemException {
		final Locale locale = themeDisplay.getLocale();
		addCommonAttributes(request, model, themeDisplay, locale);
		model.addAttribute("recurrenceTypes", RecurrenceTypes.values());
		model.addAttribute("days", modelUtils.getDays(request, locale));
	}

	@SuppressWarnings("unchecked")
	private void addCommonAttributes(final PortletRequest request, final Model model, final ThemeDisplay themeDisplay, final Locale locale) throws SystemException {
		final Calendar calendar = CalendarFactoryUtil.getCalendar(themeDisplay.getTimeZone(), locale);
		model.addAttribute("calendarYear", calendar.get(Calendar.YEAR));
		model.addAttribute("firstDayOfWeek", calendar.getFirstDayOfWeek());
		model.addAttribute("countries", modelUtils.getCountries(request, locale));
		model.addAttribute("canConfigureFeaturedEvents", permissionChecker.canConfigureFeaturedEvents(themeDisplay));
		final Group scopeGroup = themeDisplay.getScopeGroup();
		Object[] securityLevels = getAvailableSecurityLevels(scopeGroup, themeDisplay.getLocale());
		model.addAttribute("securityLevelsHelpText", (String) securityLevels[1]);
		model.addAttribute("securityLevels", (Map<String, String>) securityLevels[0]);
	}

	/**
	 * @return
	 * Object[0] = Map<String, String> Available Security level values <key, label>
	 * Object[1] = Help text for the field "Security Level"
	 */
	public Object[] getAvailableSecurityLevels(Group group, final Locale locale) throws SystemException {
		final List<ContentSecurityLevel> securityLevels = new ArrayList<ContentSecurityLevel>();
		if (group.getType() == GroupConstants.TYPE_SITE_OPEN || group.getType() == GroupConstants.TYPE_SITE_RESTRICTED) {
			final NetworkGroup networkGroup = NetworkGroupLocalServiceUtil.getNetworkGroupByGroupId(group.getGroupId());
			if (Validator.isNull(networkGroup)) {
				//Not in a network
				securityLevels.add(ContentSecurityLevel.GROUP);
				securityLevels.add(ContentSecurityLevel.PUBLIC);
			} else {
				//In a network
				securityLevels.add(ContentSecurityLevel.GROUP);
				if (networkGroup.isDomainGroup()) {
					securityLevels.add(ContentSecurityLevel.NETWORK_INTRANET);
				} else {
					securityLevels.add(ContentSecurityLevel.NETWORK);
					securityLevels.add(ContentSecurityLevel.PUBLIC);
				}
			}
		} else {
			securityLevels.add(ContentSecurityLevel.GROUP);
		}

		final Object[] result = new Object[2];
		final StringBuffer labelText = new StringBuffer();
		final Map<String, String> availableValues = new HashMap<String, String>();
		for (final ContentSecurityLevel securityLevel : securityLevels) {
			final String key = securityLevel.getKey();
			availableValues.put(key, LanguageUtil.get(locale, "event-security-level-" + key));
			labelText.append("<p>");
			labelText.append(LanguageUtil.get(locale, "event-security-level-help-" + key));
			labelText.append("</p>");
		}
		result[0] = availableValues;
		result[1] = labelText.toString();
		return result;
	}
	

	/* Adds the attributes required for the add and edit event page. */
	private void addModelAttributesForEditEventbriteEventView(final PortletRequest request, final Model model, final ThemeDisplay themeDisplay, final boolean isAddAction)
			throws EventbriteException, EventbriteErrorException, SystemException {

		final Locale locale = themeDisplay.getLocale();
		addCommonAttributes(request, model, themeDisplay, locale);
		final Boolean eventbriteEnabled = configUtils.isEventbriteEnabled(themeDisplay.getCompanyId());
		if (eventbriteEnabled) {
			model.addAttribute("availableOrganizers", eventService.getAvailableOrganizers(request, getConfiguration(request)));
			model.addAttribute("availableVenues", eventService.getAvailableLocations(request, getConfiguration(request)));
			if (isAddAction) {
				model.addAttribute("availableCurrencies", modelUtils.getCurrencies(request));
			}
		}
	}

	private EventPortletConfiguration getConfiguration(final PortletRequest request) {
		return new EventPortletConfiguration(request.getPreferences());
	}

	private ThemeDisplay getThemeDisplay(final PortletRequest request) {
		return (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
	}

	/* Adds all the errors as SessionErrors */
	private void setSessionErrors(final ActionRequest request, final Set<String> errors) {
		for (final String error : errors) {
			SessionErrors.add(request, error);
		}
	}

	/* Sets the Sping sessionStatus as complete */
	private void clearSessionObject(final SessionStatus sessionStatus) {
		sessionStatus.setComplete();
	}

	@ModelAttribute("event")
	public EventModel getCommandObject() {
		return new EventModel();
	}

}
