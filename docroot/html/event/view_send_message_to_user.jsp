<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>

<portlet:defineObjects />

<div class="container-fluid">
	<portlet:actionURL var="sendMessageToUserURL" windowState="normal">
		<portlet:param name="action" value="contactUser"/>
		<portlet:param name="eventId" value="${eventId}"/>
		<portlet:param name="receiverEmailAddress" value="${receiverEmailAddress}"/>
	</portlet:actionURL>
	<c:set var="namespace"><portlet:namespace/></c:set>
		
	<aui:form action="${sendMessageToUserURL}" id="sendMessageToUserForm" name="sendMessageToUserForm" inlineField="false">
		<div class="row-fluid">
			<div class="span12">
				<aui:input name="emailBody" type="textarea" value="" label="event.message.user.body" showRequiredLabel="false" cssClass="emailInvitesText"/>			
			</div>
			 
			<div class="row-fluid margin-15">
				<div class="span12">
					<div id="sendMessagePanelFooder">
						<c:set var="alertMessage"><liferay-ui:message key="warn.event.message.empty"/></c:set>
						<a href="javascript:void(0);" onclick="submitSendMessageToUserForm('${namespace}', '${alertMessage}');" class="btn btn-primary">
							<span class="icon-envelope"></span>&nbsp;<liferay-ui:message key="button.send.message.to.user"/>
						</a>
						<a href="javascript:void(0);" onClick="event.preventDefault();Liferay.Intelligus.closePopup();" class="btn btn-default">
							<span class="icon-remove"></span>&nbsp;<liferay-ui:message key="button.close"/>
						</a>
					</div>
					<div style="display:none;" id="loadingSendMessagePanelFooder">
						<h4><liferay-ui:message key="info.loading.sending.message"/><span class="pull-right loadingGif"></span></h4>
					</div>
				</div>
			</div>
		</div>
	</aui:form>
</div>
