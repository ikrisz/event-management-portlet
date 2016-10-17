<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>

<theme:defineObjects/>

<div class="row-fluid separator-row">
	<div class="span12">
		<liferay-ui:asset-tags-summary 	className="${calEventClassName}" classPK="${eventView.eventId}" message=""/>
	</div>
</div>
<div class="row-fluid separator-row">
	<div class="span6">
		<liferay-ui:ratings classPK="${eventView.eventId}"	className="${calEventClassName}" type="thumbs" />
	</div>
	<div class="span6">
		<liferay-ui:flags className="${calEventClassName}" classPK="${eventView.eventId}" contentTitle="${eventView.title}" reportedUserId="${themeDisplay.getUserId()}"/>
	</div>
</div>

<div class="row-fluid">
	<div class="span12">
		<liferay-ui:panel collapsible="true" cssClass="lfr-document-library-comments" extended="true" persistState="true" title="comments">
			<portlet:actionURL var="updateEventDiscussionMessageURL" >
				<portlet:param name="action" value="updateEventDiscussionMessage"/>
			</portlet:actionURL>
			
			<liferay-ui:discussion 
					formName="fm" 
					formAction="${updateEventDiscussionMessageURL}"
					className="${calEventClassName}"
					classPK="${eventView.eventId}" 
					userId="${eventView.userId}"		
					ratingsEnabled="false"
					subject="${eventView.title}"
					/>
		</liferay-ui:panel>
	</div>
</div>

