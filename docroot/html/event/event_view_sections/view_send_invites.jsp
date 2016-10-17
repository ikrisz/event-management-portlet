<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
			
<c:if test="${not empty eventView.eventbrite.eventbriteId}">
	<portlet:defineObjects />
	
	<portlet:actionURL var="inviteUserURL" windowState="normal">
		<portlet:param name="action" value="invitePeople"/>
		<portlet:param name="eventId" value="${eventView.eventId}"/>
	</portlet:actionURL>
	
	<div id="sendInvitesLoadingModal" style="display:none;" class="modal" tabindex="-1" role="dialog" aria-hidden="true">
		<div class="modal-body">
			<h4><liferay-ui:message key="info.loading.sending.invites"/><span class="pull-right loadingGif"></span></h4>
		</div>
	</div>
	
	<aui:form action="${inviteUserURL}" id="invitePeopleForm" name="invitePeopleForm" inlineField="false">
		<div class="accordion-group margin-15">
			<div class="accordion-heading">
				<a class="accordion-toggle" data-toggle="collapse" href="#inviteMembersDetails">
					<span class="icon-envelope"></span>&nbsp;<liferay-ui:message key="section.event.inviteUsers"/>
				</a>
			</div>
			<div id="inviteMembersDetails" class="accordion-body collapse in">
				<div class="accordion-inner">
					<div class="row-fluid">
						<div class="span12">
							<liferay-ui:error key="people-invite-email-required" message="error.people.invite.email.required" />
							<liferay-ui:error key="people-invite-email-invalid">
								<liferay-ui:message key="error.people.invite.emails.invalid" arguments="${emailAddressesToInviteInvalid}"/>
							</liferay-ui:error>
							<p class="muted no-margin"><liferay-ui:message key="event.invite.user.emails.help"/></p>	 
							<aui:input name="emailAddressesToInvite" type="textarea" value="${emailAddressesToInvite}" label="event.invite.user.emails" showRequiredLabel="false" cssClass="emailInvitesText"/>			
						</div>
					</div>
					<div class="row-fluid">
						<div class="span12">
							<a href="javascript:void(0);" onclick="submitInvitePeopleForm('<portlet:namespace />', 'sendInvitesLoadingModal');" class="btn btn-default">
								<span class="icon-envelope"></span>&nbsp;<liferay-ui:message key="button.send.invites"/>
							</a>
						</div>
					</div>
				</div>
			</div>
		</div>
	</aui:form>
</c:if>
			