package com.pfiks.intelligus.events.startup;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.expando.DuplicateColumnNameException;
import com.liferay.portlet.expando.model.ExpandoColumn;
import com.liferay.portlet.expando.model.ExpandoColumnConstants;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.pfiks.intelligus.portal.service.CustomExpandoFieldsLocalServiceUtil;

/**
 * Utility class to create expando fields
 *
 * @author Ilenia Zedda
 *
 */
public class ExpandoFieldCreationUtil {

    private final static String[] guestPermissions = new String[] { ActionKeys.VIEW };
    private final static String[] userPermissions = new String[] { ActionKeys.VIEW, ActionKeys.UPDATE };
    private final static String visibleField = "index-type=2\nhidden=0\nvisible-with-update-permission=1";

    public ExpandoFieldCreationUtil() {
	//
    }

    /**
     * Creates a new expando field for CalEvent model. Field type: Boolean Field
     * default value: False
     *
     * Field permissions: -Guest = VIEW -User = VIEW + UPDATE
     *
     * Field will be searchable as keywork Field will be visibile with update,
     * and NOT hidden
     *
     * @param fieldName
     * @throws NestableException
     */
    public void createCalendarEventBooleanExpando(final long companyId, final String fieldName) throws NestableException {
	createExpandoField(companyId, fieldName, ExpandoColumnConstants.BOOLEAN, Boolean.FALSE);
    }

    /**
     * Creates a new expando field for CalEvent model. Field type: String
     *
     * Field permissions: -Guest = VIEW -User = VIEW + UPDATE
     *
     * Field will be searchable as keywork Field will be visibile with update,
     * and NOT hidden
     *
     * @param fieldName
     * @throws NestableException
     */
    public void createCalendarEventStringExpando(final long companyId, final String fieldName) throws NestableException {
	createExpandoField(companyId, fieldName, ExpandoColumnConstants.STRING, StringPool.BLANK);
    }

    /**
     * Creates a new expando field for CalEvent model. Field type: Long
     *
     * Field permissions: -Guest = VIEW -User = VIEW + UPDATE
     *
     * Field will be searchable as keywork Field will be visibile with update,
     * and NOT hidden
     *
     * @param fieldName
     * @throws NestableException
     */
    public void createCalendarEventLongExpando(final long companyId, final String fieldName) throws NestableException {
	createExpandoField(companyId, fieldName, ExpandoColumnConstants.LONG, null);
    }

    private void createExpandoField(final long companyId, final String fieldName, final int columnType, final Object defaultValue) throws NestableException {
	try {
	    final ExpandoColumn createdColumn = CustomExpandoFieldsLocalServiceUtil.createExpandoColumn(companyId, CalEvent.class.getName(), fieldName, columnType, defaultValue,
		    visibleField);
	    CustomExpandoFieldsLocalServiceUtil.setColumnPermissionForRole(companyId, createdColumn.getColumnId(), RoleConstants.GUEST, guestPermissions);
	    CustomExpandoFieldsLocalServiceUtil.setColumnPermissionForRole(companyId, createdColumn.getColumnId(), RoleConstants.USER, userPermissions);
	} catch (final DuplicateColumnNameException e) {
	    //
	}
    }

    /**
     * Checks if the expando column exists
     * @param companyId
     * @param className
     * @param fieldName
     * @return
     */
    public static boolean doesExpandoFieldExist(final long companyId, final String className, final String fieldName) {
	boolean exists = false;
	try {
	    final ExpandoColumn expandoColumn = ExpandoColumnLocalServiceUtil.getColumn(companyId, className, ExpandoTableConstants.DEFAULT_TABLE_NAME, fieldName);
	    exists = expandoColumn != null;
	} catch (final Exception e) {
	    //
	}
	return exists;
    }

}
