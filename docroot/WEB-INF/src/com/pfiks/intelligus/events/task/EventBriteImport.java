package com.pfiks.intelligus.events.task;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.liferay.portal.NoSuchRoleException;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.pfiks.intelligus.events.service.IEventService;
import com.pfiks.intelligus.events.service.INotificationService;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;

@Component
public class EventBriteImport {
    private static final Logger LOG = Logger.getLogger(EventBriteImport.class);

    @Resource
    private IEventService eventService;

    @Resource
    private INotificationService notificationService;

    @Resource
    private ConfigurationUtils configurationUtils;

    public void importEventBriteEvents() {
	try{
	    List<Company> companies = CompanyLocalServiceUtil.getCompanies();
	    for (Company company : companies) {
		syncrhonizeEvents(company.getCompanyId());
	    }
	} catch (final Exception e) {
	    LOG.error("Exception retrieving companies to sync eventbrite.", e);
	}
    }

    private void syncrhonizeEvents(long companyId) {
	Boolean eventbriteEnabled = configurationUtils.isEventbriteEnabled(companyId);
	LOG.info("Sync eventbrite events for companyId: " + companyId + " - eventbriteEnabled? " + eventbriteEnabled);
	if (eventbriteEnabled) {
	    try {
		final long syncStartTime = System.currentTimeMillis();
		runAsAdmin(companyId);
		final List<ExpandoValue> eventsToRetrieve = getEventsToRetrieve(companyId);
		LOG.info("Synchonizing eventBrite events for companyId: " + companyId + ". Found " + eventsToRetrieve.size() + " CalEvents to sync");
		if (eventsToRetrieve != null && !eventsToRetrieve.isEmpty()) {
		    final List<String> exceptions = Lists.newArrayList();
		    for (final ExpandoValue expandoValue : eventsToRetrieve) {
			final String eventBriteId = expandoValue.getData();
			final long eventId = expandoValue.getClassPK();
			if (StringUtils.isNotBlank(eventBriteId)) {
			    syncEvent(eventId, eventBriteId, exceptions);
			} else {
			    LOG.info("Skipping eventId: " + eventId + " as eventbriteId is empty.");
			    deleteEmptyExpandoEventbriteId(expandoValue);
			}
		    }

		    if (!exceptions.isEmpty()) {
			LOG.info("Errors occurred whily sync. Notifying admins");
			final String content = Joiner.on("<br/>").skipNulls().join(exceptions);
			notificationService.sendImportErrorNotification(companyId, getAdminEmails(companyId), content);
		    }
		}
		final long syncEndTime = System.currentTimeMillis() - syncStartTime;
		final long syncDuration = syncEndTime / 1000;
		LOG.info("Synchronization completed for companyId: " + companyId + ". took :" + syncDuration + " seconds.");
	    } catch (final Exception e) {
		LOG.error("Exception retrieving events to sync with eventbrite.", e);
	    }
	}
    }

    private InternetAddress[] getAdminEmails(final long companyId) {
	final Set<User> results = Sets.newHashSet();
	String[] adminEmailReceiverRoles = configurationUtils.getEventbriteAdminRoles(companyId);
	for (String roleName : adminEmailReceiverRoles) {
	    try {
		Role role = RoleLocalServiceUtil.getRole(companyId, roleName);
		final List<User> roleUsers = UserLocalServiceUtil.getRoleUsers(role.getRoleId());
		results.addAll(roleUsers);
	    } catch (final NoSuchRoleException e) {
		LOG.warn("No role found with name " + roleName, e);
	    } catch (final NestableException e) {
		LOG.warn(e);
	    }
	}

	final FluentIterable<InternetAddress> toAddresses = FluentIterable.from(results).transform(new Function<User, InternetAddress>() {
	    @Override
	    public InternetAddress apply(final User user) {
		try {
		    return new InternetAddress(user.getEmailAddress(), user.getFullName());
		} catch (final Exception e) {
		    LOG.error("Invalid email address for userId: " + user.getUserId(), e);
		    return null;
		}
	    }
	}).filter(Predicates.notNull());

	return toAddresses.toArray(InternetAddress.class);
    }

    private void deleteEmptyExpandoEventbriteId(ExpandoValue expandoValue) {
	try {
	    ExpandoValueLocalServiceUtil.deleteExpandoValue(expandoValue);
	} catch (final SystemException e) {
	    LOG.warn("Exception removing empty expando value for event." + e.getMessage());
	}
    }

    private void runAsAdmin(long companyId) throws Exception {
	Role adminRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.ADMINISTRATOR);
	List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());
	if (adminUsers.size() > 0) {
	    LOG.debug("Running EventBriteImport as Administrator");

	    PrincipalThreadLocal.setName(adminUsers.get(0).getUserId());
	    PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(adminUsers.get(0));
	    PermissionThreadLocal.setPermissionChecker(permissionChecker);
	} else {
	    LOG.debug("Unable to run EventBriteImport as administrator.");
	}
    }


    /*
     * Get all ExpandoValues that have classNameId = CalEvent and have the
     * eventbriteId column set
     */
    @SuppressWarnings("unchecked")
    private List<ExpandoValue> getEventsToRetrieve(long companyId) throws SystemException {
	final DynamicQuery expandoQuery = DynamicQueryFactoryUtil.forClass(ExpandoValue.class, PortalClassLoaderUtil.getClassLoader());
	// Get only events
	expandoQuery.add(RestrictionsFactoryUtil.eq("classNameId", configurationUtils.getEventClassNameId()));
	// That have expandoColumn eventBriteId set
	expandoQuery.add(RestrictionsFactoryUtil.eq("columnId", configurationUtils.getExpandoColumEventbriteId(companyId)));
	return ExpandoValueLocalServiceUtil.dynamicQuery(expandoQuery);
    }

    private void syncEvent(long eventId, String eventBriteId, List<String> syncExceptions) {
	try {
	    final String syncEventFromEventbrite = eventService.syncEventFromEventbrite(eventService.getCalEvent(eventId), eventBriteId);
	    if (StringUtils.isNotBlank(syncEventFromEventbrite)) {
		syncExceptions.add("eventId: " + eventId + " - eventBriteId: " + eventBriteId + " . Exception message: " + syncEventFromEventbrite);
	    }
	} catch (final Exception e) {
	    syncExceptions.add("eventId: " + eventId + " - eventBriteId: " + eventBriteId + " . Exception message: " + e.getMessage());
	}
    }

}