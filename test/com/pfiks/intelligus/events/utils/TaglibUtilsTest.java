package com.pfiks.intelligus.events.utils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.utils.TaglibUtils;

public class TaglibUtilsTest {

    @Test
    public void testRecurrenceDaySelectionContainsEntry() {
	assertThat(TaglibUtils.isDaySelectedAsRecurrence(anEventWithRecurrencySetting(), 1), is(Boolean.TRUE));
    }

    @Test
    public void testRecurrenceDaySelectionDoesNotContainEntry() {
	assertThat(TaglibUtils.isDaySelectedAsRecurrence(anEventWithRecurrencySetting(), 4), is(Boolean.FALSE));
    }

    @Test
    public void testRecurrenceDaySelectionDoesNotContainEntryIfRecurrenceIsNotSet() {
	final EventModel event = new EventModel();
	assertThat(TaglibUtils.isDaySelectedAsRecurrence(event, 1), is(Boolean.FALSE));
    }

    private EventModel anEventWithRecurrencySetting() {
	final EventModel event = new EventModel();
	event.getDates().setRecurrenceDaySelectionInterval(Lists.newArrayList(1, 2, 3));
	return event;
    }

}
