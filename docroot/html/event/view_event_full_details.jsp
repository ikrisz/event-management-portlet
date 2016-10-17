<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>

<portlet:defineObjects />
<theme:defineObjects/>

<c:if test="${relatedContentEnabled}">
	<input class="rel-srch-entry" type="hidden" name="CalEvent" value="<c:out value="${eventView.eventId}"/>" />
</c:if>

<div class="container-fluid">
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title"><liferay-ui:message key="page.title.eventDetails"/></h3>
		</div>
		<div class="panel-body accordion">
			<div class="row-fluid">
				<div class="span12">
					<liferay-ui:success key="people-invited" message="success.event.people.invited" />
					<liferay-ui:success key="user-message-sent" message="success.user.message.sent" />
					<liferay-ui:error key="user-message-invalid" message="error.user.message.invalid"/>
				</div>
			</div>
		
			<div class="row-fluid">
				<div class="span12">
					<h3 class="no-margin"><c:out value="${eventView.title}"/></h3>
				</div>
			</div>
			<c:set var="googlemapsAddress" value="${eventUtils:getGoogleMapAddress(eventView)}"/>
			
			<c:set var="leftColumnWidth" value="span9"/>
			<c:set var="rightColumnWidth" value="span3"/>
			<c:if test="${not empty googlemapsAddress}">
				<c:set var="leftColumnWidth" value="span6"/>
				<c:set var="rightColumnWidth" value="span6"/>
			</c:if>
			
			<div class="row-fluid">
				<div class="${leftColumnWidth}">
					<c:if test="${not empty eventView.eventbrite.status && eventView.eventbrite.status == 'started'}">
						<p class="text-warning"><liferay-ui:message key="event.started"/></p>
					</c:if>
					<p>
						<i:eventDatesDetails event="${eventView}" currentStartDate="${eventView.dates.startDate}"/>
					</p>
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
					
					<p><c:out value="${eventView.summary}" /></p>
					
					<p><c:out value="${eventView.description}" escapeXml="false" /></p>
				</div>
				
				<div class="${rightColumnWidth}">
					<jsp:include page="event_view_sections/view_user_action_link.jsp"/>

					<c:if test="${not empty googlemapsAddress}">
						<div class="row-fluid">
							<div class="span12">
								<jsp:include page="google_map.jsp"/>
								<div id="mapview">
									<div id="map_canvas" class="googleMap"></div>
									<a href="https://maps.google.co.uk?q=${googlemapsAddress}" target="_blank" class="btn btn-link pull-right">
										<span class="icon-road"></span>&nbsp;<liferay-ui:message key="button.googlemaps"/>
									</a>
									<script type="text/javascript">
										jQuery(document).ready(function(){
											codeAddress('${googlemapsAddress}');
										});
									</script>
								</div>
							</div>
						</div>
					</c:if>
				</div>
			</div>
			
			<c:if test="${not empty eventView.organizer.name}">
				<div class="row-fluid">
					<div class="span12">
						<p><strong><liferay-ui:message key="organizer"/></strong><c:out value="${eventView.organizer.name}"/></p>
					</div>
				</div>
			</c:if>
			
			<jsp:include page="event_view_sections/view_future_dates.jsp"/>
			
			<c:if test="${not empty eventView.eventbrite.eventbriteId}">
				<jsp:include page="event_view_sections/view_tickets.jsp"/>
			
				<jsp:include page="event_view_sections/view_attendees.jsp"/>
			
				<jsp:include page="event_view_sections/view_send_invites.jsp"/>
			</c:if>
			
			<jsp:include page="event_view_sections/view_user_action_sections.jsp"/>
		</div>
		
		<div class="panel-footer" id="eventDetailsFooterDiv">
			<c:if test="${not empty eventView.eventbrite.url}">
				<a href="${eventView.eventbrite.url}" target="_blank" class="btn btn-success">
					<span class="icon-share"></span>&nbsp;<liferay-ui:message key="button.view.eventbrite"/>
				</a>
			</c:if>
			
			<c:if test="${hasEditPermission}">
				<portlet:renderURL var="editEventURL" windowState="normal">
					<portlet:param name="view" value="${manageEventbriteEvent ? 'updateEventbriteEvent' : 'updateEvent'}"/>
					<portlet:param name="eventId" value="${eventView.eventId}"/>
				</portlet:renderURL>
				<a href="${editEventURL}" class="btn btn-primary">
					<span class="icon-pencil"></span>&nbsp;<liferay-ui:message key="button.edit"/>
				</a>
			</c:if>
			 
			<c:if test="${hasDeletePermission}">
				<portlet:actionURL var="deleteEventURL" windowState="normal">
					<portlet:param name="action" value="deleteEvent"/>
					<portlet:param name="eventId" value="${eventView.eventId}"/>
				</portlet:actionURL>
				
				<a href="#deleteEventModal" role="button" class="btn btn-danger" data-toggle="modal">
					<span class="icon-trash"></span>&nbsp;<liferay-ui:message key="button.delete"/>
				</a>
				
				<div id="deleteEventModal" style="display:none;" class="modal" tabindex="-1" role="dialog" aria-hidden="true">
					<div class="modal-body">
						<h4>
							<c:choose>
								<c:when test="${not empty eventView.eventbrite.eventbriteId}">
									<liferay-ui:message key="warn.delete.event.eventbrite"/>
								</c:when>
								<c:otherwise>
									<liferay-ui:message key="warn.delete.event"/>
								</c:otherwise>
							</c:choose>
						</h4>
					</div>
					<div class="modal-footer" id="deletePanelFooder">
						<a class="btn btn-danger" href="${deleteEventURL}" onclick="javascript:jQuery('#deletePanelFooder').hide();jQuery('#loadingDeletePanelFooder').show();">
							<span class="icon-trash"></span>&nbsp;Delete
						</a>
						&nbsp;&nbsp;
						<a class="btn btn-default" id="closeDialog" href="javascript:void(0);" onclick="javascript:jQuery('#deleteEventModal').modal('hide');">
							<span class="icon-remove"></span>&nbsp;Close
						</a>
					</div>
					<div class="modal-footer" style="display:none;" id="loadingDeletePanelFooder">
						<h4>
							<liferay-ui:message key="info.loading.deleting.event"/>&nbsp;&nbsp;<span class="pull-right loadingGif"></span>
						</h4>
					</div>
				</div>

			</c:if>
			
			<portlet:renderURL var="backURL" portletMode="view" windowState="normal"/>
			<a href="${backURL}" class="btn btn-default"><span class="icon-share-alt"></span>&nbsp;<liferay-ui:message key="button.back"/></a>
		</div>
	</div>
</div>