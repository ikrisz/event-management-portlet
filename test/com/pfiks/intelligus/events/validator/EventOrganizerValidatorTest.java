package com.pfiks.intelligus.events.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.model.event.EventOrganizer;

public class EventOrganizerValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_newOrganizer_organizerName_alwaysMadatory() {
	final EventOrganizer organizer = aNewOrganizer();
	organizer.setName(StringPool.BLANK);
	event.setOrganizer(organizer);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("organizer.name-required");
    }

    @Test
    public void testThat_newOrganizer_organizerName_alwaysMaxLenght_100() {
	final EventOrganizer organizer = aNewOrganizer();
	organizer.setName(StringUtils.rightPad(organizer.getName(), 101));
	event.setOrganizer(organizer);
	validate();
	assertThat_EventbriteErrors_AlwaysContain("organizer.name-too-long");
    }

    @Test
    public void testThat_previousOrganizerSelection_isValid() {
	event.setOrganizer(anExistingOrganizer());
	validate();
	assertThat_allErrors_NeverContain("organizer.name-required");
    }

    @Test
    public void testThat_newOrganizer_isValid() {
	event.setOrganizer(aNewOrganizer());
	validate();
	assertThat_allErrors_NeverContain("organizer.name-required");
	assertThat_allErrors_NeverContain("organizer.name-too-long");
    }

}
