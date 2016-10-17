<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>

<theme:defineObjects/>

<div class="container-fluid">
	<div class="panel panel-default">
		<div class="panel-heading"><h3 class="panel-title"><liferay-ui:message key="page.title.importEventbriteEvents"/></h3></div>
		<div class="panel-body">
			<div class="row-fluid margin-15">
				<div class="span12">
					<p class="muted no-margin margin-sides"><liferay-ui:message key="info.event.import.help"/></p>
				</div>
			</div>
			<c:choose>
				<c:when test="${empty eventbriteEvents}">
					<div class="row-fluid">
						<div class="span12">
							<div class="alert alert-warning">
								<liferay-ui:message key="warn.no.eventbrite.events.found"/>
							</div>
						</div>
					</div>
				</c:when>
				<c:otherwise>
				<joda:dateTimeZone value="${themeDisplay.timeZone.ID}" >
					<portlet:resourceURL var="importEventbriteEvent" id="importEventbriteEvent"/>
					<c:forEach items="${eventbriteEvents}" var="event"  varStatus="loop">
						<div class="row-fluid ${loop.last ? 'separator-row-last' : 'separator-row' }" >
							<div class="span6">
								<h4><c:out value="${event.title}"/></a></h4>
								<p class="no-margin"><joda:format value="${event.dates.startDate}" style="LS"/>&nbsp;|&nbsp;<joda:format value="${event.dates.endDate}" style="LS"/></p>
								<p class="no-margin"><c:out value="${event.description}" escapeXml="false"/></p>
								<p class="no-margin"><c:out value="${eventUtils:getFullVenueLocation(event, themeDisplay)}"/></p>
								<p>
									<a href="${event.eventbrite.url}" target="_blank" title="View event in Eventbrite" >
										<liferay-ui:message key="button.view.eventbrite"/> 
									</a>
								</p>
							</div>
							<c:set var="isImportable" value="${not event.eventbrite.recurrent && not event.eventbrite.multiday}"/>
							<div class="span2">
								<c:choose>
									<c:when test="${event.eventbrite.recurrent}">
										<h4 class="text-warning"><liferay-ui:message key="info.eventbrite.recurrent.event"/></h4>
									</c:when>
									<c:otherwise>
										<c:if test="${event.eventbrite.multiday}">
											<h4 class="text-warning"><liferay-ui:message key="info.eventbrite.multiday.event"/></h4>
										</c:if>
									</c:otherwise>
								</c:choose>
							</div>
							
							<div class="span4 pull-right">
								<c:if test="${isImportable}">
									<a href="javascript:void(0);" id="eventImport_${event.eventbrite.eventbriteId}"
										onclick="importEventbriteEvent('${importEventbriteEvent}','<portlet:namespace />', '${event.eventbrite.eventbriteId}')" class="pull-right btn btn-small btn-primary">
										<span class="icon-download-alt"></span>&nbsp;
										<liferay-ui:message key="button.import"/> 
									</a>
									<a href="javascript:void(0);" id="eventImport_importing_${event.eventbrite.eventbriteId}" class="pull-right btn btn-small btn-default hide">
										<span class="icon-refresh"></span>&nbsp;
										<liferay-ui:message key="info.event.importing"/> 
									</a>
									<a href="javascript:void(0);" id="eventImported_${event.eventbrite.eventbriteId}" class="pull-right btn btn-small btn-success hide">
										<span class="icon-ok"></span>&nbsp;
										<liferay-ui:message key="success.event.imported"/> 
									</a>
								</c:if>
								<a href="javascript:void(0);" id="eventImported_error_${event.eventbrite.eventbriteId}" class="pull-right btn btn-small btn-danger ${isImportable? 'hide' : ''}">
									<span class="icon-minus-sign"></span>&nbsp;
									<liferay-ui:message key="warn.event.importing.error"/> 
								</a>
							</div>
						</div>
					</c:forEach>
				</joda:dateTimeZone>
				</c:otherwise>
			</c:choose>
		</div>
		<div class="panel-footer">
			<portlet:renderURL var="backURL" windowState="normal"/>
			<a href="${backURL}" class="btn btn-default"><span class="icon-share-alt"></span>&nbsp;<liferay-ui:message key="button.back"/></a>
		</div>
	</div>
</div>