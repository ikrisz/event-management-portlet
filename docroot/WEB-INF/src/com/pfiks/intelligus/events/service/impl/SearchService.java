package com.pfiks.intelligus.events.service.impl;

import java.util.Arrays;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.constants.EventExpandoConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.model.EventPortletConfiguration;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.retrieval.IRetrievalResponse;
import com.pfiks.intelligus.search.service.SearchLocalServiceUtil;
import com.pfiks.intelligus.util.SolrFilterState;
import com.pfiks.intelligus.util.SolrFilterState.SortMethod;

@Component
public class SearchService {

	private static final Log LOG = LogFactoryUtil.getLog(SearchService.class);
	private final Set<String> searchDisplayFields;
	private final Set<String> searchAcrossFields;

	public SearchService() {
		// Fields that are returned by search query fl
		searchDisplayFields = Sets.newHashSet(Field.ENTRY_CLASS_PK, Field.DESCRIPTION, Field.UID, Field.COMPANY_ID, Field.GROUP_ID, Field.USER_ID, Field.TITLE,
				SearchConstants.START_DATE, SearchConstants.END_DATE, "allDay", EventExpandoConstants.VENUE, EventExpandoConstants.CITY,
				EventExpandoConstants.REGION_STATE, EventExpandoConstants.COUNTRY, EventExpandoConstants.ONLINE_EVENT);

		// Fields used to search the keyword pf, qf
		searchAcrossFields = Sets.newHashSet(Field.TITLE, Field.CONTENT, Field.DESCRIPTION, SearchConstants.TAG_ENTRIES, EventExpandoConstants.VENUE,
				EventExpandoConstants.ADDRESS_1, EventExpandoConstants.ADDRESS_2, EventExpandoConstants.ZIP_CODE, EventExpandoConstants.CITY,
				EventExpandoConstants.REGION_STATE);
	}

