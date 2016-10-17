<%@ page contentType="text/html" isELIgnored="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>

<c:if test="${not empty eventView.eventbrite.eventbriteId && not empty eventView.eventbrite.tickets && (eventView.eventbrite.tickets.size() > 1 || !eventView.eventbrite.tickets.get(0).isNullTicket())}">
	<div class="accordion-group margin-15">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" href="#ticketsDetails">
				<span class="icon-tasks"></span>&nbsp;<liferay-ui:message key="section.event.eventbrite.tickets"/>
			</a>
		</div>
		<div id="ticketsDetails" class="accordion-body collapse in">
			<div class="accordion-inner maxHeightScrollable">
				<c:forEach var="ticket" items="${eventView.eventbrite.tickets}" varStatus="loop">
					<c:if test="${not ticket.isNullTicket()}">
						<div class="row-fluid ${loop.last ? 'separator-row-last' : 'separator-row' }">
							<div class="span6">
								<c:out value="${ticket.name}"/></p>
							</div>
							<div class="span4">
								<c:if test='${not empty ticket.price && ticket.price != "0.0"}'>
									<p class="no-margin">
										<strong>
											<liferay-ui:message key="tickets.price"/>&nbsp;
											<c:out value="${eventUtils:getCurrencySymbol(eventView.eventbrite)}"/>
											${ticket.price}
										</strong>
									</p>
								</c:if>
								<c:if test="${not empty ticket.quantityAvailable}">
									<p class="no-margin">
										<liferay-ui:message key="tickets.available"/>&nbsp;<c:out value="${ticket.quantityAvailable - ticket.quantitySold }"/>
									</p>
								</c:if>
								<c:if test="${not empty ticket.quantitySold}">
									<p class="no-margin">
										<liferay-ui:message key="tickets.sold"/>&nbsp;<c:out value="${ticket.quantitySold}"/>
									</p>
								</c:if>
							</div>
							<div class="span2">
								<span class="badge pull-right"><strong><liferay-ui:message key="ticket.${ticket.type}"/></strong></span>
							</div>
						</div>
					</c:if>
				</c:forEach>
			</div>
		</div>
	</div>
</c:if>
			
