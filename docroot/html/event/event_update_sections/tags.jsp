<%@page import="com.liferay.portlet.calendar.model.CalEvent"%>
<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>

<aui:model-context bean="${event.calEvent}" model="<%= CalEvent.class %>" />

<div class="accordion-group">
	<div class="accordion-heading">
		<a class="accordion-toggle" data-toggle="collapse" href="#tagsDetails">
			<liferay-ui:message key="section.eventupdate.tags"/>
		</a>
	</div>
	<div id="tagsDetails" class="accordion-body collapse in">
		<div class="accordion-inner">
			<div class="row-fluid">
				<div class="span12">
					<aui:input classPK="${event.eventId}" name="tags" type="assetTags"  />
				</div>
			</div>
		</div>
	</div>
</div>

