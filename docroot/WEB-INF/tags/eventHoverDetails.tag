<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>

<%@ attribute name="eventView" required="true" type="com.pfiks.intelligus.events.model.event.EventModel" %>
<%@ attribute name="eventHoverDivId" required="true" type="java.lang.String" %>

<theme:defineObjects/>

<div id="${eventHoverDivId}" tabindex="-1" class="eventQuickLookPopup" style="display:none;">
	<portlet:defineObjects />
	<div class="container-fluid">
		<div class="events-portlet">
			<div class="panel panel-default">
				<div class="panel-heading">
					<h5 class="panel-title">
						<c:out value="${eventView.title}"/>
						<a class="close pull-right" href="javascript:void(0);" onclick="closeEventQuickLookPopup();">&times;</a>
					</h5>	
				</div>
				<div class="panel-body">
					<div class="maxHeightScrollable">
						<p class="no-margin"><i:eventDatesDetails event="${eventView}" currentStartDate="${eventView.dates.startDate}"/></p>
						<p class="no-margin">
							<c:choose>
								<c:when test="${eventView.venue.online}">
									<strong><liferay-ui:message key="online"/></strong>
								</c:when>
								<c:otherwise>
									<strong><liferay-ui:message key="venue"/></strong>
									<c:out value="${eventUtils:getFullVenueLocation(eventView,themeDisplay)}"/>
								</c:otherwise>
							</c:choose>
						</p>
						
						<c:if test="${eventView.user != null}">
							<p><liferay-ui:message key="creator"/> ${eventView.user.fullName}</p>
						</c:if>
						
						<c:if test="${not empty eventView.summary}">
							<p>
								<c:out value="${eventView.summary}" />
							</p>
						</c:if>
								
						<jsp:include page="/html/event/event_view_sections/view_future_dates.jsp"/>
					</div>
				</div>
				<div class="panel-footer">
					<portlet:renderURL var="eventFullDetailsURL" windowState="normal">
						<portlet:param name="view" value="eventFullDetails"/>
						<portlet:param name="eventId" value="${eventView.eventId}"/>
						<portlet:param name="eventUid" value="${eventView.eventUid}"/>
					</portlet:renderURL>
					 
					<a href="${eventFullDetailsURL}" class="btn btn-xs btn-success">
						<span class="icon-share"></span>&nbsp;<liferay-ui:message key="button.full.details"/>
					</a>
					 
					<c:if test="${eventView.hasEditPermission}">
						<c:set var="isEventbrite" value="${eventbriteEnabled && not empty eventView.eventbrite.eventbriteId}"/>
						<portlet:renderURL var="editEventURL" windowState="normal">
							<portlet:param name="view" value="${isEventbrite ? 'updateEventbriteEvent' : 'updateEvent'}"/>
							<portlet:param name="eventId" value="${eventView.eventId}"/>
						</portlet:renderURL>
						<a href="${editEventURL}" class="btn btn-xs btn-primary">
							<span class="icon-pencil"></span>&nbsp;<liferay-ui:message key="button.edit"/>
						</a>
					</c:if>
				</div>
			</div>
		</div>
	</div>
</div>