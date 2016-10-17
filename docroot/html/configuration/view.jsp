<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://alloy.liferay.com/tld/aui" prefix="aui" %>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<portlet:defineObjects />

<portlet:actionURL name="updatePreferences" var="updateDefaultPrefenrencesUrl" />

<aui:form action="${updateDefaultPrefenrencesUrl}" method="POST" name="updateEventsPreferencesForm" id="updateEventsPreferencesForm" >
	<div class="container-fluid">
		<div class="panel panel-default">
			<div class="panel-heading">
				<div class="panel-title"><liferay-ui:message key="page.title.preferences"/></div>
			</div>
			<div class="panel-body accordion">
				<div class="row-fluid">
					<div class="span12">
						<liferay-ui:success key="preferences-updated" message="success.preferences.updated" />
					</div>
				</div>
				
				
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse" href="#generalDetails">
							<liferay-ui:message key="section.configuration.general"/>
						</a>
					</div>
					<div id="generalDetails" class="accordion-body collapse in">
						<div class="accordion-inner">
							<div class="row-fluid">
								<div class="span4">
									<i:validationErrors fieldName="configuration.scopeGuestGroup"/>
									<aui:select name="eventsScope" id="eventsScope" showEmptyOption="false" label="configuration.scope">
										<c:forEach items="${availableScopes}" var="availableScope">
											<aui:option value="${availableScope}" selected="${eventConfiguration.configuredScope.equals(availableScope)}">
												<liferay-ui:message key="configuration.${availableScope}"/>
											</aui:option>
										</c:forEach>
									</aui:select>
									<p class="muted no-margin"><liferay-ui:message key="configuration.scope.help"/></p>
								</div>
								
								<div class="span4">
									<i:validationErrors fieldName="configuration.maxEventsToShow"/>
									<aui:input name="maxEvents" type="number" value="${eventConfiguration.maxEventsToShow}" label="configuration.maxEvents" />
									<p class="muted no-margin"><liferay-ui:message key="configuration.maxEvents.help"/></p>
								</div>
								
								<div class="span4">
									<aui:select name="eventsViewMode" id="eventsViewMode" showEmptyOption="false" label="configuration.viewMode">
										<c:forEach items="${availableViewModes}" var="availableViewMode">
											<aui:option value="${availableViewMode}" selected="${eventConfiguration.configuredViewMode.equals(availableViewMode)}">
												<liferay-ui:message key="configuration.${availableViewMode}"/>
											</aui:option>
										</c:forEach>
									</aui:select>
									<p class="muted no-margin"><liferay-ui:message key="configuration.viewMode.help"/></p>
								</div>
							</div>
						</div>
					</div>
				</div>
				
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse" href="#featuredDetails">
							<liferay-ui:message key="section.configuration.featured"/>
						</a>
					</div>
					<div id="featuredDetails" class="accordion-body collapse in">
						<div class="accordion-inner">
							<div class="row-fluid">
								<div class="span6">
									<aui:input name="includeFeatured" type="checkbox" value="${eventConfiguration.includeFeaturedEvents}" label="configuration.featured"/>
									<p class="muted no-margin"><liferay-ui:message key="configuration.featured.help"/></p>
								</div>
								<div class="span6">
									<i:validationErrors fieldName="configuration.maxFeaturedEventsToShow"/>
									<aui:input name="maxFeaturedEvents" type="number" value="${eventConfiguration.maxFeaturedEventsToShow}" label="configuration.maxFeaturedEvents" />
									<p class="muted no-margin"><liferay-ui:message key="configuration.maxFeaturedEvents.help"/></p>
								</div>
							</div>
						</div>
					</div>
				</div>
				<c:if test="${eventbriteEnabled && not isGuestGroup}">
					<div class="accordion-group">
						<div class="accordion-heading">
							<a class="accordion-toggle" data-toggle="collapse" href="#eventbriteDetails">
								<liferay-ui:message key="section.configuration.eventbrite"/>
							</a>
						</div>
						<div id="eventbriteDetails" class="accordion-body collapse in">
							<div class="accordion-inner">
								<div class="row-fluid">
									<div class="span12">
										<aui:input name="eventbriteUserKey" type="text" value="${eventConfiguration.customEventbriteUserKey}" label="configuration.eventbriteUserKey"/>
										<p class="muted no-margin"><liferay-ui:message key="configuration.eventbriteUserKey.help"/></p>
									</div>
								</div>
								<div class="row-fluid">
									<div class="span12">
										<aui:input name="liferayCreationDisabled" type="checkbox" value="${eventConfiguration.liferayCreationDisabled}" label="configuration.liferayCreationDisabled"/>
										<p class="muted no-margin"><liferay-ui:message key="configuration.liferayCreationDisabled.help"/></p>
									</div>
								</div>
							</div>
						</div>
					</div>
				</c:if>
			</div>
			<div class="panel-footer">
				<a href="javascript:void(0);" onclick="submitForm('<portlet:namespace />updateEventsPreferencesForm');" class="btn btn-primary"><span class="icon-ok"></span>&nbsp;<liferay-ui:message key="button.save"/></a>
				<portlet:renderURL var="backURL" portletMode="view" windowState="normal"/>
				<a href="${backURL}" class="btn btn-default"><span class="icon-share-alt"></span>&nbsp;<liferay-ui:message key="button.back"/></a>
			</div>
		</div>
	</div>
</aui:form>

<script type="text/javascript">
function submitForm(formId){
	jQuery('#'+formId).submit();
	return false;
}
</script>
