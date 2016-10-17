package com.pfiks.intelligus.events.upgrade;

import java.util.List;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
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
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.startup.ExpandoFieldCreationUtil;
import com.pfiks.intelligus.util.ContentSecurityLevel;

public class Upgrade10_MigratePublicFlagToSecurityLevelField extends UpgradeProcess {
	private static final Log LOG = LogFactoryUtil.getLog(Upgrade10_MigratePublicFlagToSecurityLevelField.class);

	@Override
	public int getThreshold() {
		return 10;
	}

	@Override
	protected void doUpgrade() throws Exception {
		try {
			LOG.info("Running Upgrade10_MigratePublicFlagToSecurityLevelField..");
			final List<Company> companies = CompanyLocalServiceUtil.getCompanies();
			for (final Company company : companies) {
				runAsAdmin(company.getCompanyId());
				migrateExpandoPublicFlagToSecurityLevel(company.getCompanyId());
				deletePublicFlagExpandoColumn(company.getCompanyId());
			}
			LOG.info("Upgrade process completed");
		} catch (final Exception e) {
			LOG.error(e);
			throw new ActionException(e);
		}
	}

	private void deletePublicFlagExpandoColumn(final long companyId) {
		try {
			ExpandoColumnLocalServiceUtil.deleteColumn(companyId, CalEvent.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, EventExpandoConstants.PUBLIC_FLAG);
			LOG.info("Expando column 'public' correctly removed for Events");
		} catch (final Exception e) {
			LOG.error("Exception removing event expando column 'public' - " + e.getMessage());
		}
	}

	private void migrateExpandoPublicFlagToSecurityLevel(final long companyId) throws Exception {
		createExpandoField(companyId);

		final List<ExpandoValue> eventsExpandoValues = ExpandoValueLocalServiceUtil.getColumnValues(companyId, CalEvent.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME,
				EventExpandoConstants.PUBLIC_FLAG, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
		LOG.info("Found " + eventsExpandoValues.size() + " events to migrate for companyId: " + companyId);
		for (final ExpandoValue expandoValue : eventsExpandoValues) {
			updateEventExpandoValue(companyId, expandoValue);
		}
	}

	private void createExpandoField(final long companyId) throws NestableException {
		final ExpandoFieldCreationUtil efcu = new ExpandoFieldCreationUtil();
		efcu.createCalendarEventStringExpando(companyId, EventExpandoConstants.SECURITY_LEVEL);
	}

	private void updateEventExpandoValue(final long companyId, final ExpandoValue expandoValue) {
		try {
			final long eventId = expandoValue.getClassPK();
			final boolean publicEvent = expandoValue.getBoolean();
			ContentSecurityLevel securityLevel = ContentSecurityLevel.GROUP;
			if (publicEvent) {
				securityLevel = ContentSecurityLevel.PUBLIC;
			}
			ExpandoValueLocalServiceUtil.addValue(companyId, CalEvent.class.getName(), ExpandoTableConstants.DEFAULT_TABLE_NAME, EventExpandoConstants.SECURITY_LEVEL, eventId,
					securityLevel.getKey());
			ExpandoValueLocalServiceUtil.deleteExpandoValue(expandoValue);
			LOG.info("Migrated eventId: " + eventId + ". Public? " + publicEvent);
		} catch (final NestableException e) {
			LOG.error("Exception migrating event expandoValue primaryKey: " + expandoValue.getPrimaryKey() + " - " + e.getMessage());
		}
	}

	private void runAsAdmin(final long companyId) throws Exception {
		final Role adminRole = RoleLocalServiceUtil.getRole(companyId, RoleConstants.ADMINISTRATOR);
		final List<User> adminUsers = UserLocalServiceUtil.getRoleUsers(adminRole.getRoleId());
		if (adminUsers.size() > 0) {
			PrincipalThreadLocal.setName(adminUsers.get(0).getUserId());
			final PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(adminUsers.get(0));
			PermissionThreadLocal.setPermissionChecker(permissionChecker);
		} else {
			LOG.warn("Unable to run as administrator.");
		}
	}

}
