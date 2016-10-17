package com.pfiks.intelligus.events.service.impl;

import static org.mockito.Mockito.when;

import org.junit.Test;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.util.SolrFilterState.SortMethod;

public class SearchServiceFeaturedEventsTest extends SearchServiceTest {

    @Test
    public void testThatFeaturedFilter_has_configuredSearchFields() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.DEFAULT_CONFIG_SCOPE);

	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatConfiguredSearchFieldsAreSet();
    }

    @Test
    public void testThatFeaturedFilter_hasPaginationSettings() throws SystemException {
	final String maxResults = "2";

	when(portletPreferences.getValue(ConfigurationConstants.MAX_FEATURED_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER)).thenReturn(
		maxResults);
	configuration = new EventPortletConfiguration(portletPreferences);

	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatDeltaIs(Integer.valueOf(maxResults));
    }

    @Test
    public void testThatFeaturedFilter_has_groupId_when_configuredScopeIsGroup() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_GROUP);
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatParameterExists(Field.GROUP_ID);
    }

    @Test
    public void testThatFeaturedFilter_doesNotHave_groupId_when_configuredScopeIsAll() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_ALL);
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);
	assertThatParameterDoesNotExist(Field.GROUP_ID);
    }

    @Test
    public void testThatFeaturedFilter_has_publicFlag_when_userIsGuest() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(false);
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);
	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatFeaturedFilter_has_publicFlag_when_userIsSignedIn_and_userIsNotMemberOfGroup() throws EventException, SystemException {

	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(false);
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatFeaturedFilter_doesNotHave_publicFlag_when_userIsSignedIn_and_userIsMemberOfGroup() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(true);
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatPublicFilterIsDisabled();
    }

    @Test
    public void testThatFeaturedFilter_hasFeaturedFilterSet() throws SystemException {
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatParameterHasValue(EventExpandoConstants.FEATURED, "true");
    }

    @Test
    public void testThatFeaturedFilter_onlyReturnsFutureEvents_orderedByStartDateAsc() throws SystemException {
	filterState = searchService.getSearchFilterForFeaturedEvents(themeDisplay, configuration);

	assertThatSortByIs(SearchConstants.START_DATE, SortMethod.ASCENDING);

	assertThatParameterHasValue(SearchConstants.END_DATE, "[NOW TO *]");
    }

}
