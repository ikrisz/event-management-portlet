package com.pfiks.intelligus.events.startup;

import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.events.SimpleAction;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;

/**
 * Called on startup, it will create the required expando fields, velocity
 * templates and web content articles. If any of these already exist, they won't
 * be updated
 *
 * @author Ilenia Zedda
 *
 */
public class EventsStartupAction extends SimpleAction {

    private static final Log LOG = LogFactoryUtil.getLog(EventsStartupAction.class);

    @Override
    public void run(final String[] ids) throws ActionException {
	try {
	    LOG.debug("Intelligus Events app - Running initial setup action....");
	    for (final String companyIdVal : ids) {
		final Long companyId = GetterUtil.getLong(companyIdVal);
		createExpandoFieldsForCalEvent(companyId);
		createWebContent(companyId);
		ConfigurationUtils.initializeEventbriteConfiguration(companyId);
	    }
	    LOG.debug("Intelligus Events app - setup completed");
	} catch (final NestableException e) {
	    LOG.error("Exception running startUp action", e);
	    throw new ActionException(e);
	}
    }

    private void createWebContent(final long companyId) throws NestableException {
	final WebContentCreationUtil wccu = new WebContentCreationUtil(companyId);
	wccu.createMissingWebContentArticle(ConfigurationConstants.ERROR_EVENT_NOT_FOUND_PAGE_ARTICLE_ID);
	wccu.createMissingWebContentArticle(ConfigurationConstants.ERROR_PERMISSION_PAGE_ARTICLE_ID);
	wccu.createMissingWebContentArticle(ConfigurationConstants.ERROR_EXCEPTION_PAGE_ARTICLE_ID);
	wccu.createMissingWebContentArticle(ConfigurationConstants.ERROR_EVENTBRITE_PAGE_ARTICLE_ID);
	wccu.createMissingWebContentArticle(ConfigurationConstants.ERROR_SYNC_EVENT);
	wccu.createMissingWebContentArticle(ConfigurationConstants.INVITE_USERS_ARTICLE_ID);
	wccu.createMissingWebContentArticle(ConfigurationConstants.MESSAGE_USER_ARTICLE_ID);
    }

    private void createExpandoFieldsForCalEvent(final long companyId) throws NestableException {
	final ExpandoFieldCreationUtil efcu = new ExpandoFieldCreationUtil();

	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.SECURITY_LEVEL);
	efcu.createCalendarEventLongExpando(companyId, EventExpandoConstants.NETWORK_ID);

	efcu.createCalendarEventBooleanExpando(companyId, EventExpandoConstants.FEATURED);
	efcu.createCalendarEventBooleanExpando(companyId, EventExpandoConstants.ONLINE_EVENT);

	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.SUMMARY);

	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.VENUE);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.ADDRESS_1);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.ADDRESS_2);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.CITY);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.REGION_STATE);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.ZIP_CODE);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.COUNTRY);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.EVENTBRITE_ID);
	efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.EVENTBRITE_USER_API);
    }

}
