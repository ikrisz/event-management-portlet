package com.pfiks.intelligus.events.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import org.junit.Before;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.pfiks.intelligus.events.ClassesConstructorsTest;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.util.SolrFilterState;
import com.pfiks.intelligus.util.SolrFilterState.SortMethod;

public class SearchServiceTest extends ClassesConstructorsTest {

    protected final SearchService searchService = new SearchService();
    protected final PortletPreferences portletPreferences = mock(PortletPreferences.class);
    protected SolrFilterState filterState;
    protected EventPortletConfiguration configuration;

    @Before
    public void setUp() throws ReadOnlyException, SystemException {
	when(portletPreferences.getValue(ConfigurationConstants.CONFIG_SCOPE, ConfigurationConstants.DEFAULT_CONFIG_SCOPE)).thenReturn(
		ConfigurationConstants.DEFAULT_CONFIG_SCOPE);
	when(portletPreferences.getValue(ConfigurationConstants.MAX_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER)).thenReturn(
		ConfigurationConstants.DEFAULT_CONFIG_NUMBER);
	when(portletPreferences.getValue(ConfigurationConstants.MAX_FEATURED_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER)).thenReturn(
		ConfigurationConstants.DEFAULT_CONFIG_NUMBER);
	configuration = new EventPortletConfiguration(portletPreferences);

	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(true);
    }

    protected void assertThatConfiguredSearchFieldsAreSet() {
	final String expandoFieldPrefix = "expando/custom_fields/";

	assertThat(
		filterState.getConfineSearchFields(),
		containsInAnyOrder(Field.TITLE, Field.CONTENT, SearchConstants.TAG_ENTRIES, expandoFieldPrefix + EventExpandoConstants.VENUE,
			expandoFieldPrefix + EventExpandoConstants.ADDRESS_1, expandoFieldPrefix + EventExpandoConstants.ADDRESS_2,
			expandoFieldPrefix + EventExpandoConstants.ZIP_CODE, expandoFieldPrefix + EventExpandoConstants.CITY, expandoFieldPrefix
				+ EventExpandoConstants.REGION_STATE));
	assertThat(
		filterState.getDisplayFields(),
		containsInAnyOrder(Field.ENTRY_CLASS_PK, Field.UID, Field.COMPANY_ID, Field.GROUP_ID, Field.USER_ID, Field.TITLE,
			SearchConstants.START_DATE, SearchConstants.END_DATE, "allDay", EventExpandoConstants.VENUE, EventExpandoConstants.CITY,
			EventExpandoConstants.REGION_STATE, EventExpandoConstants.COUNTRY, EventExpandoConstants.ONLINE_EVENT));
    }

    protected void assertThatPublicFilterIsEnabled() {
	assertThat(filterState.getCustomParameters(), hasKey(SearchConstants.PUBLIC));
	assertThat(filterState.getCustomParameters().get(SearchConstants.PUBLIC), is("true"));
    }

    protected void assertThatPublicFilterIsDisabled() {
	assertThat(filterState.getCustomParameters(), not(hasKey(SearchConstants.PUBLIC)));
    }

    protected void assertThatSortByIs(final String sortByKey, final SortMethod sortMethod) {
	assertThat(filterState.getSortBy(), hasKey(sortByKey));
	assertThat(filterState.getSortBy().get(sortByKey), is(sortMethod));
    }

    protected void assertThatParameterHasValue(final String parameterKey, final String parameterValue) {
	assertThat(filterState.getCustomParameters(), hasKey(parameterKey));
	assertThat(filterState.getCustomParameters().get(parameterKey), is(parameterValue));
    }

    protected void assertThatParameterExists(final String parameterKey) {
	assertThat(filterState.getCustomParameters(), hasKey(parameterKey));
    }

    protected void assertThatParameterDoesNotExist(final String parameterKey) {
	assertThat(filterState.getCustomParameters(), not(hasKey(parameterKey)));
    }

    protected void assertThatSortByIsNot(final String sortByKey) {
	assertThat(filterState.getSortBy(), not(hasKey(sortByKey)));
    }

    protected void assertThatDeltaIs(final Integer deltaValue) {
	assertThat(filterState.getDelta(), is(deltaValue));
    }

    protected void assertThatFirstResultIs(final Integer indexValue) {
	assertThat(filterState.getCurrentPage(), is(indexValue));
    }

    protected void aDefaultConfigurationWithScope(final String scopeConfig) {
	when(portletPreferences.getValue(ConfigurationConstants.CONFIG_SCOPE, ConfigurationConstants.DEFAULT_CONFIG_SCOPE)).thenReturn(scopeConfig);
	configuration = new EventPortletConfiguration(portletPreferences);
    }

}
