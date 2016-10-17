<%@ page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:resourceURL var="calendarListEventsURL" id="calendarListEvents"/>

<portlet:renderURL var="eventDetailsURL" windowState="<%= LiferayWindowState.EXCLUSIVE.toString() %>">
	<portlet:param name="view" value="eventShortDetails"/>
</portlet:renderURL>

<div id="calendar"></div>

<script type="text/javascript">
jQuery(document).ready(function() {
    loadCalendar('<portlet:namespace/>','${calendarListEventsURL}', '${eventDetailsURL}');
});
</script>
