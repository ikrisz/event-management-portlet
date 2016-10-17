<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>

<theme:defineObjects/>
<portlet:defineObjects />

<c:set var="namespace">
	<portlet:namespace/>
</c:set>

<c:choose>
	<c:when test="${empty featuredEvents}">
		<c:set var="eventListCssClass" value="span12"/>
	</c:when>
	<c:otherwise>
		<c:set var="eventListCssClass" value="span8"/>
	</c:otherwise>
</c:choose>

<div class="row-fluid">
	<div class="${eventListCssClass}">
		<c:choose>
			<c:when test="${empty events}">
				<div class="alert alert-warning">
					<liferay-ui:message key="warn.no.events.found"/>
				</div>
			</c:when>
			<c:otherwise>
				<c:forEach items="${events}" var="event" varStatus="loop">
					<div class="row-fluid ${loop.last ? 'separator-row-last' : 'separator-row' }" id="eventRow-${loop.index}">
						<div class="span2 centered-content hidden-tablet hidden-phone">
							<span class="icon-calendar large-icon"></span>
							<liferay-ui:ratings classPK="${event.eventId}"	className="${calEventClassName}" type="thumbs" />
						</div>		
						<div class="span8">
							<portlet:renderURL var="eventDetailsURL" windowState="normal">
								<portlet:param name="view" value="eventFullDetails"/>
								<portlet:param name="eventId" value="${event.eventId}"/>
								<portlet:param name="eventUid" value="${event.eventUid}"/>
							</portlet:renderURL>
							<h4 class="no-margin"><a href="${eventDetailsURL}"><c:out value="${event.title}"/></a></h4>	
							
							<p class="no-margin"><i:eventDatesDetails event="${event}" currentStartDate="${event.dates.startDate}"/></p>
							
							<p class="no-margin">
								<c:choose>
									<c:when test="${event.venue.online}">
										<strong><liferay-ui:message key="online"/></strong>
									</c:when>
									<c:otherwise>
										<c:out value="${eventUtils:getShortVenueLocation(event, themeDisplay)}"/>
									</c:otherwise>
								</c:choose>
							</p>
							<c:if test="${event.user != null}">
								<p><liferay-ui:message key="creator"/> ${event.user.fullName}</p>
							</c:if>
						</div>
						<div class="span2 hidden-tablet hidden-phone">
							<a href="javascript:void(0);" onclick="showEventQuickLook('event','${loop.index}');"  class="btn btn-mini btn-default">
								<liferay-ui:message key="button.quick.look"/>
							</a>
							<i:eventHoverDetails eventView="${event}" eventHoverDivId="eventListHover-${loop.index}"/>
						</div>
					</div>
				</c:forEach>
			</c:otherwise>
		</c:choose>
		
		<!-- Pagination -->
		<c:if test="${pagination.totalPages > 1}">
			<portlet:renderURL var="paginateURL">
				<portlet:param name="searchText" value="${searchText}" />
				<portlet:param name="viewPastEvents" value="${viewPastEvents}"/>
				<portlet:param name="viewFutureEvents" value="${viewFutureEvents}"/>
				<portlet:param name="performSearch" value="${performSearch}"/>
			</portlet:renderURL>
			<div class="row-fluid">
				<div class="span12">
					<div class="pagination">
						<ul>
							<c:if test="${(pagination.currentPage-1) > 0}">
								<li class="previous"><a href="javascript:void(0);" onclick="updateResultsPage('${paginateURL}', '${namespace}', '${pagination.currentPage-1}');">&laquo;</a></li>
							</c:if>
							<c:forEach begin="${pagination.rangeLow}" end="${pagination.totalPages < pagination.rangeHigh ? pagination.totalPages: pagination.rangeHigh}" var="item" step="1">
								<c:choose>
									<c:when test="${item == pagination.currentPage }">
										<li class="active"><a href="javascript:void(0);"><span>${item}</span></a></li>
									</c:when>
									<c:otherwise>
										<li><a href="javascript:void(0);" onclick="updateResultsPage('${paginateURL}', '${namespace}', '${item}');"><span>${item}</span></a></li>
									</c:otherwise>
								</c:choose>
							</c:forEach>
							<c:if test="${(pagination.currentPage + 1)  <= pagination.totalPages}">
								<li class="next"><a href="javascript:void(0);" onclick="updateResultsPage('${paginateURL}', '${namespace}', '${pagination.currentPage+1}');">&raquo;</a></li>
							</c:if>
						</ul>
					</div>
				</div>
			</div>
		</c:if>
	</div>
	
	<c:if test="${not empty featuredEvents}">
		<div class="span4">
			<jsp:include page="view_featured_events.jsp"/>
		</div>
	</c:if>
</div>

<script type="text/javascript">
	function showEventQuickLook(divId, divIndex){
		var parentRow = '#'+divId+'Row-'+divIndex;
		var divIdToShow = '#'+divId+'ListHover-'+divIndex;
		var row_top_updated = 0;
		var initial_top = jQuery(parentRow).position().top;
		if(initial_top == 0){
			 	row_top_updated = 0;
		} else if(initial_top > 2000){ 
			row_top_updated =initial_top - 200;
		}else{
			row_top_updated = initial_top - 100; 
		}
		jQuery(divIdToShow).attr('style','top: '+row_top_updated+'px !important');
		$(divIdToShow).fadeIn(10,function(){$(this).focus();});
	};
	
	function closeEventQuickLookPopup(){
		var currentHeight= jQuery(window).scrollTop();
		$('.eventQuickLookPopup').fadeOut(50);
		jQuery(window).scrollTop(currentHeight);
	}
	
	$(".eventQuickLookPopup").on('blur',function(){
		setTimeout(function() {
			var currentHeight= jQuery(window).scrollTop();
			$('.eventQuickLookPopup').fadeOut(50);
		    jQuery(window).scrollTop(currentHeight);
		},50);
	});
</script>