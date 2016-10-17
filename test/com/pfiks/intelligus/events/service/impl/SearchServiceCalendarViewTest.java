package com.pfiks.intelligus.events.service.impl;

import static org.mockito.Mockito.when;

import org.junit.Test;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.portal.SearchConstants;

public class SearchServiceCalendarViewTest extends SearchServiceTest {

    private final String startDateFilter = "2014-06-01";
    private final String endDateFilter = "2014-06-30";

    @Test
    public void testThatCalendarFilter_has_configuredSearchFields() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.DEFAULT_CONFIG_SCOPE);
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatConfiguredSearchFieldsAreSet();
    }

    @Test
    public void testThatCalendarFilter_has_groupId_when_configuredScopeIsGroup() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_GROUP);
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatParameterExists(Field.GROUP_ID);
    }

    @Test
    public void testThatCalendarFilter_doesNotHave_groupId_when_configuredScopeIsAll() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_ALL);
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatParameterDoesNotExist(Field.GROUP_ID);
    }

    @Test
    public void testThatCalendarFilter_has_publicFlag_when_userIsGuest() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(false);
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatCalendarFilter_has_publicFlag_when_userIsSignedIn_and_userIsNotMemberOfGroup() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(false);
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatCalendarFilter_doesNotHave_publicFlag_when_userIsSignedIn_and_userIsMemberOfGroup() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(true);
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatPublicFilterIsDisabled();
    }

    @Test
    public void testThatCalendarFilter_returnsAllExistingResults() throws SystemException {
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	assertThatFirstResultIs(1);
	assertThatDeltaIs(Integer.MAX_VALUE);
    }

    @Test
    public void testThatCalendarFilter_hasDateFiltersSet() throws SystemException {
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);

	final String startDateFilterVal = startDateFilter + "T00:00:00Z";
	final String endDateFilterVal = endDateFilter + "T23:59:59Z";
	final String startDateVal = "[" + startDateFilterVal + " TO " + endDateFilterVal + "]";
	assertThatParameterHasValue(SearchConstants.START_DATE, startDateVal);

	final String endDateVal = "[" + startDateFilterVal + " TO " + endDateFilterVal + "]";
	assertThatParameterHasValue(SearchConstants.END_DATE, endDateVal);
    }

    @Test
    public void testThatCalendarFilter_hasNoSortingEnabled() throws SystemException {
	filterState = searchService.getSearchFilterForCalendarView(themeDisplay, configuration, startDateFilter, endDateFilter);
	assertThatSortByIsNot("score");
	assertThatSortByIsNot(SearchConstants.START_DATE);
	assertThatSortByIsNot(SearchConstants.END_DATE);
    }

}
