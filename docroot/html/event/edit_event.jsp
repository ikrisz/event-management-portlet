<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>

<portlet:defineObjects />
<c:set var="namespace" scope="request">
	<portlet:namespace/>
</c:set>

<portlet:actionURL var="updateEventURL">
	<portlet:param name="action" value="${actionName}"/>
</portlet:actionURL>

<c:set var="showEventbriteVenue" value="false" scope="request"/>
<c:set var="showRecurrencyDates" value="true" scope="request"/>
<c:set var="showMultidayDates" value="true" scope="request"/>
<c:set var="showEventbriteGeneralFields" value="false" scope="request"/>

<aui:form action="${updateEventURL}" id="updateEventForm" name="updateEventForm" inlineField="false" onSubmit="showLoadingDiv();">
	<div class="container-fluid">
		<div class="panel panel-default">
			<div class="panel-heading"><div class="panel-title"><liferay-ui:message key="page.title.${actionName}"/></div></div>

			<div class="panel-body accordion">
				<jsp:include page="event_update_sections/general.jsp"/>
				<jsp:include page="event_update_sections/dates.jsp"/>
				<jsp:include page="event_update_sections/venue.jsp"/>
				<jsp:include page="event_update_sections/tags.jsp"/>
			</div>

			<div class="panel-footer">
				<aui:button type="submit" value="button.save" cssClass="btn btn-primary" />
				
				<c:if test="${hasDeletePermission}">
					<portlet:actionURL var="deleteEventURL">
						<portlet:param name="action" value="deleteEvent"/>
						<portlet:param name="eventId" value="${event.eventId}"/>
					</portlet:actionURL>
					
					<a href="#deleteEventModal" role="button" class="btn btn-danger" data-toggle="modal">
						<span class="icon-trash"></span>&nbsp;<liferay-ui:message key="button.delete"/>
					</a>
					
					<div id="deleteEventModal" style="display:none;" class="modal" tabindex="-1" role="dialog" aria-hidden="true">
						<div class="modal-body">
							<h4>
								<liferay-ui:message key="warn.delete.event"/>
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
				
				<portlet:renderURL var="backURL"/>
				<a href="${backURL}" class="btn btn-default"><span class="icon-share-alt"></span>&nbsp;<liferay-ui:message key="button.back"/></a>
			</div>
		</div>
	</div>
</aui:form>

<div id="updateEventLoadingModal" style="display:none;" class="modal" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="modal-body">
		<h4><liferay-ui:message key="info.loading.saving.event"/><span class="pull-right loadingGif"></span></h4>
	</div>
</div>

<script type="text/javascript">
jQuery(document).ready(function(){
	var eventConfirmMessage = '<liferay-ui:message key="warn.public.event.check"/>';
	intelligusEvent.InitializeEventUpdate('${namespace}', eventConfirmMessage, '${event.dates.allDay}', '${event.dates.multiDay}', '${event.dates.recurrenceLabel}', '${event.venue.online}', '${event.venue.country}', '${event.venue.regionState}');
});

function showLoadingDiv(){
	jQuery('#updateEventLoadingModal').modal({backdrop:'static'});
}

</script>