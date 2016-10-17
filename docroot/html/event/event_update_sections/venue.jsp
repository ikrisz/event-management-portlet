<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<div class="accordion-group">
	<div class="accordion-heading">
		<a class="accordion-toggle" data-toggle="collapse" href="#venueDetails">
			<liferay-ui:message key="section.eventupdate.venue"/>
		</a>
	</div>
	<div id="venueDetails" class="accordion-body collapse in">
		<div class="accordion-inner">
			<c:if test="${not empty event.location}">
				<div class="row-fluid">
					<div class="span12">
						<div class="alert alert-warning">
							<c:set var="alertLocationMessage">
								<liferay-ui:message key="warn.event.location.to.migrate" arguments="${event.location}"/>
							</c:set>
							<c:out value="${alertLocationMessage}" escapeXml="false"/>
						</div>					
					</div>
				</div>
			</c:if>
		
			<div class="row-fluid">
				<div class="span12">
					<aui:input name="venue.online" id="venueOnline" label="venue.online" type="checkbox" value="${event.venue.online}" />
				</div>
			</div>
			
			<c:if test="${showEventbriteVenue}">
				<div class="row-fluid">
					<div class="span12">
						<i:validationErrors fieldName="venue.eventbrite.duplicate"/>
						<aui:select name="venue.venueId" id="venueId" label="venue.eventbrite.select" showEmptyOption="true">
							<c:forEach items="${availableVenues}" var="entry">
								<aui:option value="${entry.venueId}">${entry.name} - ${entry.addressLineOne}, ${entry.city}</aui:option>
							</c:forEach>
						</aui:select>
					</div>
				</div>
			</c:if>
			
			<div class="row-fluid margin-15">
				<div class="span12">
					<i:validationErrors fieldName="venue.name"/>
					<aui:input name="venue.name" id="venueName" type="text" value="${event.venue.name}" label="venue.name" showRequiredLabel="false" />
				</div>
			</div>
			
			<div class="row-fluid margin-15">
				<div class="span6">
					<i:validationErrors fieldName="venue.addressLineOne"/>
					<aui:input name="venue.addressLineOne" id="venueAddressLineOne" type="text" value="${event.venue.addressLineOne}" label="venue.addressLineOne" showRequiredLabel="false" />
					<br/>
					<i:validationErrors fieldName="venue.addressLineTwo"/>
					<aui:input name="venue.addressLineTwo" id="venueAddressLineTwo" type="text" value="${event.venue.addressLineTwo}" label="venue.addressLineTwo" showRequiredLabel="false" />
					<br/>
					<i:validationErrors fieldName="venue.city"/>
					<aui:input name="venue.city" id="venueCity" type="text" value="${event.venue.city}" label="venue.city" showRequiredLabel="false" />
				</div>
				<div class="span6">
					<i:validationErrors fieldName="venue.zip"/>
					<aui:input name="venue.zip" id="venueZip" type="text" value="${event.venue.zip}" label="venue.zip" showRequiredLabel="false" />
					<br/>
					<i:validationErrors fieldName="venue.regionState.usa"/>
					<i:validationErrors fieldName="venue.regionState"/>
					<div id="${namespace}stateTextDiv">
						<aui:input name="regionStateText" id="venueRegionStateText" type="text" value="${event.venue.regionState}" label="venue.state" showRequiredLabel="false" />
					</div>
					<div class="hide" id="${namespace}stateSelectUSADiv">
						<jsp:useBean id="stateHelper" class="com.pfiks.intelligus.events.model.StateHelperUSA" scope="application" />
						<aui:select name="regionStateSelect" id="venueRegionStateSelect" label="venue.state" showEmptyOption="true">
							<c:forEach var="entry" items="${stateHelper.getAllStates()}">
								<aui:option value="${entry.code}" selected="${entry.code.equals(event.venue.regionState)}"><c:out value="${entry.name}"/></aui:option>
							</c:forEach>
						</aui:select>
					</div>
					<br/>
					<i:validationErrors fieldName="venue.country"/>
					<aui:select name="venue.country" id="venueCountry" label="venue.country" showEmptyOption="true">
						<c:forEach var="entry" items="${countries}">
							<aui:option value="${entry.key}" selected="${entry.key.equals(event.venue.country)}"><c:out value="${entry.value}"/></aui:option>
						</c:forEach>
					</aui:select>
				</div>
			</div>
		</div>
	</div>
</div>

