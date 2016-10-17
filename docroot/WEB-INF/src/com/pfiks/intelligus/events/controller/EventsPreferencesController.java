package com.pfiks.intelligus.events.controller;

import java.util.Set;

import javax.annotation.Resource;
import javax.portlet.ActionRequest;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import com.google.common.collect.Sets;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.Validator;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;

/**
 * Controller to manage portlet preferences (portlet EDIT view)
 *
 * @author Ilenia Zedda
 *
 */
@Controller
@RequestMapping("EDIT")
public class EventsPreferencesController {

    private static final Log LOG = LogFactoryUtil.getLog(EventsPreferencesController.class);

    @Resource
    private ConfigurationUtils configUtils;

    public void setConfigUtils(final ConfigurationUtils configUtils) {
	this.configUtils = configUtils;
    }

    private final  Set<String> availableScopes;
    private final Set<String> availableViewModes;


    public EventsPreferencesController() {
	availableScopes = Sets.newHashSet(ConfigurationConstants.SCOPE_GROUP, ConfigurationConstants.SCOPE_PUBLIC);
	availableViewModes = Sets.newHashSet(ConfigurationConstants.VIEW_LIST, ConfigurationConstants.VIEW_CALENDAR);
    }

    @RenderMapping
    public String view(final RenderRequest request, final PortletPreferences portletPreferences, final Model model) throws NestableException {
	model.addAttribute("availableScopes", availableScopes);
	model.addAttribute("availableViewModes",availableViewModes);

	EventPortletConfiguration configuration = (EventPortletConfiguration) request.getAttribute("configuration");
	if (configuration == null) {
	    configuration = new EventPortletConfiguration(portletPreferences);
	}
	long companyId =configUtils.getCompanyId(request);
	model.addAttribute("eventConfiguration", configuration);
	model.addAttribute("eventbriteEnabled", configUtils.isEventbriteEnabled(companyId));

	model.addAttribute("isGuestGroup", configUtils.isGuestGroup(request));

	return "configuration/view";
    }

    @ActionMapping
    public void updatePreferences(final ActionRequest request, final PortletPreferences preferences) throws EventException, NestableException {
	LOG.debug("Updating portlet preferences...");
	final EventPortletConfiguration configuration = new EventPortletConfiguration(request);
	if (validConfiguration(request, configuration)) {
	    configuration.savePreferences(preferences);
	    SessionMessages.add(request, "preferences-updated");
	    LOG.debug("Portlet preferences correctly saved");
	} else {
	    LOG.debug("Portlet preferences invalid");
	    request.setAttribute("configuration", configuration);
	}
    }

    private boolean validConfiguration(final ActionRequest request, final EventPortletConfiguration config) throws NestableException {
	boolean result = true;
	if (isInvalidNumber(config.getMaxEventsToShow())) {
	    SessionErrors.add(request, "configuration.maxEventsToShow-invalid");
	    result = false;
	}
	if (config.getIncludeFeaturedEvents() && isInvalidNumber(config.getMaxFeaturedEventsToShow())) {
	    SessionErrors.add(request, "configuration.maxFeaturedEventsToShow-invalid");
	    result = false;
	}
	if (configUtils.isGuestGroup(request) && config.getConfiguredScope().equals(ConfigurationConstants.SCOPE_GROUP)) {
	    SessionErrors.add(request, "configuration.scopeGuestGroup-invalid");
	    result = false;
	}

	return result;
    }


    private boolean isInvalidNumber(final Integer numberToValidate) {
	return Validator.isNull(numberToValidate) || numberToValidate <= 0;
    }

}
