package com.pfiks.intelligus.events.model;

import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;

import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;

/**
 * Model to represent the portlet preferences.
 *
 * @author Ilenia Zedda
 *
 */
public class EventPortletConfiguration {

    private static final Log LOG = LogFactoryUtil.getLog(EventPortletConfiguration.class);

    // Portlet general settings
    private final String configuredScope;
    private final String configuredViewMode;
    private final Integer maxEventsToShow;

    // Featured events settings
    private final Boolean includeFeaturedEvents;
    private final Integer maxFeaturedEventsToShow;

    // Eventbrite fields
    private final String customEventbriteUserKey;

    private final Boolean liferayCreationDisabled;

    public EventPortletConfiguration(final PortletPreferences portletPreferences) {
	configuredScope = portletPreferences.getValue(ConfigurationConstants.CONFIG_SCOPE, ConfigurationConstants.DEFAULT_CONFIG_SCOPE);

	configuredViewMode = portletPreferences.getValue(ConfigurationConstants.CONFIG_VIEW_MODE, ConfigurationConstants.DEFAULT_CONFIG_VIEW_MODE);

	maxEventsToShow = Integer.valueOf(portletPreferences.getValue(ConfigurationConstants.MAX_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER));

	includeFeaturedEvents = Boolean.valueOf(portletPreferences.getValue(ConfigurationConstants.SHOW_FEATURED_EVENTS, ConfigurationConstants.DEFAULT_FEATURED_ENABLED));

	maxFeaturedEventsToShow = Integer.valueOf(portletPreferences.getValue(ConfigurationConstants.MAX_FEATURED_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER));

	customEventbriteUserKey = portletPreferences.getValue(ConfigurationConstants.EVENTBRITE_USER_KEY, StringPool.BLANK);

	liferayCreationDisabled = Boolean.valueOf(portletPreferences.getValue(ConfigurationConstants.DISABLE_LIFERAY_CREATION,
		ConfigurationConstants.DEFAULT_LIFERAY_CREATION_DISABLE));
    }

    public EventPortletConfiguration(final ActionRequest request) {
	configuredScope = ParamUtil.getString(request, "eventsScope", ConfigurationConstants.CONFIG_SCOPE);
	configuredViewMode = ParamUtil.getString(request, "eventsViewMode", ConfigurationConstants.CONFIG_VIEW_MODE);
	maxEventsToShow = ParamUtil.getInteger(request, "maxEvents", Integer.valueOf(ConfigurationConstants.DEFAULT_CONFIG_NUMBER));

	includeFeaturedEvents = ParamUtil.getBoolean(request, "includeFeatured", Boolean.valueOf(ConfigurationConstants.DEFAULT_FEATURED_ENABLED));
	maxFeaturedEventsToShow = ParamUtil.getInteger(request, "maxFeaturedEvents", Integer.valueOf(ConfigurationConstants.DEFAULT_CONFIG_NUMBER));

	customEventbriteUserKey = StringUtils.trimToEmpty(ParamUtil.getString(request, "eventbriteUserKey", StringPool.BLANK));

	liferayCreationDisabled = ParamUtil.getBoolean(request, "liferayCreationDisabled", Boolean.valueOf(ConfigurationConstants.DEFAULT_LIFERAY_CREATION_DISABLE));
    }

    public void savePreferences(final PortletPreferences portletPreferences) throws EventException {
	try {
	    portletPreferences.setValue(ConfigurationConstants.CONFIG_SCOPE, configuredScope);
	    portletPreferences.setValue(ConfigurationConstants.CONFIG_VIEW_MODE, configuredViewMode);
	    portletPreferences.setValue(ConfigurationConstants.MAX_EVENTS, String.valueOf(maxEventsToShow));

	    portletPreferences.setValue(ConfigurationConstants.SHOW_FEATURED_EVENTS, String.valueOf(includeFeaturedEvents));
	    portletPreferences.setValue(ConfigurationConstants.MAX_FEATURED_EVENTS, String.valueOf(maxFeaturedEventsToShow));

	    portletPreferences.setValue(ConfigurationConstants.EVENTBRITE_USER_KEY, customEventbriteUserKey);

	    portletPreferences.setValue(ConfigurationConstants.DISABLE_LIFERAY_CREATION, String.valueOf(liferayCreationDisabled));

	    portletPreferences.store();
	} catch (final Exception e) {
	    LOG.error("Exception while saving portlet preferences: " + e.getMessage());
	    throw new EventException("Exception while saving portlet preferences", e);
	}
    }

    public String getConfiguredScope() {
	return configuredScope;
    }

    public String getConfiguredViewMode() {
	return configuredViewMode;
    }

    public Integer getMaxEventsToShow() {
	return maxEventsToShow;
    }

    public Boolean getIncludeFeaturedEvents() {
	return includeFeaturedEvents;
    }

    public Integer getMaxFeaturedEventsToShow() {
	return maxFeaturedEventsToShow;
    }

    public String getCustomEventbriteUserKey() {
	return customEventbriteUserKey;
    }

    public Boolean getLiferayCreationDisabled() {
	return liferayCreationDisabled;
    }

}
