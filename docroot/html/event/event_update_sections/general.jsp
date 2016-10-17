<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<portlet:defineObjects />

<div class="accordion-group">
	<div class="accordion-heading">
		<a class="accordion-toggle" data-toggle="collapse" href="#generalDetails">
			<liferay-ui:message key="section.eventupdate.general"/>
		</a>
	</div>
	<div id="generalDetails" class="accordion-body collapse in">
		<div class="accordion-inner">
			<div class="row-fluid">
				<div class="span12">
					<i:validationErrors fieldName="title"/>
					<aui:input name="title" type="text" value="${event.title}" label="event.title" showRequiredLabel="false">
						<aui:validator name="required"/>
						<aui:validator name="max-length">75</aui:validator>
					</aui:input>
				</div>
			</div>
			
			<div class="row-fluid margin-15">
				<div class="span12">
					<i:validationErrors fieldName="summary"/>
					<aui:input name="summary" id="summary" type="textarea" value="${event.summary}" label="summary" showRequiredLabel="false" />
				</div>
			</div>
			
			<div class="row-fluid margin-15">
				<div class="span12">
					<i:validationErrors fieldName="description"/>
					<label class="aui-field-label"><liferay-ui:message key="event.description"/></label>
					<liferay-ui:input-editor toolbarSet="email" width="450" height="480" initMethod="initEditor" resizable="false" onChangeMethod="changeEditor"/>
			   		<aui:input name="description" id="description" type="hidden" value="${event.description}" />
			  	</div>
			</div>
			
			<c:if test="${not empty securityLevels}">
				<div class="row-fluid margin-15">
					<div class="span12">
						<aui:select name="securityLevel" multiple="false" showEmptyOption="false" label="event.securityLevel" helpMessage="${securityLevelsHelpText}">
							<c:forEach items="${securityLevels}" var="securityLevel">
								<aui:option value="${securityLevel.getKey()}" label="${securityLevel.getValue()}" selected="${securityLevel.getKey().equals(event.getSecurityLevelKey())}"/>
							</c:forEach>
						</aui:select>
					</div>
				</div>
			</c:if>
			
			<c:if test="${canConfigureFeaturedEvents}">
				<div class="row-fluid margin-15">
					<div class="span12">
						<aui:input name="featuredEvent" type="checkbox" value="${event.featuredEvent}" label="event.featured"/>
						<p class="muted no-margin"><liferay-ui:message key="event.featured.help"/></p>
					</div>
				</div>
			</c:if>
		</div>
	</div>
</div>
<script type="text/javascript">
function <portlet:namespace />initEditor() {
	return '${event.descriptionForEditor}';
}

function <portlet:namespace />changeEditor() {
	document.<portlet:namespace />updateEventForm.<portlet:namespace />description.value = window.<portlet:namespace />editor.getHTML();
}
</script>
