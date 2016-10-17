package com.pfiks.intelligus.events.validator;

import org.junit.Test;

import com.pfiks.intelligus.events.model.event.EventDates;

public class EventRecurrencyDatesValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_eventRecurrenceEndDate_madatoryIfRecurrenceTypeIsSelected() {
	final EventDates dates = aDailyRecurrentEventDates();
	dates.setRecurrenceEndDate(null);
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceEndDate-required");
    }

    @Test
    public void testThat__eventRecurrenceEndDate_alwaysAfter_eventEndDate() {
	final EventDates dates = aDailyRecurrentEventDates();
	dates.setRecurrenceEndDate(dates.getEndDate().minusDays(5));
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceEndDate-invalid");
    }

    @Test
    public void testThat_dailyRecurrentEvent_daysIntervals_alwaysMandatory() {
	final EventDates dates = aDailyRecurrentEventDates();
	dates.setRecurrenceDayInterval(0);
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceDayInterval-required");
    }

    @Test
    public void testThat_dailyRecurrentEvent_isValid() {
	event.setDates(aDailyRecurrentEventDates());
	validate();
	assertThat_allErrors_NeverContain("dates.recurrenceEndDate-required");
	assertThat_allErrors_NeverContain("dates.recurrenceEndDate-invalid");
	assertThat_allErrors_NeverContain("dates.recurrenceDayInterval-required");
    }

    @Test
    public void testThat_weeklyRecurrentEvent_weeksIntervals_alwaysMandatory() {
	final EventDates dates = aWeeklyRecurrentEventDates();
	dates.setRecurrenceWeekInterval(0);
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceWeekInterval-required");
    }

    @Test
    public void testThat_weeklyRecurrentEvent_daysIntervals_alwaysMandatory() {
	final EventDates dates = aWeeklyRecurrentEventDates();
	dates.setRecurrenceDaySelectionInterval(null);
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceDayInterval-required");
    }

    @Test
    public void testThat_weeklyRecurrentEvent_isValid() {
	event.setDates(aWeeklyRecurrentEventDates());
	validate();
	assertThat_allErrors_NeverContain("dates.recurrenceEndDate-required");
	assertThat_allErrors_NeverContain("dates.recurrenceEndDate-invalid");
	assertThat_allErrors_NeverContain("dates.recurrenceWeekInterval-required");
	assertThat_allErrors_NeverContain("dates.recurrenceDayInterval-required");
    }

    @Test
    public void testThat_monthlyRecurrentEvent_monthsIntervals_alwaysMandatory() {
	final EventDates dates = aMonthlyRecurrentEventDates();
	dates.setRecurrenceMonthInterval(0);
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceMonthInterval-required");
    }

    @Test
    public void testThat_monthlyRecurrentEvent_daysIntervals_alwaysMandatory() {
	final EventDates dates = aMonthlyRecurrentEventDates();
	dates.setRecurrenceDayInterval(0);
	event.setDates(dates);
	validate();
	assertThat_EventErrors_AlwaysContain("dates.recurrenceDayInterval-required");
    }

    @Test
    public void testThat_monthlyRecurrentEvent_isValid() {
	event.setDates(aMonthlyRecurrentEventDates());
	validate();
	assertThat_allErrors_NeverContain("dates.recurrenceEndDate-required");
	assertThat_allErrors_NeverContain("dates.recurrenceEndDate-invalid");
	assertThat_allErrors_NeverContain("dates.recurrenceMonthInterval-required");
	assertThat_allErrors_NeverContain("dates.recurrenceDayInterval-required");
    }

}