	public IRetrievalResponse retrieveEvent(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final Long eventId) throws EventException {
		try {
			final SolrFilterState filterState = getSearchFilterForEventDetailsView(themeDisplay, configuration, eventId);
			return executeSearch(filterState, themeDisplay);
		} catch (final Exception e) {
			LOG.error("Exception searching event details: " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	public IRetrievalResponse retrieveEventsForListView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupIds, final String searchText, final Boolean performSearch, final Boolean viewPastEvents, final Boolean viewFutureEvents,
			final int start) throws EventException {
		try {
			final SolrFilterState filterState = getSearchFilterForListView(themeDisplay, configuration, networkId, networkGroupIds, searchText, performSearch,
					viewPastEvents, viewFutureEvents, start);
			return executeSearch(filterState, themeDisplay);
		} catch (final Exception e) {
			LOG.error("Exception searching events for list view: " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	public IRetrievalResponse retrieveFeaturedEvents(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupids) throws EventException {
		try {
			final SolrFilterState filterState = getSearchFilterForFeaturedEvents(themeDisplay, configuration, networkId, networkGroupids);
			return executeSearch(filterState, themeDisplay);
		} catch (final Exception e) {
			LOG.error("Exception searching featured events: " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	public IRetrievalResponse retrieveEventsForCalendarView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupIds, final String startDateFilter, final String endDateFilter) throws EventException {
		try {
			final SolrFilterState filterState = getSearchFilterForCalendarView(themeDisplay, configuration, networkId, networkGroupIds, startDateFilter, endDateFilter);
			return executeSearch(filterState, themeDisplay);
		} catch (final Exception e) {
			LOG.error("Exception searching events for calendar view: " + Throwables.getRootCause(e));
			throw new EventException(e);
		}
	}

	@VisibleForTesting
	SolrFilterState getSearchFilterForListView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupIds, final String searchText, final Boolean performSearch, final Boolean viewPastEvents, final Boolean viewFutureEvents,
			final int start) throws SystemException {
		final SolrFilterState filterState = getGenericFilterState(configuration, themeDisplay, networkId, networkGroupIds);
		filterState.setCurrentPage(start);
		filterState.setDelta(configuration.getMaxEventsToShow());

		final String searchKeyword = StringUtils.isBlank(searchText) ? StringPool.BLANK : searchText.toLowerCase();
		filterState.setSearchKeyword(searchKeyword);
		if (StringUtils.isNotBlank(searchKeyword) && performSearch) {
			filterState.addSortBy("score", SortMethod.DESCENDING);
			filterState.setMinimumShouldMatch("1");
		}
		if (!performSearch) {
			addDatesRestrictions(viewPastEvents, viewFutureEvents, themeDisplay.getTimeZone(), filterState);
		}
		return filterState;
	}

	@VisibleForTesting
	SolrFilterState getSearchFilterForCalendarView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupIds, final String startDateFilter, final String endDateFilter) throws SystemException {
		final SolrFilterState filterState = getGenericFilterState(configuration, themeDisplay, networkId, networkGroupIds);
		filterState.setCurrentPage(1);
		filterState.setDelta(Integer.MAX_VALUE);
		filterState.setSearchKeyword(StringPool.BLANK);

		final String startFilter = startDateFilter.concat("T00:00:00Z");
		final String endFilter = endDateFilter.concat("T23:59:59Z");
		filterState.addCustomParameter(SearchConstants.START_DATE, "[* TO " + endFilter + "]");
		filterState.addCustomParameter(SearchConstants.END_DATE, "[" + startFilter + " TO *]");
		return filterState;
	}

	@VisibleForTesting
	SolrFilterState getSearchFilterForFeaturedEvents(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final long networkId,
			final Long[] networkGroupIds) throws SystemException {
		final SolrFilterState filterState = getGenericFilterState(configuration, themeDisplay, networkId, networkGroupIds);
		filterState.setCurrentPage(1);
		filterState.setDelta(configuration.getMaxFeaturedEventsToShow());
		filterState.setSearchKeyword(StringPool.BLANK);
		addDatesRestrictions(false, true, themeDisplay.getTimeZone(), filterState);
		filterState.addCustomParameter(EventExpandoConstants.FEATURED, "true");
		return filterState;
	}

	@VisibleForTesting
	SolrFilterState getSearchFilterForEventDetailsView(final ThemeDisplay themeDisplay, final EventPortletConfiguration configuration, final Long eventId)
			throws SystemException {
		final SolrFilterState filterState = getGenericFilterState(configuration, themeDisplay, 0, null);
		filterState.setCurrentPage(1);
		filterState.setDelta(Integer.MAX_VALUE);
		filterState.setSearchKeyword(StringPool.BLANK);
		filterState.addCustomParameter(Field.ENTRY_CLASS_PK, String.valueOf(eventId));
		return filterState;
	}

	private IRetrievalResponse executeSearch(final SolrFilterState filterState, final ThemeDisplay themeDisplay) throws Exception {
		final IRetrievalResponse retrievalResponse = SearchLocalServiceUtil.doSearchWithFilter(filterState, themeDisplay);
		LOG.debug("Search completed. Results found are :" + retrievalResponse.numOverallHits());
		return retrievalResponse;
	}

	private void addDatesRestrictions(final Boolean viewPastEvents, final Boolean viewFutureEvents, final TimeZone tz, final SolrFilterState filterState) {
		if (viewFutureEvents && !viewPastEvents) {
			// Only future
			filterState.addSortBy(SearchConstants.START_DATE, SortMethod.ASCENDING);
			filterState.addCustomParameter(SearchConstants.END_DATE, "[NOW TO *]");
		} else if (!viewFutureEvents && viewPastEvents) {
			// Only past
			filterState.addSortBy(SearchConstants.END_DATE, SortMethod.DESCENDING);
			filterState.addCustomParameter(SearchConstants.END_DATE, "[* TO NOW]");
		} // else all events
	}

	/*
	 * Sets generic filters for search: contentType=CalEvent restrictions based
	 * on groupId, if the configured scope is Group restrictions based on user
	 * and event public flag.
	 */
	private SolrFilterState getGenericFilterState(final EventPortletConfiguration config, final ThemeDisplay themeDisplay, final long networkId, final Long[] networkGroupids)
			throws SystemException {
		final SolrFilterState filterState = new SolrFilterState();
		filterState.setCompanyId(themeDisplay.getCompanyId());
		filterState.setQueryOperator(com.pfiks.intelligus.util.SolrFilterState.QueryOperator.OR);
		filterState.setCustomFilterOperator(com.pfiks.intelligus.util.SolrFilterState.QueryOperator.AND);
		filterState.setFilterTypes(Lists.newArrayList(SearchConstants.EVENT_TYPE));
		filterState.setConfineSearchFields(searchAcrossFields);
		filterState.setDisplayFields(searchDisplayFields);
		filterState.addBoostField(Field.TITLE, 1.0F);
		filterState.addBoostField(Field.CONTENT, 1.0F);
		final long currentGroupId = themeDisplay.getScopeGroupId();
		addFilterBasedOnGroupScope(filterState, config, currentGroupId, networkGroupids);
		addSecurityViewRestriction(themeDisplay, currentGroupId, networkId, filterState, config);
		return filterState;
	}

	/*
	 * If configuration is PUBLIC :
	 * By default only show public events (guest or logged in user, no difference)
	 * If the user is signed in and has a network active, only show results for that network.
	 * 
	 * If configuration is GROUP:
	 * show all event for the group, do not filter is a network is active.
	 */
	private void addSecurityViewRestriction(final ThemeDisplay themeDisplay, final long currentGroupId, final long networkId, final SolrFilterState filterState,
			final EventPortletConfiguration config) throws SystemException {
		
		final boolean isGuestUser = !themeDisplay.isSignedIn();
			final boolean userMemberOfGroup = UserLocalServiceUtil.hasGroupUser(currentGroupId, themeDisplay.getUserId());
		final boolean publicScope = config.getConfiguredScope().equals(ConfigurationConstants.SCOPE_PUBLIC);

		final boolean addSelectedNetworkRestrictions = networkId > 0;
		if (themeDisplay.isSignedIn() && publicScope && addSelectedNetworkRestrictions) {
			filterState.addCustomParameter(SearchConstants.NETWORK_ID, String.valueOf(networkId));
		}else if(isGuestUser ||!userMemberOfGroup || publicScope) {
			filterState.addCustomParameter(SearchConstants.PUBLIC, "true");
		}
	}

	private void addFilterBasedOnGroupScope(final SolrFilterState filterState, final EventPortletConfiguration config, final long currentGroupId, final Long[] networkGroupids) {
		final boolean onlyCurrentGroup = config.getConfiguredScope().equals(ConfigurationConstants.SCOPE_GROUP);
		if (onlyCurrentGroup) {
			filterState.setGroupIds(Lists.newArrayList(currentGroupId));
		} else {
			if (ArrayUtils.isNotEmpty(networkGroupids)) {
				filterState.setGroupIds(Arrays.asList(networkGroupids));
			}
		}
	}

}
