<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="aui" uri="http://alloy.liferay.com/tld/aui"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="i" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="eventUtils" uri="eventUtilsTag" %>

<div class="accordion-group">
	<div class="accordion-heading">
		<a class="accordion-toggle" data-toggle="collapse" href="#datesDetails">
			<liferay-ui:message key="section.eventupdate.date"/>
		</a>
	</div>
	<div id="datesDetails" class="accordion-body collapse in">
		<div class="accordion-inner">
			<div class="row-fluid">
				<div class="span12">
					<aui:input name="dates.allDay" id="datesAllDay" label="dates.allDay" type="checkbox" value="${event.dates.allDay}" />
				</div>
			</div>
			<c:if test="${showMultidayDates}">
				<div class="row-fluid margin-15">
					<div class="span12">
						<aui:input name="dates.multiDay" id="datesMultiDay" label="dates.multiDay" type="checkbox" value="${event.dates.multiDay}" />
						<p class="muted no-margin"><liferay-ui:message key="dates.multiDay.help"/></p>
					</div>
				</div>
			</c:if>
			
			<div class="row-fluid">
				<div class="span12">
					<i:validationErrors fieldName="dates.startDate"/>
					<i:validationErrors fieldName="dates.startDate.eventbrite"/>
					<i:validationErrors fieldName="dates.endDate"/>
				 </div>
			</div>
			<div class="row-fluid margin-15">
				<div class="span6">
					<label class="control-label"><liferay-ui:message key="dates.startDate"/> </label>
					<liferay-ui:input-date firstDayOfWeek="${firstDayOfWeek}" disabled="false"
							dayParam="startDay" dayValue="${event.dates.startDay}"
							monthParam="startMonth" monthValue="${event.dates.startMonth}"
							yearParam="startYear" yearValue="${event.dates.startYear}" />
				</div>
				<div class="span1">
					<aui:select name="dates.startHour" id="datesStartHour" multiple="false" showEmptyOption="false" label="dates.startHour" cssClass="time-selection">
						<c:forEach var="val" step="1" begin="0" end="23">
							<aui:option value="${val}" selected="${val == event.dates.startHour}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
						</c:forEach>
					</aui:select>
				</div>
				<div class="span2">
					<aui:select name="dates.startMinute" id="datesStartMinute" multiple="false" showEmptyOption="false" label="dates.startMinute" cssClass="time-selection">
						<c:forEach var="val" step="1" begin="0" end="59">
							<aui:option value="${val}" selected="${val == event.dates.startMinute}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
						</c:forEach>
					</aui:select>
				</div>
				<div class="span3 hidden-phone hidden-tablet">&nbsp;</div>
			</div>
			<div class="row-fluid margin-15">
				<div class="span6">
					<c:if test="${showMultidayDates}">
						<div class="" id="${namespace}multidaySelectionDiv">
							<label class="control-label"><liferay-ui:message key="dates.endDate"/> </label>
							<liferay-ui:input-date firstDayOfWeek="${firstDayOfWeek}" disabled="false" 
									dayParam="endDay" dayValue="${event.dates.endDay}"
									monthParam="endMonth" monthValue="${event.dates.endMonth}"
									yearParam="endYear" yearValue="${event.dates.endYear}" />
						</div>
					</c:if>
					<div class="" id="${namespace}multidayEmptyDiv">&nbsp;
					</div>
				</div>
				<div class="span1">
					<aui:select name="dates.endHour" id="datesEndHour" multiple="false" showEmptyOption="false" label="dates.endHour" cssClass="time-selection">
						<c:forEach var="val" step="1" begin="0" end="23">
							<aui:option value="${val}" selected="${val == event.dates.endHour}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
						</c:forEach>
					</aui:select>
				</div>
				<div class="span2">
					<aui:select name="dates.endMinute" id="datesEndMinute" multiple="false" showEmptyOption="false" label="dates.endMinute" cssClass="time-selection">
						<c:forEach var="val" step="1" begin="0" end="59">
							<aui:option value="${val}" selected="${val == event.dates.endMinute}"><fmt:formatNumber value="${val}"  pattern="00"/></aui:option>
						</c:forEach>
					</aui:select>
				</div>
				<div class="span3 hidden-phone hidden-tablet">&nbsp;</div>
			</div>
			
			<c:if test="${showRecurrencyDates}">
				<div class="row-fluid margin-15" id="${namespace}recurrenceSelectionDiv">
					<div class="span12">
						<i:validationErrors fieldName="dates.recurrenceType"/>
						<aui:select name="dates.recurrenceLabel" label="dates.recurrence" id="recurrenceLabel">
							<c:forEach var="entry" items="${recurrenceTypes}">
								<aui:option value="${entry.label}" selected="${entry.label.equals(event.dates.recurrenceLabel)}">
									<liferay-ui:message key="recurrence.${entry.label}"/>
								</aui:option>
							</c:forEach>
						</aui:select>
					</div>
				</div>
				
				<div class="row-fluid margin-15" id="${namespace}recurrenceIntervalValuesDiv">
					<div class="span12">
						<div class="row-fluid" id="${namespace}weekIntervalDiv">
							<div class="span12" >
								<i:validationErrors fieldName="dates.recurrenceWeekInterval"/>
								<div class="inline-fields">
									<span class="control-label"><liferay-ui:message key="recurrence.repeat.every"/> &nbsp;</span>
									<aui:select name="dates.recurrenceWeekInterval" id="recurrenceWeekInterval" label="" cssClass="time-selection">
										<c:forEach step="1" begin="1" end="12" var="weekIndex">
											<aui:option value="${weekIndex}" selected="${weekIndex == event.dates.recurrenceWeekInterval}">${weekIndex}</aui:option>
										</c:forEach>
									</aui:select>
									<span class="control-label">&nbsp; <liferay-ui:message key="weeks"/></span>
								</div>
							</div>
						</div>
						<div class="row-fluid" id="${namespace}monthIntervalDiv" >
							<div class="span12" >
								<i:validationErrors fieldName="dates.recurrenceMonthInterval"/>
								<div class="inline-fields">
									<span class="control-label"><liferay-ui:message key="recurrence.repeat.every"/> &nbsp;</span>
									<aui:select name="dates.recurrenceMonthInterval" id="recurrenceMonthInterval" label="" cssClass="time-selection">
										<c:forEach step="1" begin="1" end="12" var="monthIndex">
											<aui:option value="${monthIndex}" selected="${monthIndex == event.dates.recurrenceMonthInterval}">${monthIndex}</aui:option>
										</c:forEach>
									</aui:select>
									<span class="control-label">&nbsp; <liferay-ui:message key="months"/></span>
								</div>
							</div>
						</div>
						<div class="row-fluid" id="${namespace}dayIntervalDiv" >
							<div class="span12" >
								<i:validationErrors fieldName="dates.recurrenceDayInterval"/>
								<div class="inline-fields">
									<span class="control-label"><liferay-ui:message key="recurrence.repeat.every"/> &nbsp;</span>
									<aui:select name="dates.recurrenceDayInterval" id="recurrenceDayInterval" label="" cssClass="time-selection">
										<c:forEach step="1" begin="1" end="31" var="dayIndex">
											<aui:option value="${dayIndex}" selected="${dayIndex == event.dates.recurrenceDayInterval}">${dayIndex}</aui:option>
										</c:forEach>
									</aui:select>
									<span class="control-label"><span id="${namespace}dayIntervalLabelPostDiv"></span></span>
								</div>
							</div>
						</div>
						<div class="row-fluid margin-15" id="${namespace}dayMultiIntervalDiv">	
							<div class="span12" >
								<i:validationErrors fieldName="dates.recurrenceDayInterval"/>
								<div class="inline-fields">
									<span class="control-label"><liferay-ui:message key="recurrence.repeat.on"/> &nbsp;</span>
									<c:forEach items="${days}" var="dayEntry">
										<label class="checkbox-inline"><c:out value="${dayEntry.value}"/></label>
										&nbsp;
										<c:choose>
											<c:when test="${eventUtils:isDaySelectedAsRecurrence(event,dayEntry.key)}">
												<input name="<portlet:namespace/>recurrenceDaysSelection" type="checkbox" value="${dayEntry.key}" checked="checked" />
											</c:when>
											<c:otherwise>
												<input name="<portlet:namespace/>recurrenceDaysSelection" type="checkbox" value="${dayEntry.key}"/>
											</c:otherwise>
										</c:choose>
										&nbsp;&nbsp;
									</c:forEach>
								</div>
							</div>
						</div>
					</div>
				</div>
				
				<div class="row-fluid margin-15" id="${namespace}endDateTimeRecurrenceValuesDiv">
					<div class="span6" id="${namespace}endDateTimeRecurrenceValuesDiv">
						<i:validationErrors fieldName="dates.recurrenceEndDate"/>
						<label class="control-label"><liferay-ui:message key="dates.recurrenceEndDate"/></label>
						<liferay-ui:input-date firstDayOfWeek="${firstDayOfWeek}" disabled="false"
								dayParam="recurrenceEndDay" dayValue="${event.dates.recurrenceEndDay}"
								monthParam="recurrenceEndMonth" monthValue="${event.dates.recurrenceEndMonth}"
								yearParam="recurrenceEndYear" yearValue="${event.dates.recurrenceEndYear}"/>
					</div>
				</div>
			</c:if>
		</div>
	</div>
</div>
