<%@ page contentType="text/html" isELIgnored="false"%>

<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>

<div class="row-fluid">
	<div class="span12">
		<portlet:renderURL var="eventShareURL" windowState="normal">
			<portlet:param name="view" value="eventFullDetails"/>
			<portlet:param name="eventId" value="${eventView.eventId}"/>
			<portlet:param name="eventUid" value="${eventView.eventUid}"/>
		</portlet:renderURL>
		<strong><liferay-ui:message key="event.share"/></strong>
		<br/>
		<liferay-ui:social-bookmarks displayStyle="simple" url="${eventShareURL}" title="${eventView.title}" target="_blank" />
	</div>
</div>

<c:if test="${isTeamworxxDeployed && not empty loggedInUser}">
	<div class="row-fluid">
		<div class="span12">
			<div><notifications className="${calEventClassName}" classPK="${eventView.eventId}" userId="${loggedInUser.userId}" type="4" setting="5"/></div>
			<div><favourites className="${calEventClassName}" classPK="${eventView.eventId}"  userId="${loggedInUser.userId}" groupId="${eventView.groupId}" /></div>
		</div>
	</div>
</c:if>
					
