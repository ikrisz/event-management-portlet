package com.pfiks.intelligus.events.utils;

import java.util.Arrays;
import java.util.Map;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;

@Component
public class ConfigurationUtils {

    private static final Log LOG = LogFactoryUtil.getLog(ConfigurationUtils.class);

    private static final Map<Long, Boolean> companyEventbriteEnabled = Maps.newHashMap();
    private static final Map<Long, String> companyEventbriteAppKey = Maps.newHashMap();
    private static final Map<Long, String> companyEventbriteUserKey = Maps.newHashMap();
    private static final Map<Long, String[]> companyEventbriteAdminRoles = Maps.newHashMap();
    private static final Map<Long, Long> companyExpandoColumnEventbriteId = Maps.newHashMap();
    private static final Map<Long, Long> companyExpandoColumnEventbriteUserApiKey = Maps.newHashMap();

    private final long eventClassNameId;

    public ConfigurationUtils() {
	eventClassNameId = ClassNameLocalServiceUtil.getClassNameId(CalEvent.class);
    }

    public long getEventClassNameId() {
	return eventClassNameId;
    }

    public boolean isGuestGroup(PortletRequest request) throws NestableException {
	final ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
	return getGuestGroupId(themeDisplay.getCompanyId()) == themeDisplay.getScopeGroupId();
    }

    public boolean isGuestGroup(ThemeDisplay themeDisplay) throws NestableException {
	return getGuestGroupId(themeDisplay.getCompanyId()) == themeDisplay.getScopeGroupId();
    }

    public long getCompanyId(PortletRequest request) {
	return PortalUtil.getCompanyId(request);
    }

    public static final void initializeEventbriteConfiguration(long companyId) throws SystemException {
	String eventbriteEnabledValue = PrefsPropsUtil.getString(companyId, "eventbrite.enabled", StringPool.FALSE);
	String appKey = PrefsPropsUtil.getString(companyId, "eventbrite.appkey", StringPool.BLANK);
	String userKey = PrefsPropsUtil.getString(companyId, "eventbrite.api.userkey", StringPool.BLANK);
	String roleValues = PrefsPropsUtil.getString(companyId, "event.import.error.emails.admin.to.roles", StringPool.BLANK);
	boolean eventbriteEnabled = Boolean.valueOf(eventbriteEnabledValue);
	String[] adminRoles = StringUtils.split(roleValues, StringPool.COMMA);

	LOG.info("Eventbrite configuration for companyId: " + companyId + " - eventbriteEnabled? "+ eventbriteEnabled + ", appKey: " + appKey + ", userKey: " + userKey + ", configuredRoles: " + Iterables.toString(Arrays.asList(adminRoles)));
	validateEventbriteConfiguration(eventbriteEnabled, appKey, userKey, adminRoles);

	companyEventbriteEnabled.put(companyId, eventbriteEnabled);
	if (eventbriteEnabled) {
	    companyEventbriteAppKey.put(companyId, appKey);
	    companyEventbriteUserKey.put(companyId, userKey);
	    companyEventbriteAdminRoles.put(companyId, adminRoles);

	    long expandoColumnIdUserKey = ExpandoColumnLocalServiceUtil.getColumn(companyId, CalEvent.class.getName(),
		    ExpandoTableConstants.DEFAULT_TABLE_NAME, EventExpandoConstants.EVENTBRITE_USER_API).getColumnId();
	    companyExpandoColumnEventbriteUserApiKey.put(companyId, expandoColumnIdUserKey);

	    long expandoColumnIdEventbriteId = ExpandoColumnLocalServiceUtil.getColumn(companyId, CalEvent.class.getName(),
		    ExpandoTableConstants.DEFAULT_TABLE_NAME, EventExpandoConstants.EVENTBRITE_ID).getColumnId();
	    companyExpandoColumnEventbriteId.put(companyId, expandoColumnIdEventbriteId);
	}
    }

