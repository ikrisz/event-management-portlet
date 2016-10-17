<%@ tag isELIgnored="false" body-content="scriptless" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui"%>
<%@ taglib prefix="theme" uri="http://liferay.com/tld/theme" %>

<%@ attribute name="currentStartDate" required="true" type="org.joda.time.DateTime" %>
<%@ attribute name="event" required="true" type="com.pfiks.intelligus.events.model.event.EventModel" %>

<theme:defineObjects/>

<joda:dateTimeZone value="${themeDisplay.timeZone.ID}" >
	<c:choose>
		<c:when test="${event.dates.allDay}">
			<joda:format value="${currentStartDate}" style="L-" locale="${themeDisplay.locale}" />
			&nbsp;|&nbsp;
			<strong><liferay-ui:message key="allDay"/></strong>
		</c:when>
		<c:when test="${event.dates.multiDay}">
			<joda:format value="${currentStartDate}" style="LS" locale="${themeDisplay.locale}"/>
			&nbsp;|&nbsp;
			<joda:format value="${event.dates.endDate}" style="LS" locale="${themeDisplay.locale}"/>
		</c:when>
		<c:otherwise>
			<joda:format value="${currentStartDate}" style="L-" locale="${themeDisplay.locale}"/>
			&nbsp;|&nbsp;
			<joda:format value="${currentStartDate}" style="-S" locale="${themeDisplay.locale}"/>
			&nbsp;-&nbsp;
			<joda:format value="${event.dates.endDate}" style="-S" locale="${themeDisplay.locale}"/>
		</c:otherwise>
	</c:choose>
</joda:dateTimeZone>