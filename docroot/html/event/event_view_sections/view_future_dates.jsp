<%@ page contentType="text/html" isELIgnored="false"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>

<c:if test="${not empty eventView.dates.recurrenceDates}">
	<div class="accordion-group margin-15">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" href="#datesDetails">
				<span class="icon-calendar"></span>&nbsp;<liferay-ui:message key="section.event.recurrence.dates"/>
			</a>
		</div>
		<div id="datesDetails" class="accordion-body collapse in">
			<div class="accordion-inner maxHeightScrollable">
			    <c:forEach items="${eventView.dates.recurrenceDates}" var="futureDate">
			    	<p><i:eventDatesDetails event="${eventView}" currentStartDate="${futureDate.value}"/></p>
			    </c:forEach>
		    </div>
		</div>
	</div>
</c:if>
