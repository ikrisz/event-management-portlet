package com.pfiks.intelligus.events.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;

public class EventGeneralFieldsValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_title_alwaysMandatory() {
	event.setTitle(StringPool.BLANK);
	validate();
	assertThat_allErrors_AlwaysContain("title-required");
    }

    @Test
    public void testThat_title_alwaysMaxLenght_75() {
	event.setTitle(StringUtils.rightPad(event.getTitle(), 76));
	validate();
	assertThat_allErrors_AlwaysContain("title-too-long");
    }

    @Test
    public void testThat_description_alwaysMaxLenght_2000() {
	event.setDescription(StringUtils.rightPad(event.getDescription(), 2001));
	validate();
	assertThat_allErrors_AlwaysContain("description-too-long");
    }

    @Test
    public void testThat_title_isValid() {
	validate();
	assertThat_allErrors_NeverContain("title-required");
	assertThat_allErrors_NeverContain("title-too-long");
    }

    @Test
    public void testThat_description_isValid() {
	validate();
	assertThat_allErrors_NeverContain("description-too-long");
    }

}
