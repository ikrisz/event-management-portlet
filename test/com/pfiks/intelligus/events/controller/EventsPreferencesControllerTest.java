package com.pfiks.intelligus.events.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import com.pfiks.intelligus.events.ClassesConstructorsTest;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;

public class EventsPreferencesControllerTest extends ClassesConstructorsTest {

    private final Model model = new ExtendedModelMap();
    private final RenderRequest mockRequest = mock(RenderRequest.class);
    private final PortletPreferences mockPortletPreferences = mock(PortletPreferences.class);
    private EventsPreferencesController preferencesController;

    @Before
    public void setUp() {
	preferencesController = new EventsPreferencesController();
	preferencesController.setConfigUtils(configurationUtils);

	when(mockPortletPreferences.getValue(ConfigurationConstants.MAX_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER)).thenReturn(
		ConfigurationConstants.DEFAULT_CONFIG_NUMBER);
	when(mockPortletPreferences.getValue(ConfigurationConstants.MAX_FEATURED_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER)).thenReturn(
		ConfigurationConstants.DEFAULT_CONFIG_NUMBER);

    }

    @Test
    public void testViewHasRightObjects() throws Exception {
	preferencesController.view(mockRequest, mockPortletPreferences, model);
	assertThat((Collection<?>) model.asMap().get("availableScopes"), hasSize(2));
    }

}
