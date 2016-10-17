package com.pfiks.intelligus.events.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.model.event.EventbriteDetails;

public class EventbriteGeneralFieldsValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_eventbriteCurrency_alwaysMadatory() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setCurrency(StringPool.BLANK);
	event.setEventbrite(eventbrite);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.currency-required");
    }

    @Test
    public void testThat_eventbriteCurrency_alwaysMaxLenght_10() {
	final EventbriteDetails eventbrite = anEventbriteDetailsModel();
	eventbrite.setCurrency(StringUtils.rightPad(eventbrite.getCurrency(), 11));
	event.setEventbrite(eventbrite);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.currency-too-long");
    }

    @Test
    public void testThat_eventbriteCurrency_isValid() {
	validate_onlyEventbrite();
	assertThat_allErrors_NeverContain("eventbrite.currency-required");
	assertThat_allErrors_NeverContain("eventbrite.currency-too-long");
    }

}
