package com.pfiks.intelligus.events.service.impl;

import static org.mockito.Mockito.when;

import org.junit.Test;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.portal.SearchConstants;

public class SearchServiceEventDetailsViewTest extends SearchServiceTest {

    private final Long eventId = 1234L;

    @Test
    public void testThatEventDetailsFilter_has_configuredSearchFields() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.DEFAULT_CONFIG_SCOPE);
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatConfiguredSearchFieldsAreSet();
    }

    @Test
    public void testThatEventDetailsFilter_has_groupId_when_configuredScopeIsGroup() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_GROUP);
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatParameterExists(Field.GROUP_ID);
    }

    @Test
    public void testThatEventDetailsFilter_doesNotHave_groupId_when_configuredScopeIsAll() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_ALL);
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatParameterDoesNotExist(Field.GROUP_ID);
    }

    @Test
    public void testThatEventDetailsFilter_has_publicFlag_when_userIsGuest() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(false);
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatEventDetailsFilter_has_publicFlag_when_userIsSignedIn_and_userIsNotMemberOfGroup() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(false);
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatEventDetailsFilter_doesNotHave_publicFlag_when_userIsSignedIn_and_userIsMemberOfGroup() throws EventException,
	    SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(true);
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatPublicFilterIsDisabled();
    }

    @Test
    public void testThatEventDetailsFilter_returnsAllExistingResults() throws SystemException {
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatFirstResultIs(1);
	assertThatDeltaIs(Integer.MAX_VALUE);
    }

    @Test
    public void testThatEventDetailsFilter_hasNoDateFiltersSet() throws SystemException {
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatParameterDoesNotExist(SearchConstants.START_DATE);
	assertThatParameterDoesNotExist(SearchConstants.END_DATE);
    }

    @Test
    public void testThatEventDetailsFilter_hasNoSortingEnabled() throws SystemException {
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);
	assertThatSortByIsNot("score");
	assertThatSortByIsNot(SearchConstants.START_DATE);
	assertThatSortByIsNot(SearchConstants.END_DATE);
    }

    @Test
    public void testThatEventDetailsFilter_hasEventIdFilterSet() throws SystemException {
	filterState = searchService.getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);

	assertThatParameterHasValue(Field.ENTRY_CLASS_PK, String.valueOf(eventId));
    }
}
