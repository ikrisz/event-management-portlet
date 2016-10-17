package com.pfiks.intelligus.events.validator;

import org.joda.time.DateTime;
import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.model.event.EventDates;

public class EventDatesValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_eventStartDate_alwaysMadatory() {
	final EventDates dates = someValidEventDates();
	dates.setStartDate(null);
	event.setDates(dates);
	validate();
	assertThat_allErrors_AlwaysContain("dates.startDate-required");
    }

    @Test
    public void testThat_eventEndDate_alwaysMadatory() {
	final EventDates dates = someValidEventDates();
	dates.setEndDate(null);
	event.setDates(dates);
	validate();
	assertThat_allErrors_AlwaysContain("dates.endDate-required");
    }

    @Test
    public void testThat_eventEndDate_alwaysAfter_eventStartDate() {
	final EventDates dates = someValidEventDates();
	dates.setEndDate(dates.getStartDate().minusDays(5));
	event.setDates(dates);
	validate();
	assertThat_allErrors_AlwaysContain("dates.endDate-invalid");
    }

    @Test
    public void testThat_eventStartDate_alwaysFutureIfEventbriteEvent() {
	final EventDates dates = someValidEventDates();
	final DateTime now = DateTime.now();
	dates.setStartDate(now.minusDays(5));
	event.setDates(dates);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("dates.startDate.eventbrite-invalid");
    }

    @Test
    public void testThat_eventStartDate_anyDateIfEventbriteDisabled() {
	event.getEventbrite().setEventbriteId(StringPool.BLANK);
	final EventDates dates = someValidEventDates();
	final DateTime now = DateTime.now();
	dates.setStartDate(now.minusDays(5));
	event.setDates(dates);
	validate();
	assertThat_EventErrors_NeverContain("dates.startDate.eventbrite-invalid");
    }

    @Test
    public void testThat_eventDates_areValid() {
	validate();
	assertThat_allErrors_NeverContain("dates.startDate-required");
	assertThat_allErrors_NeverContain("dates.endDate-required");
	assertThat_allErrors_NeverContain("dates.endDate-invalid");
	assertThat_allErrors_NeverContain("dates.startDate.eventbrite-invalid");
	assertThat_allErrors_NeverContain("dates.startDate.eventbrite-invalid");
    }

}
