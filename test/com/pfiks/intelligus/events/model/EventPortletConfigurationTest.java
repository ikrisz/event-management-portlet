package com.pfiks.intelligus.events.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;

public class EventPortletConfigurationTest {

    private EventPortletConfiguration configuration;

    private FakePortletPreferences preferences;

    @Before
    public void setUp() {
	preferences = new FakePortletPreferences();
    }

    @Test
    public void testPreferencesAreAppliedAtTimeOfConstruction() throws Exception {
	preferences.setValue(ConfigurationConstants.CONFIG_SCOPE, ConfigurationConstants.SCOPE_ALL);
	preferences.setValue(ConfigurationConstants.MAX_EVENTS, "5");
	preferences.setValue(ConfigurationConstants.SHOW_FEATURED_EVENTS, "true");
	preferences.setValue(ConfigurationConstants.MAX_FEATURED_EVENTS, "8");
	preferences.setValue(ConfigurationConstants.EVENTBRITE_USER_KEY, "testEmail");
	configuration = new EventPortletConfiguration(preferences);

	assertThat(configuration.getConfiguredScope(), is(ConfigurationConstants.SCOPE_ALL));
	assertThat(configuration.getMaxEventsToShow(), is(5));
	assertThat(configuration.getIncludeFeaturedEvents(), is(true));
	assertThat(configuration.getMaxFeaturedEventsToShow(), is(8));
	assertThat(configuration.getCustomEventbriteUserKey(), is("testEmail"));
    }

    @Test
    public void testDefaultPreferencesValuesAreStoredIfNoneSpecified() throws Exception {
	configuration = new EventPortletConfiguration(preferences);
	configuration.savePreferences(preferences);

	assertThat(configuration.getConfiguredScope(), is(ConfigurationConstants.SCOPE_GROUP));
	assertThat(configuration.getMaxEventsToShow(), is(Integer.valueOf(ConfigurationConstants.DEFAULT_CONFIG_NUMBER)));
	assertThat(configuration.getIncludeFeaturedEvents(), is(Boolean.valueOf(ConfigurationConstants.DEFAULT_CONFIG_ENABLED)));
	assertThat(configuration.getMaxFeaturedEventsToShow(), is(Integer.valueOf(ConfigurationConstants.DEFAULT_CONFIG_NUMBER)));
	assertThat(configuration.getCustomEventbriteUserKey(), is(StringPool.BLANK));
    }

    @Test
    public void testPreferencesAreStored() throws Exception {
	configuration = new EventPortletConfiguration(preferences);
	configuration.savePreferences(preferences);
	assertTrue(preferences.stored);
    }

    public static class FakePortletPreferences implements PortletPreferences {

	private final Map<String, String> stringValues = Maps.newHashMap();

	private final Map<String, String[]> stringArrayValues = Maps.newHashMap();

	private boolean stored = false;

	@Override
	public boolean isReadOnly(final String key) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public String getValue(final String key, final String def) {
	    if (stringValues.containsKey(key)) {
		return stringValues.get(key);
	    } else {
		return def;
	    }
	}

	@Override
	public String[] getValues(final String key, final String[] def) {
	    if (stringArrayValues.containsKey(key)) {
		return stringArrayValues.get(key);
	    } else {
		return def;
	    }
	}

	@Override
	public void setValue(final String key, final String value) throws ReadOnlyException {
	    stringValues.put(key, value);
	}

	@Override
	public void setValues(final String key, final String[] values) throws ReadOnlyException {
	    stringArrayValues.put(key, values);
	}

	@Override
	public Enumeration<String> getNames() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String[]> getMap() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void reset(final String key) throws ReadOnlyException {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void store() throws IOException, ValidatorException {
	    stored = true;
	}
    }

}