    private static void validateEventbriteConfiguration(boolean eventbriteEnabled, String appKey, String userKey, String[] adminRoles) throws IllegalStateException {
	if (eventbriteEnabled) {
	    boolean invalidAppKey = StringUtils.isBlank(StringUtils.trimToEmpty(appKey));
	    boolean invalidUserKey = StringUtils.isBlank(StringUtils.trimToEmpty(userKey));
	    boolean invalidRoles = ArrayUtils.isEmpty(adminRoles);
	    if (invalidAppKey || invalidUserKey || invalidRoles) {
		throw new IllegalStateException("Invalid eventbrite configuration. If eventbrite is enabled, 'eventbrite.appkey', 'eventbrite.api.userkey' and 'event.import.error.emails.admin.to.roles' are required. Please configured your portal-<companyWebId>.properties correctly.");
	    }
	}
    }

    /**
     * If a custom value is set in PortletPreferences it will be returns,
     * otherwise the value configured in portlet.properties is returned
     *
     * @return the configured User API Key for the portlet.
     * @throws SystemException
     */
    public String getEventbriteUserKey(long companyId, final EventPortletConfiguration configuration) {
	if (Validator.isNotNull(configuration)) {
	    final String customUserKey = configuration.getCustomEventbriteUserKey();
	    if (StringUtils.isNotBlank(StringUtils.trimToNull(customUserKey))) {
		return customUserKey;
	    }
	}
	return companyEventbriteUserKey.get(companyId);
    }

    public String getEventbriteApplicationKey(long companyId) {
	return companyEventbriteAppKey.get(companyId);
    }

    /**
     * Creates a dynamicQuery for ExpandoValue table Projection = data
     * Restriction on classNameId = CalEvent Restriction on columnId =
     * eventbrite-id
     *
     * @return DynamicQuery that will retrieve all values saved in ExpandoValue
     *         table for the eventbrite-id column
     * @throws SystemException
     */
    public DynamicQuery getQueryForEventbriteIdExpandoValueData(long companyId) {
	final DynamicQuery expandoQuery = DynamicQueryFactoryUtil.forClass(ExpandoValue.class, PortalClassLoaderUtil.getClassLoader());
	expandoQuery.setProjection(ProjectionFactoryUtil.property("data"));
	expandoQuery.add(RestrictionsFactoryUtil.eq("classNameId", eventClassNameId));
	expandoQuery.add(RestrictionsFactoryUtil.eq("columnId", getExpandoColumEventbriteId(companyId)));
	return expandoQuery;
    }

    /**
     * Creates a dynamicQuery for ExpandoValue table Projection = data
     * Restriction on classNameId = CalEvent Restriction on columnId =
     * eventbrite-user-api-key
     *
     * @return DynamicQuery that will retrieve all values saved in ExpandoValue
     *         table for the eventbrite-user-api-key column
     * @throws SystemException
     */
    public DynamicQuery getQueryForEventbriteUserApiKeyExpandoValueData(long companyId) {
	final DynamicQuery expandoQuery = DynamicQueryFactoryUtil.forClass(ExpandoValue.class, PortalClassLoaderUtil.getClassLoader());
	expandoQuery.setProjection(ProjectionFactoryUtil.property("data"));
	expandoQuery.add(RestrictionsFactoryUtil.eq("classNameId", eventClassNameId));
	expandoQuery.add(RestrictionsFactoryUtil.eq("columnId", getExpandoColumEventbriteUserKey(companyId)));
	return expandoQuery;
    }

    public long getGlobalGroupId(long companyId) throws NestableException {
	return GroupLocalServiceUtil.getCompanyGroup(companyId).getGroupId();
    }

    public long getGuestGroupId(long companyId) throws NestableException {
	return GroupLocalServiceUtil.getGroup(companyId, GroupConstants.GUEST).getGroupId();
    }

    public Boolean isEventbriteEnabled(long companyId) {
	return companyEventbriteEnabled.get(companyId);
    }

    public String[] getEventbriteAdminRoles(long companyId) {
	return companyEventbriteAdminRoles.get(companyId);
    }

    public long getExpandoColumEventbriteUserKey(long companyId) {
	return companyExpandoColumnEventbriteUserApiKey.get(companyId);
    }

    public long getExpandoColumEventbriteId(long companyId) {
	return companyExpandoColumnEventbriteId.get(companyId);
    }

}
