package com.pfiks.intelligus.events.service.impl;

import static org.mockito.Mockito.when;

import org.junit.Test;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.util.SolrFilterState.SortMethod;

public class SearchServiceListViewTest extends SearchServiceTest {

    private String searchText = "";
    private boolean viewFutureEvents = true;
    private boolean viewPastEvents = true;
    private boolean performSearch = false;
    private int start = 1;

    @Test
    public void testThatListFilter_has_configuredSearchFields() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.DEFAULT_CONFIG_SCOPE);

	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatConfiguredSearchFieldsAreSet();
    }

    @Test
    public void testThatListFilter_hasPaginationSettings() throws SystemException {
	start = 50;
	final String maxResults = "80";

	when(portletPreferences.getValue(ConfigurationConstants.MAX_EVENTS, ConfigurationConstants.DEFAULT_CONFIG_NUMBER)).thenReturn(maxResults);
	configuration = new EventPortletConfiguration(portletPreferences);

	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatFirstResultIs(start);
	assertThatDeltaIs(Integer.valueOf(maxResults));
    }

    @Test
    public void testThatListFilter_has_groupId_when_configuredScopeIsGroup() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_GROUP);
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatParameterExists(Field.GROUP_ID);
    }

    @Test
    public void testThatListFilter_doesNotHave_groupId_when_configuredScopeIsAll() throws SystemException {
	aDefaultConfigurationWithScope(ConfigurationConstants.SCOPE_ALL);
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatParameterDoesNotExist(Field.GROUP_ID);
    }

    @Test
    public void testThatListFilter_has_publicFlag_when_userIsGuest() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(false);
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatListFilter_has_publicFlag_when_userIsSignedIn_and_userIsNotMemberOfGroup() throws EventException, SystemException {

	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(false);
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatPublicFilterIsEnabled();
    }

    @Test
    public void testThatListFilter_doesNotHave_publicFlag_when_userIsSignedIn_and_userIsMemberOfGroup() throws EventException, SystemException {
	when(themeDisplay.isSignedIn()).thenReturn(true);
	when(UserLocalServiceUtil.hasGroupUser(0, 0L)).thenReturn(true);
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatPublicFilterIsDisabled();
    }

    @Test
    public void testThatListFilter_doesNotHave_sortByScore_when_searchKeywordIsBlank() throws SystemException {
	searchText = StringPool.BLANK;
	performSearch = true;
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatSortByIsNot("score");
    }

    @Test
    public void testThatListFilter_has_sortByScore_when_searchKeywordIsSet() throws SystemException {
	searchText = "searchText";
	performSearch = true;
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatSortByIs("score", SortMethod.DESCENDING);
    }

    @Test
    public void testThatListFilter_onlyFutureEvents_orderedByStartDateAsc_when_onlyShowFutureEventsIsSelected() throws SystemException {

	viewFutureEvents = true;
	viewPastEvents = false;

	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatSortByIs(SearchConstants.START_DATE, SortMethod.ASCENDING);

	assertThatParameterHasValue(SearchConstants.END_DATE, "[NOW TO *]");
    }

    @Test
    public void testThatListFilter_onlyPastEvents_orderedByEndDateDesc_when_onlyShowPastEventsIsSelected() throws SystemException {
	viewFutureEvents = false;
	viewPastEvents = true;

	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatSortByIs(SearchConstants.END_DATE, SortMethod.DESCENDING);
	assertThatParameterHasValue(SearchConstants.END_DATE, "[* TO NOW]");
    }

    @Test
    public void testThatListFilter_noDateFiltersAreSet_when_searchIsPerformed() throws SystemException {
	searchText = "searchText";
	performSearch = true;
	filterState = searchService.getSearchFilterForListView(themeDisplay, configuration, searchText, performSearch, viewPastEvents,
		viewFutureEvents, start);

	assertThatSortByIsNot(SearchConstants.START_DATE);
	assertThatSortByIsNot(SearchConstants.END_DATE);
    }

}
