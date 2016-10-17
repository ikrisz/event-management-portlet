<%@ page contentType="text/html" isELIgnored="false"%>

<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>

<theme:defineObjects/>
<portlet:defineObjects />

<div class="container-fluid">
	<div class="row-fluid separator-row">
		<div class="span12">
			<h3 class="no-margin"><c:out value="${eventView.title}"/></h3>
			<div>
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
				<c:if test="${not empty eventView.description}">
					<div class="maxHeightScrollable">
						<p><c:out value="${eventView.description}" escapeXml="false" /></p>
					</div>
				</c:if>
				
				<jsp:include page="/html/event/event_view_sections/view_future_dates.jsp"/>

			</div>
		</div>
	</div>
	<div class="row-fluid margin-15">
		<div class="span12">
			<portlet:renderURL var="eventDetailsURL" windowState="normal">
				<portlet:param name="view" value="eventFullDetails"/>
				<portlet:param name="eventId" value="${eventView.eventId}"/>
				<portlet:param name="eventUid" value="${eventView.eventUid}"/>
			</portlet:renderURL>
			<a href="${eventDetailsURL}" class="btn btn-success">
				<span class="icon-share"></span>&nbsp;<liferay-ui:message key="button.full.details"/>
			</a>				
			
			<c:if test="${hasEditPermission}">
				<portlet:renderURL var="editEventURL" windowState="normal">
					<portlet:param name="view" value="${manageEventbriteEvent ? 'updateEventbriteEvent' : 'updateEvent'}"/>
					<portlet:param name="eventId" value="${eventView.eventId}"/>
				</portlet:renderURL>
				<a href="${editEventURL}" class="btn btn-primary">
					<span class="icon-pencil"></span>&nbsp;<liferay-ui:message key="button.edit"/>
				</a>
			</c:if>
			<a href="javascript:void(0);" onClick="event.preventDefault();Liferay.Intelligus.closePopup();" class="btn btn-default">
				<span class="icon-remove"></span>&nbsp;<liferay-ui:message key="button.close"/>
			</a>
		</div>
	</div>
</div>

<style>
	#intelligus_dialog .modal-body{
		height: auto !important;
		max-height: 400px !important;
	}
</style>