package com.pfiks.intelligus.events.social;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.calendar.NoSuchEventException;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;
import com.liferay.portlet.social.model.SocialActivity;
import com.liferay.portlet.social.model.SocialActivityFeedEntry;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.permissions.EventPermissionChecker;
import com.pfiks.intelligus.networks.portal.service.NetworkGroupLocalServiceUtil;
import com.pfiks.intelligus.networks.portal.service.NetworkUserLocalServiceUtil;
import com.pfiks.intelligus.util.ContentSecurityLevel;
import com.pfiks.intelligus.util.IntelligusUtil;
import com.pfiks.intelligus.util.social.IntelligusActivityInterpreter;

public class CalEventActivityInterpreter extends IntelligusActivityInterpreter {
	private static final Log LOG = LogFactoryUtil.getLog(CalEventActivityInterpreter.class);

	private static final String[] _CLASS_NAMES = new String[] { CalEvent.class.getName() };

	@Override
	public String getPortletId() {
		return PortletKeys.CALENDAR;
	}

	@Override
	public String[] getClassNames() {
		return _CLASS_NAMES;
	}

	@Override
	public SocialActivityFeedEntry interpret(final SocialActivity activity, final ServiceContext serviceContext) {
		try {
			return interpretActivity(activity, serviceContext);
		} catch (final Exception e) {
			LOG.warn(e);
			return null;
		}
	}

	@Override
	public SocialActivityFeedEntry doInterpret(final SocialActivity activity, final ServiceContext serviceContext) throws Exception {
		return interpretActivity(activity, serviceContext);
	}

	private SocialActivityFeedEntry interpretActivity(final SocialActivity activity, final ServiceContext serviceContext) throws Exception {
		final CalEvent event = getEvent(activity);
		if (Validator.isNull(event)) {
			return null;
		}

		final PermissionChecker permissionChecker = getPermissionChecker(serviceContext, serviceContext.getThemeDisplay());

		final Group activityGroup = getActivityGroup(activity);

		if (!showEventToUser(serviceContext, permissionChecker, activityGroup, event)) {
			return null;
		}

		final String creatorUserName = getActivityCreatorUserName(activity, serviceContext);
		String receiverUserName = getActivityReceiverUserName(activity, serviceContext);
		final boolean isGroupMember = isGroupMember(serviceContext.getUserId(), activityGroup.getGroupId());
		final String groupName = getActivityGroupName(activity, serviceContext);

		String titlePattern = null;
		final int activityType = activity.getType();
		if (activityType == CalEventActivityKeys.ADD_EVENT) {
			titlePattern = "activity-calendar-add-event";
		} else if (activityType == CalEventActivityKeys.UPDATE_EVENT) {
			titlePattern = "activity-calendar-update-event";
		} else if (activityType == CalEventActivityKeys.ADD_COMMENT) {
			titlePattern = "activity-calendar-add-comment";
		} else if (activityType == CalEventActivityKeys.LIKE) {
			if (Validator.isNull(receiverUserName)) {
				receiverUserName = getUserName(event.getUserId(), serviceContext);
			}
			titlePattern = "activity-calendar-like-event";
		}

		if (Validator.isNull(titlePattern)) {
			LOG.warn("Unable to find a title pattern for CalEvent activityId: " + activity.getActivityId() + ", activityType: " + activity.getType());
			return null;
		}

		if (Validator.isNotNull(groupName)) {
			titlePattern += "-in";
		}

		String eventTitle = getJSONValue(activity.getExtraData(), "title", event.getTitle());
		String link = StringPool.BLANK;
		if (isGroupMember) {
			final Group calEventGroup = GroupLocalServiceUtil.getGroup(event.getGroupId());
			link = getLinkForEvent(activity, serviceContext, calEventGroup);
			eventTitle = wrapLink(link, eventTitle);
		}

		final Object[] titleArguments = new Object[] { groupName, creatorUserName, eventTitle, receiverUserName };

		final String title = serviceContext.translate(titlePattern, titleArguments);

		return new SocialActivityFeedEntry(link, title, StringPool.BLANK);
	}

