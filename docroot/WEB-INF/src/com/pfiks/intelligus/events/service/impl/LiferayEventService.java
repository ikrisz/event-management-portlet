package com.pfiks.intelligus.events.service.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.liferay.portal.kernel.cal.DayAndPosition;
import com.liferay.portal.kernel.cal.Duration;
import com.liferay.portal.kernel.cal.Recurrence;
import com.liferay.portal.kernel.cal.TZSRecurrence;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceContextFactory;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.calendar.NoSuchEventException;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.constants.PrivacySettingConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.exception.EventNotFoundException;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventDates;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.startup.ExpandoFieldCreationUtil;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;
import com.pfiks.intelligus.networks.portal.service.NetworkGroupLocalServiceUtil;

@Component
public class LiferayEventService {

	private static final Log LOG = LogFactoryUtil.getLog(LiferayEventService.class);

	@Resource
	private ConfigurationUtils utils;

	public CalEvent getEvent(final Long eventId) throws EventException, EventNotFoundException {
		try {
			return CalEventLocalServiceUtil.getEvent(eventId);
		} catch (final NoSuchEventException e) {
			LOG.error("NoSuchEventException with eventId: " + eventId);
			throw new EventNotFoundException("event.not.found");
		} catch (final NestableException e) {
			LOG.error("Exception retrieving event with eventId: " + eventId + ". " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	public void deleteEvent(final Long eventId) throws EventException {
		try {
			CalEventLocalServiceUtil.deleteEvent(eventId);
			LOG.info("Deleted CalEvent with eventId: " + eventId);
		} catch (final NestableException e) {
			LOG.error("Exception deleting event with eventId: " + eventId + ". " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	public CalEvent createEvent(final ActionRequest request, final EventModel model) throws EventException {
		try {
			final ServiceContext serviceContext = getServiceContextFromRequest(request);
			setServiceContextValues(serviceContext, model);

			final ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			final Long userId = themeDisplay.getUserId();

			final String title = model.getTitle();
			final String description = model.getDescription();
			final boolean allDay = model.getDates().isAllDay();

			final int startDateMinute = model.getDates().getStartMinute();
			final int startDateHour = model.getDates().getStartHour();
			final int startDateDay = model.getDates().getStartDay();
			final int startDateMonth = model.getDates().getStartMonth();
			final int startDateYear = model.getDates().getStartYear();

			final int durationHour = model.getDates().getDurationHour();
			final int durationMinute = model.getDates().getDurationMinute();

			final TZSRecurrence recurrence = getTZSRecurrenceForEvent(model.getDates(), themeDisplay.getTimeZone(), themeDisplay.getLocale());
			final boolean repeating = recurrence != null;

			final boolean timeZoneSensitive = true;
			final CalEvent event = CalEventLocalServiceUtil.addEvent(userId, title, description, StringPool.BLANK, startDateMonth, startDateDay, startDateYear,
					startDateHour, startDateMinute, durationHour, durationMinute, allDay, timeZoneSensitive, "event", repeating, recurrence, 0, 0, 0,
					serviceContext);

			LOG.info("Created CalEvent with id: " + event.getEventId());
			return event;
		} catch (final NestableException e) {
			LOG.error("Exception creating new event. ", e);
			throw new EventException(e);
		}
	}

	public CalEvent updateEvent(final ActionRequest request, final EventModel model) throws EventException {
		try {
			final ServiceContext serviceContext = getServiceContextFromRequest(request);
			setServiceContextValues(serviceContext, model);

			final ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
			final Long userId = themeDisplay.getUserId();
			final Long eventId = model.getEventId();
			final String title = model.getTitle();
			final String description = model.getDescription();

			final boolean allDay = model.getDates().isAllDay();

			final int startDateMinute = model.getDates().getStartMinute();
			final int startDateHour = model.getDates().getStartHour();
			final int startDateDay = model.getDates().getStartDay();
			final int startDateMonth = model.getDates().getStartMonth();
			final int startDateYear = model.getDates().getStartYear();

			final int durationHour = model.getDates().getDurationHour();
			final int durationMinute = model.getDates().getDurationMinute();

			final TZSRecurrence recurrence = getTZSRecurrenceForEvent(model.getDates(), themeDisplay.getTimeZone(), themeDisplay.getLocale());
			final boolean repeating = recurrence != null;

			final boolean timeZoneSensitive = true;

			final CalEvent updateEvent = CalEventLocalServiceUtil.updateEvent(userId, eventId, title, description, StringPool.BLANK, startDateMonth, startDateDay,
					startDateYear, startDateHour, startDateMinute, durationHour, durationMinute, allDay, timeZoneSensitive, "event", repeating, recurrence, 0,
					0, 0, serviceContext);

			LOG.info("Updated CalEvent with id: " + updateEvent.getEventId());
			return updateEvent;
		} catch (final NestableException e) {
			LOG.error("Exception updating event", e);
			throw new EventException(e);
		}
	}

	public CalEvent syncEventFromEventbrite(final CalEvent calEvent, final EventModel event) throws EventException {
		try {
			final ServiceContext serviceContext = createNewServiceContext(calEvent);
			setServiceContextValues(serviceContext, event);

			final Long userId = calEvent.getUserId();

			final String title = event.getTitle();
			final String description = event.getDescription();

			final DateTime startDate = event.getDates().getStartDate();
			final DateTime endDate = event.getDates().getEndDate();

			final int startDateMinute = startDate.getMinuteOfHour();
			final int startDateHour = startDate.getHourOfDay();
			final int startDateDay = startDate.getDayOfMonth();
			final int startDateMonth = startDate.getMonthOfYear() - 1;
			final int startDateYear = startDate.getYear();

			int durationHour = Hours.hoursBetween(startDate, endDate).getHours();
			int durationMinute = Minutes.minutesBetween(startDate, endDate).getMinutes() % 60;

			TZSRecurrence recurrence = calEvent.getRecurrenceObj();
			boolean allDay = false;
			if (durationHour > 24) {
				final TimeZone timeZone = recurrence.getTimeZone();
				final User eventUser = UserLocalServiceUtil.getUser(userId);
				final Locale locale = eventUser.getLocale();
				// Multi day
				allDay = true;
				startDate.withMillisOfDay(00);
				startDate.withHourOfDay(00);
				durationHour = 24;
				durationMinute = 00;

				final Calendar recStartCal = startDate.toCalendar(locale);
				recurrence = new TZSRecurrence(recStartCal, new Duration(1, 0, 0, 0), RecurrenceTypes.DAILY.getType());
				recurrence.setTimeZone(timeZone);
				recurrence.setWeekStart(Calendar.SUNDAY);
				recurrence.setUntil(endDate.toCalendar(locale));
				recurrence.setInterval(1);
			}

			final boolean repeating = recurrence != null;

			final boolean timeZoneSensitive = true;

			final CalEvent updateEvent = CalEventLocalServiceUtil.updateEvent(userId, calEvent.getEventId(), title, description, StringPool.BLANK, startDateMonth,
					startDateDay, startDateYear, startDateHour, startDateMinute, durationHour, durationMinute, allDay, timeZoneSensitive, "event", repeating,
					recurrence, 0, 0, 0, serviceContext);

			LOG.info("Updated CalEvent with id: " + updateEvent.getEventId());
			return updateEvent;
		} catch (final NestableException e) {
			LOG.error("Exception updating event", e);
			throw new EventException(e);
		}
	}

	public List<String> getIdsOfImportedEventbriteEvents(final long companyId) throws EventException {
		try {
			final List<String> results = Lists.newArrayList();
			final List<ExpandoValue> columnValues = ExpandoValueLocalServiceUtil.getColumnValues(companyId, CalEvent.class.getName(),
					ExpandoTableConstants.DEFAULT_TABLE_NAME, EventExpandoConstants.EVENTBRITE_ID, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
			for (final ExpandoValue expandoValue : columnValues) {
				final String data = expandoValue.getData();
				if (Validator.isNotNull(data)) {
					results.add(data);
				}
			}
			return results;
		} catch (final NestableException e) {
			LOG.warn("Exception retrieving imported eventbriteIds: " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	public CalEvent createEventFromEventbriteDetails(final EventModel model, final ThemeDisplay themeDisplay) throws EventException {
		try {
			final ServiceContext serviceContext = new ServiceContext();
			serviceContext.setCompanyId(themeDisplay.getCompanyId());
			serviceContext.setScopeGroupId(themeDisplay.getScopeGroupId());

			updatePermissionsInServiceContext(model.isSecurityLevelPublic(), serviceContext);
			setExpandoFieldsInServiceContext(model, serviceContext);

			final Long userId = themeDisplay.getUserId();

			final String title = model.getTitle();
			final String description = model.getDescription();

			final DateTime utcStartDate = model.getDates().getStartDate();
			final DateTime utcEndDate = model.getDates().getEndDate();
			final DateTimeZone userTimeZone = DateTimeZone.forID(themeDisplay.getTimeZone().getID());
			final DateTime startDate = utcStartDate.withZone(userTimeZone);
			final DateTime endDate = utcEndDate.withZone(userTimeZone);

			final int startDateMinute = startDate.getMinuteOfHour();
			final int startDateHour = startDate.getHourOfDay();
			final int startDateDay = startDate.getDayOfMonth();
			final int startDateMonth = startDate.getMonthOfYear() - 1;
			final int startDateYear = startDate.getYear();

			int durationHour = Hours.hoursBetween(startDate, endDate).getHours();
			int durationMinute = Minutes.minutesBetween(startDate, endDate).getMinutes() % 60;

			TZSRecurrence recurrence = null;
			boolean allDay = false;
			if (durationHour > 24) {
				// Multi day
				allDay = true;
				startDate.withMillisOfDay(00);
				startDate.withHourOfDay(00);
				durationHour = 24;
				durationMinute = 00;
				final Locale locale = themeDisplay.getLocale();
				final TimeZone timeZone = themeDisplay.getTimeZone();

				final int recurrenceType = RecurrenceTypes.DAILY.getType();
				final Calendar recStartCal = startDate.toCalendar(locale);
				recurrence = new TZSRecurrence(recStartCal, new Duration(1, 0, 0, 0), recurrenceType);
				recurrence.setTimeZone(timeZone);
				recurrence.setWeekStart(Calendar.SUNDAY);
				recurrence.setUntil(endDate.toCalendar(locale));
				recurrence.setInterval(1);
			}

			final boolean repeating = recurrence != null;

			final boolean timeZoneSensitive = true;

			final CalEvent event = CalEventLocalServiceUtil.addEvent(userId, title, description, StringPool.BLANK, startDateMonth, startDateDay, startDateYear,
					startDateHour, startDateMinute, durationHour, durationMinute, allDay, timeZoneSensitive, "event", repeating, recurrence, 0, 0, 0,
					serviceContext);
			LOG.info("Imported Eventbrite event. Created new CalEvent with id: " + event.getEventId());
			return event;
		} catch (final NestableException e) {
			LOG.error("Exception importing eventbrite event.", e);
			throw new EventException(e);
		}
	}

	private ServiceContext getServiceContextFromRequest(final ActionRequest request) throws NestableException {
		return ServiceContextFactory.getInstance(CalEvent.class.getName(), request);
	}

	private ServiceContext setServiceContextValues(final ServiceContext serviceContext, final EventModel model) {
		setExpandoFieldsInServiceContext(model, serviceContext);
		updatePermissionsInServiceContext(model.isSecurityLevelPublic(), serviceContext);
		return serviceContext;
	}

	private ServiceContext createNewServiceContext(final CalEvent calEvent) {
		final ServiceContext serviceContext = new ServiceContext();
		serviceContext.setCompanyId(calEvent.getCompanyId());
		serviceContext.setScopeGroupId(calEvent.getGroupId());
		serviceContext.setUserId(calEvent.getUserId());
		return serviceContext;
	}

	/* Adds all the expando field values in service context */
	private void setExpandoFieldsInServiceContext(final EventModel event, final ServiceContext serviceContext) {
		final Map<String, Serializable> expAttributes = serviceContext.getExpandoBridgeAttributes();

		expAttributes.put(EventExpandoConstants.SECURITY_LEVEL, event.getSecurityLevelKey());
		expAttributes.put(EventExpandoConstants.FEATURED, event.isFeaturedEvent());

		try {
			final long networkId = NetworkGroupLocalServiceUtil.getGroupNetworkId(serviceContext.getScopeGroupId());
			if (networkId > 0) {
				expAttributes.put(EventExpandoConstants.NETWORK_ID, networkId);
			}
		} catch (final SystemException e) {
		}

		expAttributes.put(EventExpandoConstants.SUMMARY, event.getSummary());

		//Empty the old location field.
		if (ExpandoFieldCreationUtil.doesExpandoFieldExist(serviceContext.getCompanyId(), CalEvent.class.getName(), EventExpandoConstants.LOCATION)) {
			expAttributes.put(EventExpandoConstants.LOCATION, null);
		}

		boolean onlineEvent = true;
		String venueName = "";
		String venueAddressLineOne = "";
		String venueAddressLineTwo = "";
		String venueCity = "";
		String venueRegionState = "";
		String venueZip = "";
		String venueCountry = "";

		final EventVenue venue = event.getVenue();
		if (Validator.isNotNull(venue)) {
			onlineEvent = venue.isOnline();
			venueName = venue.getName();
			venueAddressLineOne = venue.getAddressLineOne();
			venueAddressLineTwo = venue.getAddressLineTwo();
			venueCity = venue.getCity();
			venueRegionState = venue.getRegionState();
			venueZip = venue.getZip();
			venueCountry = venue.getCountry();
		}

		expAttributes.put(EventExpandoConstants.ONLINE_EVENT, onlineEvent);
		expAttributes.put(EventExpandoConstants.VENUE, venueName);
		expAttributes.put(EventExpandoConstants.ADDRESS_1, venueAddressLineOne);
		expAttributes.put(EventExpandoConstants.ADDRESS_2, venueAddressLineTwo);
		expAttributes.put(EventExpandoConstants.CITY, venueCity);
		expAttributes.put(EventExpandoConstants.REGION_STATE, venueRegionState);
		expAttributes.put(EventExpandoConstants.ZIP_CODE, venueZip);
		expAttributes.put(EventExpandoConstants.COUNTRY, venueCountry);

		if (Validator.isNotNull(event.getEventbrite())) {
			expAttributes.put(EventExpandoConstants.EVENTBRITE_ID, event.getEventbrite().getEventbriteId());
			expAttributes.put(EventExpandoConstants.EVENTBRITE_USER_API, event.getEventbrite().getEventbriteUserApiKey());
		}

		serviceContext.setExpandoBridgeAttributes(expAttributes);
	}

	/* Configures the permission based on the public flag. */
	private void updatePermissionsInServiceContext(final Boolean isPublicEvent, final ServiceContext serviceContext) {
		String[] guestPermsission = null;
		String[] groupPermission = null;
		if (isPublicEvent) {
			guestPermsission = PrivacySettingConstants.GUEST_PERMISSIONS_PUBLIC.getActionKeys();
			groupPermission = PrivacySettingConstants.GROUP_PERMISSIONS_PUBLIC.getActionKeys();
		} else {
			guestPermsission = PrivacySettingConstants.GUEST_PERMISSIONS_GROUP.getActionKeys();
			groupPermission = PrivacySettingConstants.GROUP_PERMISSIONS_GROUP.getActionKeys();
		}
		serviceContext.setGuestPermissions(guestPermsission);
		serviceContext.setGroupPermissions(groupPermission);
	}

	/* Creates a TZSRecurrence object based on the event dates values */
	private TZSRecurrence getTZSRecurrenceForEvent(final EventDates eventDates, final TimeZone timeZone, final Locale locale) {
		TZSRecurrence recurrence = null;
		final boolean isRecurrent = Validator.isNotNull(eventDates.getRecurrenceType()) && !eventDates.getRecurrenceType().equals(RecurrenceTypes.NONE);

		if (isRecurrent) {
			final int recurrenceType = eventDates.getRecurrenceType().getType();
			final Calendar recStartCal = eventDates.getStartDate().toCalendar(locale);
			recurrence = new TZSRecurrence(recStartCal, new Duration(1, 0, 0, 0), recurrenceType);
			recurrence.setTimeZone(timeZone);
			recurrence.setWeekStart(Calendar.SUNDAY);

			recurrence.setUntil(eventDates.getRecurrenceEndDate().toCalendar(locale));

			if (recurrenceType == Recurrence.DAILY) {
				recurrence.setInterval(eventDates.getRecurrenceDayInterval());

			} else if (recurrenceType == Recurrence.WEEKLY) {
				recurrence.setInterval(eventDates.getRecurrenceWeekInterval());

				final DayAndPosition[] dayAndPositions = getDayAndPositionArrayFromDaysSelection(eventDates);
				recurrence.setByDay(dayAndPositions);

			} else if (recurrenceType == Recurrence.MONTHLY) {
				recurrence.setInterval(eventDates.getRecurrenceMonthInterval());
				recurrence.setByMonthDay(new int[] { eventDates.getRecurrenceDayInterval() });
			}
		}
		return recurrence;
	}

	private DayAndPosition[] getDayAndPositionArrayFromDaysSelection(final EventDates eventDates) {
		final Collection<DayAndPosition> transform = Lists.newArrayList(Iterables.transform(eventDates.getRecurrenceDaySelectionInterval(),
				new Function<Integer, DayAndPosition>() {

			@Override
			public DayAndPosition apply(final Integer arg0) {
				return new DayAndPosition(arg0, 0);
			}
		}));
		return transform.toArray(new DayAndPosition[transform.size()]);
	}

	public void removeEventbriteFieldsFromEvent(final long companyId, final Long eventId) {
		clearExpandoValue(companyId, eventId, EventExpandoConstants.EVENTBRITE_ID);
		clearExpandoValue(companyId, eventId, EventExpandoConstants.EVENTBRITE_USER_API);
		LOG.debug("Cleared eventbrite details for event with id: " + eventId);
	}

	private void clearExpandoValue(final long companyId, final Long eventId, final String columnName) {
		try {
			final ExpandoValue valueToClear = ExpandoValueLocalServiceUtil.getValue(companyId, CalEvent.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME,
					columnName, eventId);
			valueToClear.setData(StringPool.BLANK);
			ExpandoValueLocalServiceUtil.updateExpandoValue(valueToClear);
		} catch (final SystemException e) {
			LOG.warn("Exception clearing expando value " + columnName + " for event with id: " + eventId + ". " + Throwables.getRootCause(e).getMessage());
		}
	}

}
