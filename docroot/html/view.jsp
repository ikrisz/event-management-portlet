<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>

<theme:defineObjects/>
<portlet:defineObjects />

<div class="container-fluid">
	<c:choose>
		<c:when test="${invalidConfiguration}">
			<div class="alert alert-danger"><liferay-ui:message key="error.portlet.configuration.invaild"/> </div>
		</c:when>
		<c:otherwise>
			<div class="row-fluid margin-bottom-15">
				<div class="span8 pull-left">
					<div class="btn-toolbar">
						<c:if test="${hasAddPermission && not isGuestGroup}">
							<div class="btn-group pull-left">
								<portlet:renderURL var="addEventViewUrl">
									<portlet:param name="view" value="addEvent"/>
								</portlet:renderURL>
								<portlet:renderURL var="addEventbriteEventViewUrl">
									<portlet:param name="view" value="addEventbriteEvent"/>
								</portlet:renderURL>
								<portlet:renderURL var="importEventViewUrl" >
									<portlet:param name="view" value="importableEventbriteEvents"/>
								</portlet:renderURL>
								<c:choose>
									<c:when test="${not eventbriteEnabled || (eventbriteEnabled && not hasAddEventbritePermission)}">
										<a href="${addEventViewUrl}" class="btn btn-primary">
											<span class="icon-plus"></span>&nbsp;
											<liferay-ui:message key="button.create.new"/> 
										</a>
									</c:when>
									<c:otherwise>
										<div class="btn-group">
										  <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
										  		<liferay-ui:message key="button.add"/>&nbsp;<span class="caret"></span>
										  </button>
										  <ul class="dropdown-menu" role="menu">
										  	<c:if test="${not liferayCreationDisabled}">
											    <li>
											    	<a href="${addEventViewUrl}" class="no-underline">
											    		<span class="icon-plus"></span>&nbsp;
											    		<liferay-ui:message key="button.create.new"/>
											    	</a>
											    </li>
										    </c:if>
										    <li class="fb-second-drop-down-button">
										    	<a href="${addEventbriteEventViewUrl}" class="no-underline">
										    		<span class="icon-plus"></span>&nbsp;
										    		<liferay-ui:message key="button.create.new.eventbrite"/>
										    	</a>
										    </li> 
										    <c:if test="${not empty customEventbriteKey}">
											    <li class="fb-second-drop-down-button">
											    	<a href="${importEventViewUrl}" class="no-underline">
											    		<span class="icon-download"></span>&nbsp;
											    		<liferay-ui:message key="button.eventbrite.import"/>
											    	</a>
											    </li>
										    </c:if>
										  </ul>
										</div>
									</c:otherwise>
								</c:choose>
							</div>
						</c:if>
					
						<div class="btn-group pull-left">
							<portlet:renderURL var="viewListUrl">
								<portlet:param name="viewMode" value="viewMode.list"/>
								<portlet:param name="viewFutureEvents" value="true"/>
							</portlet:renderURL>
							<a href="${viewListUrl}" class="btn btn-default ${isMonthView ? '' : 'active'}" title="List view"><span class="icon-th-list"></span></a>
				
							<portlet:renderURL var="viewMonthUrl">
								<portlet:param name="viewMode" value="viewMode.calendar"/>
							</portlet:renderURL>
							<a href="${viewMonthUrl}"  class="btn btn-default ${isMonthView ? 'active' : ''} hidden-phone" title="Calendar view"><span class="icon-th"></span></a>
						</div>
					
						<c:if test="${not isMonthView}">
							<div class="btn-group pull-left switch-view">
								<c:choose>
									<c:when test="${performSearch}">
										<portlet:renderURL var="clearSearchURL">
											<portlet:param name="viewFutureEvents" value="true"/>
										</portlet:renderURL>
										<a href="${clearSearchURL}" class="btn btn-warning" title="Clear search filter">
											<span class="icon-zoom-in"></span>&nbsp;<liferay-ui:message key="button.search.clear"/>
										</a>
									</c:when>
									<c:when test="${viewPastEvents}">
										<portlet:renderURL var="viewFutureEventsURL">
											<portlet:param name="viewFutureEvents" value="true"/>
										</portlet:renderURL>
										<a href="${viewFutureEventsURL}" class="btn btn-success" title="View future events">
											<span class="icon-time"></span>&nbsp;<liferay-ui:message key="button.events.future"/>
										</a>
									</c:when>
									<c:otherwise>
										<portlet:renderURL var="viewPastEventsURL">
											<portlet:param name="viewFutureEvents" value="false"/>
											<portlet:param name="viewPastEvents" value="true"/>
										</portlet:renderURL>
										<a href="${viewPastEventsURL}" class="btn btn-warning" title="View past events">
											<span class="icon-time"></span>&nbsp;<liferay-ui:message key="button.events.past"/>
										</a>
									</c:otherwise>
								</c:choose>
							</div>
						</c:if>
					</div>
				</div>
				
				<div class="span4 pull-right">
					<portlet:actionURL var="searchEventURL">
						<portlet:param name="action" value="searchEvent"/>
					</portlet:actionURL>
					
					<form class="form-search" name="<portlet:namespace />portletSearchForm" action="${searchEventURL}" method="post" id="portletSearchForm">
						<span class="search-form input-append">
							<input type="text" placeholder="Search within events" class="span2 search-query" id="searchText" name="<portlet:namespace/>searchText" value="${searchText}"/>
							<button class="btn" type="submit"><i class="icon-search icon-white"></i></button>
						</span>
					</form>
				</div>
			</div>
		
			<liferay-ui:success key="event-created" message="success.event.added" />
			<liferay-ui:success key="event-updated" message="success.event.updated" />
			<liferay-ui:success key="event-deleted" message="success.event.deleted" />
			<liferay-ui:success key="people-invited" message="success.event.people.invited" />
			
			<c:choose>
				<c:when test="${isMonthView}">
					<jsp:include page="view_calendar.jsp"/>
				</c:when>
				<c:otherwise>
					<jsp:include page="view_list.jsp"/>
				</c:otherwise>
			</c:choose>	
		</c:otherwise>
	</c:choose>
</div>

<style>
.margin-bottom-15{
	margin-bottom:15px;
}
</style>
