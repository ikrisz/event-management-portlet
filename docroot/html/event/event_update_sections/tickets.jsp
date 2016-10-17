<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>


<div class="accordion-group">
	<div class="accordion-heading">
		<a class="accordion-toggle" data-toggle="collapse" href="#ticketsDetails">
			<liferay-ui:message key="section.eventupdate.eventbrite.ticket"/>
		</a>
	</div>
	<div id="ticketsDetails" class="accordion-body collapse in">
		<div class="accordion-inner">
			<c:if test="${not isAddAction}">
				<div class="row-fluid">
					<div class="span12">
						<aui:input name="updateTickets" id="updateTickets" label="tickets.update" type="checkbox" value="${showUpdateTickets}" />
						<p class="muted no-margin"><liferay-ui:message key="tickets.update.help"/></p>
					</div>
				</div>
			</c:if>
			
			<div class="${isAddAction || showUpdateTickets ? '' : 'hide'}" id="ticketsUpdateDiv">
				<div class="row-fluid">
					<div class="span12">
						<i:validationErrors fieldName="eventbrite.tickets.startDate"/>
					</div>
				</div>
				<div class="row-fluid margin-15">
					<div class="span6">
						<label class="control-label"><liferay-ui:message key="eventbrite.tickets.startDate"/> </label>
						<liferay-ui:input-date firstDayOfWeek="${firstDayOfWeek}" disabled="false" 
								dayParam="ticketsStartDay" dayValue="${event.eventbrite.ticketsStartDay}"
								monthParam="ticketsStartMonth" monthValue="${event.eventbrite.ticketsStartMonth}"
								yearParam="ticketsStartYear" yearValue="${event.eventbrite.ticketsStartYear}" />
					</div>
					<div class="span1">
						<aui:select name="eventbrite.ticketsStartHour" id="eventbriteStartHour" multiple="false" showEmptyOption="false" label="Start hour" cssClass="time-selection">
							<c:forEach var="val" step="1" begin="0" end="23">
								<aui:option value="${val}" selected="${val == event.eventbrite.ticketsStartHour}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
							</c:forEach>
						</aui:select>
					</div>
					<div class="span2">
						<aui:select name="eventbrite.ticketsStartMinute" id="eventbriteStartMinute" multiple="false" showEmptyOption="false" label="Start minute" cssClass="time-selection">
							<c:forEach var="val" step="1" begin="0" end="59">
								<aui:option value="${val}" selected="${val == event.eventbrite.ticketsStartMinute}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
							</c:forEach>
						</aui:select>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span12">
						<i:validationErrors fieldName="eventbrite.tickets.endDate"/>
					</div>
				</div>
				<div class="row-fluid">
					<div class="span6">
						<label class="control-label"><liferay-ui:message key="eventbrite.tickets.endDate"/> </label>
						<liferay-ui:input-date firstDayOfWeek="${firstDayOfWeek}" disabled="false"
								dayParam="ticketsEndDay" dayValue="${event.eventbrite.ticketsEndDay}"
								monthParam="ticketsEndMonth" monthValue="${event.eventbrite.ticketsEndMonth}"
								yearParam="ticketsEndYear" yearValue="${event.eventbrite.ticketsEndYear}" />
					</div>
					<div class="span1">
						<aui:select name="eventbrite.ticketsEndHour" id="eventbriteEndHour" multiple="false" showEmptyOption="false" label="End hour" cssClass="time-selection">
							<c:forEach var="val" step="1" begin="0" end="23">
								<aui:option value="${val}" selected="${val == event.eventbrite.ticketsEndHour}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
							</c:forEach>
						</aui:select>
					</div>
					<div class="span3">
						<aui:select name="eventbrite.ticketsEndMinute" id="eventbriteEndMinute" multiple="false" showEmptyOption="false" label="End minute" cssClass="time-selection">
							<c:forEach var="val" step="1" begin="0" end="59">
								<aui:option value="${val}" selected="${val == event.eventbrite.ticketsEndMinute}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
							</c:forEach>
						</aui:select>
					</div>
				</div>
				
				<div class="row-fluid">
					<div class="span12">
						<i:validationErrors fieldName="eventbrite.tickets"/>
						<a href="javascript:void(0);" onclick="addTicketDiv('${namespace}');" class="btn btn-small btn-info">
							<span class="icon-plus"></span>&nbsp;<liferay-ui:message key="button.add.ticket"/>
						</a>
					</div>
				</div>
				
				<div id="eventTicketsContainer">
					<c:forEach var="ticket" items="${event.eventbrite.tickets}" varStatus="loop">
						<c:if test="${not ticket.isNullTicket()}">
							<div class="row-fluid separator-row" id="ticketIndex[${loop.index}]">
								<div class="span12">
									<div class="row-fluid no-margin">
										<div class="span2">
											<i:ticketValidationErrors fieldKey="eventbrite.tickets[${loop.index}].type" fieldName="eventbrite.ticket.type"/>
										</div>
										<div class="span6">
											<i:ticketValidationErrors fieldKey="eventbrite.tickets[${loop.index}].name" fieldName="eventbrite.ticket.name"/>
										</div>
										<div class="span2">
											<i:ticketValidationErrors fieldKey="eventbrite.tickets[${loop.index}].quantityAvailable" fieldName="eventbrite.ticket.quantityAvailable"/>
										</div>
										<div class="span2" id="${namespace}_price_[${loop.index}]">
											<i:ticketValidationErrors fieldKey="eventbrite.tickets[${loop.index}].price" fieldName="eventbrite.ticket.price"/>
										</div>
									</div>
									<div class="row-fluid no-margin">
										<div class="span2">
											<aui:select name="eventbrite.tickets[${loop.index}].type" showEmptyOption="true" label="eventbrite.ticket.type" onChange="changeTicketType(this,'${namespace}',['${loop.index}'])" cssClass="ticket-selection">
												<aui:option value="free" selected="${event.eventbrite.tickets[loop.index].type == 'free'}"><liferay-ui:message key="ticket.free"/></aui:option>
												<aui:option value="donation" selected="${event.eventbrite.tickets[loop.index].type == 'donation'}"><liferay-ui:message key="ticket.donation"/></aui:option>
												<aui:option value="paid" selected="${event.eventbrite.tickets[loop.index].type == 'paid'}"><liferay-ui:message key="ticket.paid"/></aui:option>
											</aui:select>
										</div>
										<div class="span6">
											<aui:input name="eventbrite.tickets[${loop.index}].name" label="eventbrite.ticket.name" type="text" value="${event.eventbrite.tickets[loop.index].name}" cssClass="width-100" />
										</div>
										<div class="span2">
											<aui:input name="eventbrite.tickets[${loop.index}].quantityAvailable" type="text" value="${event.eventbrite.tickets[loop.index].quantityAvailable}" label="eventbrite.ticket.quantityAvailable" cssClass="width-50"/>
										</div>
										<div class="span2" id="${namespace}_price_[${loop.index}]">
											<aui:input name="eventbrite.tickets[${loop.index}].price" type="text" value="${event.eventbrite.tickets[loop.index].price}" label="eventbrite.ticket.price" cssClass="width-50" />
										</div>
									</div>
									<c:if test="${empty event.eventbrite.tickets[loop.index].ticketId}">
										<a href="javascript:void(0);" onclick="removeTicketDiv('${namespace}','${loop.index}');" class="btn btn-small btn-link pull-right"><span class="icon-remove"></span>&nbsp;Remove</a>
									</c:if>
								</div>
							</div>
						</c:if>
					</c:forEach>
				</div>
				
				<div class="hide">
					<div class="row-fluid eventTicketsTemplate separator-row" id="ticketIndex[0]">
						<div class="span12">
							<div class="row-fluid">							
								<div class="span2">
									<aui:select name="eventbrite.tickets[0].type" showEmptyOption="true" label="eventbrite.ticket.type" onChange="changeTicketType(this,'${namespace}','[0]')" cssClass="ticket-selection">
										<aui:option value="free"><liferay-ui:message key="ticket.free"/></aui:option>
										<aui:option value="donation"><liferay-ui:message key="ticket.donation"/></aui:option>
										<aui:option value="paid"><liferay-ui:message key="ticket.paid"/></aui:option>
									</aui:select>
								</div>
								<div class="span6">
									<aui:input name="eventbrite.tickets[0].name" label="eventbrite.ticket.name" type="text" cssClass="width-100"/>
								</div>
								<div class="span2">
									<aui:input name="eventbrite.tickets[0].quantityAvailable" type="text" label="eventbrite.ticket.quantityAvailable" cssClass="width-50"/>
								</div>
								<div class="span2" id="${namespace}_price_[0]">
									<aui:input name="eventbrite.tickets[0].price" type="text" label="eventbrite.ticket.price" cssClass="price-field width-50"/>
								</div>
							</div>
							<a href="javascript:void(0);" onclick="removeTicketDiv('${namespace}','[0]');" class="btn btn-small btn-link pull-right"><span class="icon-remove"></span>&nbsp;Remove</a>
						</div>
					</div>
				</div>
			</div>
			<aui:input name="ticketsToRemove" value="" type="hidden" id="ticketsToRemove"/>
			<aui:input name="newTicketIndex" value="${event.eventbrite.tickets.size()+1}" type="hidden" id="newTicketIndex"/>
		</div>
	</div>
</div>