	private CalEvent getEvent(final SocialActivity activity) throws PortalException, SystemException {
		try {
			return CalEventLocalServiceUtil.getEvent(activity.getClassPK());
		} catch (final NoSuchEventException e) {
			LOG.debug("No event found for SocialActivity classPK: " + activity.getClassPK());
			return null;
		}
	}

	/*
	 * Do NOT show event to user if:
	 * - event is in the GUEST Group
	 * - event's group is PRIVATE and user does not have viewPermission
	 * - event's SecurityLevel is NETWORK and user does not have access to the network
	 * - event's group is an intranet group and user does not have access to the network intranet (No matter the event's SecurityLevel, never show intranet content to non-intranet users)
	 */
	private boolean showEventToUser(final ServiceContext serviceContext, final PermissionChecker permissionChecker, final Group activityGroup, final CalEvent event)
			throws NestableException {

		if (isActivityInGuestGroup(serviceContext.getCompanyId(), activityGroup)) {
			LOG.debug("CalEventInterpreter - Skipping event: " + event.getEventId() + " as it is in the Guest group");
			return false;
		}

		if (isGroupPrivate(activityGroup) && !EventPermissionChecker.hasViewPermission(permissionChecker, event)) {
			LOG.debug("CalEventInterpreter - Skipping event: " + event.getEventId() + " as it is in a private group and user does not have view permissions");
			return false;
		}

		final String securityLevel = GetterUtil.getString(event.getExpandoBridge().getAttribute(EventExpandoConstants.SECURITY_LEVEL, false), "");
		final long networkId = getNetworkId(activityGroup, event);
		if (securityLevel.equalsIgnoreCase(ContentSecurityLevel.NETWORK.getKey())
				&& !NetworkUserLocalServiceUtil.doesUserHaveAccessToNetwork(serviceContext.getUserId(), networkId)) {
			LOG.debug("CalEventInterpreter - Skipping event: " + event.getEventId()
					+ " as it has NETWORK security and user does not have access to network. networkId: " + networkId);
			return false;
		} else if (!NetworkUserLocalServiceUtil.doesUserHaveAccessToGroupIntranetNetwork(serviceContext.getUserId(), activityGroup.getGroupId())) {
			LOG.debug("CalEventInterpreter - Skipping event: " + event.getEventId()
					+ " as group is intranet group and user does not have access to network intranet. networkId: " + networkId + " groupId: "
					+ activityGroup.getGroupId());
			return false;
		}

		return true;
	}

	private long getNetworkId(final Group activityGroup, final CalEvent event) {
		long groupNetworkId = 0L;
		try {
			groupNetworkId = NetworkGroupLocalServiceUtil.getGroupNetworkId(activityGroup.getGroupId());
		} catch (final Exception e) {
			//
		}
		final long eventNetworkId = GetterUtil.getLong(event.getExpandoBridge().getAttribute(EventExpandoConstants.NETWORK_ID, false), groupNetworkId);
		return eventNetworkId;
	}

	private String getLinkForEvent(final SocialActivity activity, final ServiceContext serviceContext, final Group calEventGroup) throws NestableException {
		String link = serviceContext.getPortalURL();

		if ((calEventGroup.getType() == GroupConstants.TYPE_SITE_RESTRICTED || calEventGroup.getType() == GroupConstants.TYPE_SITE_PRIVATE)
				&& !GroupLocalServiceUtil.hasUserGroup(serviceContext.getUserId(), calEventGroup.getGroupId())) {
			final Group guestGroup = IntelligusUtil.getGuestGroupForCompany(serviceContext.getCompanyId());

			link = link + "/web" + guestGroup.getFriendlyURL() + "/events";
		} else {
			link = link + "/group" + calEventGroup.getFriendlyURL() + "/events";
		}

		link = link + "/-/event/view/" + activity.getClassPK();
		return link;
	}

}