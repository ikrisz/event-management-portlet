<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<c:if test="${isAddAction}">
	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" href="#paymentDetails">
				<liferay-ui:message key="section.eventupdate.eventbrite.payment"/>
			</a>
		</div>
		<div id="paymentDetails" class="accordion-body collapse in">
			<div class="accordion-inner">
				<div class="row-fluid">
					<div class="span12">
						<i:validationErrors fieldName="eventbrite.currency"/>
						<aui:select name="eventbrite.currency" label="eventbrite.currency" showRequiredLabel="false" showEmptyOption="true">
							<c:forEach var="entry" items="${availableCurrencies}">
								<aui:option value="${entry.key}" selected="${entry.key.equals(event.eventbrite.currency)}">${entry.key} &nbsp; ${entry.value}</aui:option>
							</c:forEach>
						</aui:select>
					</div>
				</div>
				
				<div class="row-fluid">
					<div class="span12">
						<i:validationErrors fieldName="eventbrite.payment.paypalEmail"/>
						<aui:input name="eventbrite.payment.paypalEmail" label="eventbrite.payment.paypalEmail" type="text" value="${event.eventbrite.payment.paypalEmail}" />
						<p class="muted no-margin"><liferay-ui:message key="eventbrite.payment.paypalEmail.help"/></p>
					</div>
				</div>
				
				<div class="row-fluid">
					<div class="span3">
						<aui:input name="eventbrite.payment.checkAccepted" label="eventbrite.payment.checkAccepted" type="checkbox" value="${event.eventbrite.payment.checkAccepted}" />
					</div>
					<div class="span9">
						<i:validationErrors fieldName="eventbrite.payment.instructions.check"/>
						<aui:input name="eventbrite.payment.checkInstructions" label="eventbrite.payment.checkInstructions" type="text" value="${event.eventbrite.payment.checkInstructions}" />
					</div>
				</div>
				
				<div class="row-fluid">
					<div class="span3">
						<aui:input name="eventbrite.payment.cashAccepted" label="eventbrite.payment.cashAccepted" type="checkbox" value="${event.eventbrite.payment.cashAccepted}" />
					</div>
					<div class="span9">
					<i:validationErrors fieldName="eventbrite.payment.instructions.cash"/>
						<aui:input name="eventbrite.payment.cashInstructions" label="eventbrite.payment.cashInstructions" type="text" value="${event.eventbrite.payment.cashInstructions}" />
					</div>
				</div>
				
				<div class="row-fluid">
					<div class="span3">
						<aui:input name="eventbrite.payment.invoiceAccepted" label="eventbrite.payment.invoiceAccepted" type="checkbox" value="${event.eventbrite.payment.invoiceAccepted}" />
					</div>
					<div class="span9">
						<i:validationErrors fieldName="eventbrite.payment.instructions.invoice"/>
						<aui:input name="eventbrite.payment.invoiceInstructions" label="eventbrite.payment.invoiceInstructions" type="text" value="${event.eventbrite.payment.invoiceInstructions}" />
					</div>
				</div>
			</div>
		</div>
	</div>
</c:if>