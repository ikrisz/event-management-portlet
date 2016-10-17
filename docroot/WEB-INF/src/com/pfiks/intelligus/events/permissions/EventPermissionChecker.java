package com.pfiks.intelligus.events.permissions;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.permission.PortletPermissionUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.calendar.model.CalEvent;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;

/**
 * Utility class to check user permissions to Add, update or remove events
 *
 * @author Ilenia Zedda
 *
 */
@Component
public class EventPermissionChecker {

    private static final Log LOG = LogFactoryUtil.getLog(EventPermissionChecker.class);
    private static final String PORTLET_ID = "events_WAR_eventsmanagementportlet";
    private static final String CALENDAR_MODEL_ID = "com.liferay.portlet.calendar";

    @Resource
    private ConfigurationUtils configUtils;

    public void setConfigUtils(ConfigurationUtils configUtils) {
	this.configUtils = configUtils;
    }

    /**
     * Checks if the event can be modified
     *
     * @param themeDisplay
     * @param event
     * @param configuration
     * @return For liferay events, always returns true For eventbrite events:
     *         returns true only if event is not yet started and the configured
     *         userApiKey matches the one for the event
     */
    public boolean canEventBeModified(ThemeDisplay themeDisplay, EventModel event, EventPortletConfiguration configuration) {
	boolean result = true;
	long companyId = themeDisplay.getCompanyId();
	final boolean eventbriteEnabled = configUtils.isEventbriteEnabled(companyId) && StringUtils.isNotBlank(StringUtils.trimToNull(event.getEventbrite().getEventbriteId()));
	if (eventbriteEnabled) {
	    final boolean canUserUpdateEventbriteEvent = canUserUpdateEventbriteEvent(companyId, event, configuration);
	    final boolean eventAlreadyStarted = eventAlreadyStarted(event);
	    result = canUserUpdateEventbriteEvent && !eventAlreadyStarted;
	}
	return result;
    }

    private boolean eventAlreadyStarted(final EventModel event) {
	boolean result = false;
	result = Validator.isNotNull(event.getEventbrite()) && StringUtils.isNotBlank(event.getEventbrite().getStatus()) && event.getEventbrite().getStatus().equals("started");
	return result;
    }

    private boolean canUserUpdateEventbriteEvent(long companyId, final EventModel event, final EventPortletConfiguration portletConfiguration) {
	final String configuredUserKey = configUtils.getEventbriteUserKey(companyId, portletConfiguration);
	final String eventUserKey = StringUtils.trimToEmpty(event.getEventbrite().getEventbriteUserApiKey());
	return eventUserKey.equalsIgnoreCase(configuredUserKey);
    }

    /**
     * Checks if the logged in user can add a new event
     *
     * @param themeDisplay
     * @return true if the logged in user has ADD permission on the calendar
     *         model.
     */
    public boolean hasAddPermission(final ThemeDisplay themeDisplay) {
	final boolean result = checkPermission(themeDisplay.getPermissionChecker(), themeDisplay.getScopeGroupId(), CALENDAR_MODEL_ID, themeDisplay.getScopeGroupId(),
		ActionKeys.ADD_EVENT);
	LOG.debug("User " + themeDisplay.getUserId() + " in groupId: " + themeDisplay.getScopeGroupId() + " Has add permission? " + result);
	return result;
    }

    public boolean hasAddEventbritePermission(final ThemeDisplay themeDisplay) {
	boolean result = false;
	try {
	    result = PortletPermissionUtil.contains(themeDisplay.getPermissionChecker(), themeDisplay.getPlid(), themeDisplay.getPortletDisplay().getId(), "ADD_EVENTBRITE");
	    LOG.debug("User " + themeDisplay.getUserId() + " in groupId: " + themeDisplay.getScopeGroupId() + " Has add evenrbrite permission? " + result);
	} catch (final NestableException e) {
	    LOG.error(e);
	}
	return result;
    }

    /**
     * Checks if the logged in user can edit the specified event
     *
     * @param themeDisplay
     * @param event
     * @return true if the logged in user has owner permission on the event, if
     *         it is a group admin or if it has edit permission on the event
     */
    public boolean hasEditPermission(final ThemeDisplay themeDisplay, final EventModel event) {
	return contains(themeDisplay.getPermissionChecker(), event, ActionKeys.UPDATE);
    }

    /**
     * Checks if the logged in user can view the specified event
     *
     * @param themeDisplay
     * @param event
     * @return true if the logged in user has owner permission on the event, if
     *         it is a group admin or if it has view permission on the event
     */
    public static boolean hasViewPermission(PermissionChecker permissionChecker, CalEvent event) {
	EventPermissionChecker eventPermissionChecker = new EventPermissionChecker();
	return eventPermissionChecker.contains(permissionChecker, ActionKeys.VIEW, event.getCompanyId(), event.getGroupId(), event.getUserId(), event.getEventId());
    }

    /**
     * Checks if the logged in user can set the event as featured
     *
     */
    public boolean canConfigureFeaturedEvents(final ThemeDisplay themeDisplay) {
	boolean result = false;
	try {
	    result = PortletPermissionUtil.contains(themeDisplay.getPermissionChecker(), themeDisplay.getPlid(), themeDisplay.getPortletDisplay().getId(), "FEATURE");
	    LOG.debug("User " + themeDisplay.getUserId() + " in groupId: " + themeDisplay.getScopeGroupId() + " Has configure featured event permission? " + result);
	} catch (final NestableException e) {
	    LOG.error(e);
	}
	return result;
    }

    /**
     * Checks if the logged in user can delete the specified event
     *
     * @param themeDisplay
     * @param event
     * @return true if the logged in user has owner permission on the event, if
     *         it is a group admin or if it has delete permission on the event
     */
    public boolean hasDeletePermission(final ThemeDisplay themeDisplay, final EventModel event) {
	return contains(themeDisplay.getPermissionChecker(), event, ActionKeys.DELETE);
    }

    private boolean contains(final PermissionChecker permissionChecker, final EventModel event, final String actionId) {
	return contains(permissionChecker, actionId, event.getCompanyId(), event.getGroupId(), event.getUserId(), event.getEventId());
    }

    private boolean contains(final PermissionChecker permissionChecker, final String actionId, long eventCompanyId, long eventGroupId, long eventUserId, long eventId) {
	boolean result = false;
	if (permissionChecker.hasOwnerPermission(eventCompanyId, CalEvent.class.getName(), eventId, eventUserId, actionId)) {
	    result = true;
	} else if (permissionChecker.isGroupAdmin(eventGroupId)) {
	    result = true;
	} else {
	    result = permissionChecker.hasPermission(eventGroupId, CalEvent.class.getName(), eventId, actionId);
	    if (!result) {
		result = checkPermission(permissionChecker, eventGroupId, PORTLET_ID, eventId, actionId);
	    }
	}
	LOG.debug("User " + eventUserId + " for eventId: " + eventId + " Has " + actionId + " permission? " + result);
	return result;
    }

    private boolean checkPermission(final PermissionChecker permissionChecker, final long groupId, final String name, final Long primKey, final String actionId) {
	return permissionChecker.hasPermission(groupId, name, primKey, actionId);
    }

}
