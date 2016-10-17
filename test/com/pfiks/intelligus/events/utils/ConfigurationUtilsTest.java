package com.pfiks.intelligus.events.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.ClassesConstructorsTest;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;

public class ConfigurationUtilsTest extends ClassesConstructorsTest {

    private final EventPortletConfiguration mockConfiguration = mock(EventPortletConfiguration.class);

    @Test
    public void testThatCustomEventbriteUserKeyIsReturnedIfConfiguredInPreferences() {
	final String customUserKey = "customApiKey";
	when(mockConfiguration.getCustomEventbriteUserKey()).thenReturn(customUserKey);

	final String eventbriteUserKey = configurationUtils.getEventbriteUserKey(mockConfiguration);
	assertThat(eventbriteUserKey, is(customUserKey));
    }

    @Test
    public void testThatDefaultEventbriteUserKeyIsReturnedWhenNoCustomKeyIsConfiguredInPreferences() {
	when(mockConfiguration.getCustomEventbriteUserKey()).thenReturn(StringPool.BLANK);
	final String eventbriteUserKey = configurationUtils.getEventbriteUserKey(mockConfiguration);
	assertThat(eventbriteUserKey, is(defaultEventbriteUserKey));
    }

}
