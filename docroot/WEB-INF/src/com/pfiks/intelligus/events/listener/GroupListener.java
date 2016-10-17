package com.pfiks.intelligus.events.listener;

import java.util.List;

import com.liferay.portal.ModelListenerException;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portal.model.Group;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;

public class GroupListener extends BaseModelListener<Group> {
    private static final Log LOG = LogFactoryUtil.getLog(GroupListener.class);

    @Override
    public void onAfterRemove(Group model) throws ModelListenerException {
	super.onAfterRemove(model);
	removeEventsLinkedToGroup(model.getGroupId());
    }

    private void removeEventsLinkedToGroup(Long groupId) {
	LOG.debug("Removing events linked to groupId: " + groupId);
	try {
	    DynamicQuery query = DynamicQueryFactoryUtil.forClass(CalEvent.class, PortalClassLoaderUtil.getClassLoader());
	    query.add(RestrictionsFactoryUtil.eq("groupId", groupId));
	    List<CalEvent> eventsToRemove = CalEventLocalServiceUtil.dynamicQuery(query);
	    for (final CalEvent event : eventsToRemove) {
		removeEvent(event);
	    }
	    LOG.debug("Events correctly removed");
	} catch (final SystemException e) {
	    LOG.warn("Exception retrieving events to remove for groupId: " + groupId + ". " + e.getMessage());
	}
    }

    private void removeEvent(final CalEvent event) {
	try {
	    CalEventLocalServiceUtil.deleteCalEvent(event);
	} catch (final NestableException e) {
	    LOG.warn("Exception deleting event: " + e.getMessage());
	}
    }

}
