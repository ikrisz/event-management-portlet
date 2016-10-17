<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<div class="accordion-group">
	<div class="accordion-heading">
		<a class="accordion-toggle" data-toggle="collapse" href="#organizerDetails">
			<liferay-ui:message key="section.eventupdate.organizer"/>
		</a>
	</div>
	<div id="organizerDetails" class="accordion-body collapse in">
		<div class="accordion-inner">
			<div class="row-fluid">
				<div class="span6">	
					<i:validationErrors fieldName="organizer.eventbrite.duplicate"/>
					<i:validationErrors fieldName="organizer.name"/>
					<aui:input name="organizer.name" id="organizerName" type="text" value="${event.organizer.name}" label="organizer.name" showRequiredLabel="false" />
				</div>
				<c:if test="${not empty availableOrganizers}">
					<div class="span6 pull-left">
						<aui:select name="organizer.organizerId" id="organizerId" label="organizer.eventbrite.select" showEmptyOption="true">
							<c:forEach items="${availableOrganizers}" var="entry">
								<aui:option value="${entry.organizerId}" selected="${entry.organizerId == event.organizer.organizerId}"><c:out value="${entry.name}"/></aui:option>
							</c:forEach>
						</aui:select>
					</div>
				</c:if>
			</div>
		</div>
	</div>
</div>
