<%@ page contentType="text/html" isELIgnored="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>

<theme:defineObjects/>

<c:if test="${not empty eventView.eventbrite.eventbriteId && not empty eventView.eventbrite.attendees}">
	<div class="accordion-group margin-15">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" href="#attendeesDetails">
				<span class="icon-user"></span>&nbsp;<liferay-ui:message key="section.event.eventbrite.attendees"/>
			</a>
		</div>
		<div id="attendeesDetails" class="accordion-body collapse in">
			<div class="accordion-inner maxHeightScrollable">
				<c:set var="contactUserDialogTitle"><liferay-ui:message key="page.title.contactUser"/></c:set>
				<c:forEach var="attendee" items="${eventView.eventbrite.attendees}" varStatus="attendeeLoop" >
					<c:if test="${hasEditPermission || attendee.isLiferayUser()}">
						<div class="row-fluid ${attendeeLoop.last ? 'separator-row-last' : 'separator-row' }" id="attendeeRow-${attendeeLoop.index}">
							<div class="span2">
								<img src="${eventUtils:getAttendeePortraitUrl(attendee, themeDisplay)}" class="user-profile-image-small" />
							</div>
							<div class="span3">
								<c:choose>
									<c:when test="${attendee.isLiferayUser()}">
										 <a href="${attendee.user.getDisplayURL(themeDisplay)}" title="View profile">${attendee.firstName}&nbsp;${attendee.lastName}</a>												
									</c:when>
									<c:otherwise>
										${attendee.firstName}&nbsp;${attendee.lastName}
									</c:otherwise>
								</c:choose>
							</div>
							<div class="span2">
								<c:if test="${hasEditPermission}">
									<portlet:renderURL var="contactUserURL" windowState="exclusive">
										<portlet:param name="view" value="contactUser"/>
										<portlet:param name="emailAddress" value="${attendee.emailAddress}"/>
										<portlet:param name="eventId" value="${eventView.eventId}"/>
									</portlet:renderURL>
									<a href="javascript:void(0);" onclick="openModalDialogWithPos('${contactUserURL}', '${contactUserDialogTitle}', 'attendeeRow-${attendeeLoop.index}');">
										<span class="badge"><span class="icon-envelope"></span>&nbsp;<liferay-ui:message key="event.attendee.send.message"/></span>
									</a>	
								</c:if>
							</div>
							<div class="span5">
								<c:forEach var="ticketPurchased" items="${attendee.ticketsPurchased}">
									<div class="row-fluid">
										<div class="span6">
											<c:out value="${ticketPurchased.name}"/>
										</div>
										<div class="span4">
											<c:if test="${not empty ticketPurchased.quantitySold}">
												<liferay-ui:message key="tickets.purchased"/>&nbsp;<c:out value="${ticketPurchased.quantitySold}"/>
											</c:if>
										</div>
										<div class="span2">
											<span class="badge pull-right"><strong><liferay-ui:message key="ticket.${ticketPurchased.type}"/></strong></span>
										</div>
									</div>
								</c:forEach>
							</div>
						</div>
					</c:if>
				</c:forEach>
			</div>
		</div>
	</div>
</c:if>
