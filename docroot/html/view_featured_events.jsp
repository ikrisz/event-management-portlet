<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>

<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<theme:defineObjects/>
<portlet:defineObjects />

<div class="panel panel-default">
	<div class="panel-heading"><liferay-ui:message key="page.title.featuredEvents"/> </div>
	<div class="panel-body">
		<c:forEach items="${featuredEvents}" var="featuredEvent" varStatus="featuredLoop" >
			<div class="row-fluid ${featuredLoop.last ? 'separator-row-last' : 'separator-row' }" id="featuredEventRow-${featuredLoop.index}">
				<div class="span12">
					<portlet:renderURL var="eventDetailsURL" windowState="normal">
						<portlet:param name="view" value="eventFullDetails"/>
						<portlet:param name="eventId" value="${featuredEvent.eventId}"/>
						<portlet:param name="eventUid" value="${featuredEvent.eventUid}"/>
					</portlet:renderURL>
					<h4 class="no-margin"><a href="${eventDetailsURL}"><c:out value="${featuredEvent.title}"/></a></h4>	
					<p class="no-margin"><i:eventDatesDetails event="${featuredEvent}" currentStartDate="${featuredEvent.dates.startDate}"/></p>
					<p class="no-margin">
						<c:choose>
							<c:when test="${featuredEvent.venue.online}">
								<strong><liferay-ui:message key="online"/></strong>
							</c:when>
							<c:otherwise>
								<c:out value="${eventUtils:getShortVenueLocation(featuredEvent, themeDisplay)}"/>
							</c:otherwise>
						</c:choose>
					</p> 
					<c:if test="${not empty featuredEvent.description}">
						<p>
							<c:set var="descriptionTrimmed" value="${fn:substring(featuredEvent.description, 0, 200)}" />
							<c:out value="${descriptionTrimmed}" escapeXml="false" />...
						</p>
					</c:if>	
					
					<a href="javascript:void(0);" onclick="showEventQuickLook('featuredEvent', '${featuredLoop.index}');"  class="btn btn-mini btn-default hidden-tablet hidden-phone">
						<liferay-ui:message key="button.quick.look"/>
					</a>
					<i:eventHoverDetails eventView="${featuredEvent}" eventHoverDivId="featuredEventListHover-${featuredLoop.index}"/>
				</div>
			</div>
		</c:forEach>
	</div>
</div>
