package com.pfiks.intelligus.events.listener;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.liferay.portal.ModelListenerException;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;
import com.pfiks.intelligus.retrieval.IRetrievalResponse;
import com.pfiks.intelligus.retrieval.QueryParameters;
import com.pfiks.intelligus.retrieval.RetrievalHit;
import com.pfiks.intelligus.search.service.SearchLocalServiceUtil;

public class CalEventListener extends BaseModelListener<CalEvent> {

	private static final Log LOG = LogFactoryUtil.getLog(CalEventListener.class);

	private Indexer getIndexer() {
		return IndexerRegistryUtil.getIndexer(CalEvent.class);
	}

	@Override
	public void onAfterRemove(final CalEvent event) {
		deleteAllEventDocumentsFromIndexer(event);
	}

	@Override
	public void onBeforeUpdate(final CalEvent model) throws ModelListenerException {
		if (eventRecurrenceChanged(model)) {
			deleteAllEventDocumentsFromIndexer(model);
			reindexCurrentEvent(model);
		}
		super.onBeforeUpdate(model);

	}

	private void reindexCurrentEvent(final CalEvent model) {
		try {
			getIndexer().reindex(model);
		} catch (final SearchException e) {
			LOG.error("Exception reindexing current event " + e.getMessage());
		}
	}

	private void deleteAllEventDocumentsFromIndexer(final CalEvent event) {
		try {
			final long eventId = event.getEventId();
			final long companyId = event.getCompanyId();
			final Indexer indexer = getIndexer();
			indexer.delete(event);
			final Collection<String> uids = getUidsForEvent(companyId, eventId);
			SearchEngineUtil.deleteDocuments(getSearchEngineId(), companyId, uids);
		} catch (final Exception e) {
			LOG.error("Exception removing all event documents from indexer " + e.getMessage());
		}
	}

	private String getSearchEngineId() {
		final String intelligusEngineId = "INTELLIGUS_ENGINE";
		if (SearchEngineUtil.getSearchEngine(intelligusEngineId) != null) {
			return intelligusEngineId;
		} else {
			return SearchEngineUtil.getDefaultSearchEngineId();
		}
	}

	private boolean eventRecurrenceChanged(final CalEvent event) {
		boolean result = true;
		try {
			final CalEvent previousEvent = CalEventLocalServiceUtil.getCalEvent(event.getEventId());
			if (previousEvent.getRecurrence().equals(event.getRecurrence())) {
				result = false;
			}
		} catch (final NestableException e) {
			LOG.error("Exception checking if event recurrence changed. " + e.getMessage());
		}
		LOG.debug("Event recurrence changed? " + result);
		return result;
	}

	private Collection<String> getUidsForEvent(final Long companyId, final Long eventId) {
		final Collection<String> uidsToRemove = Lists.newArrayList();
		final QueryParameters eventQueryParams = new QueryParameters();

		eventQueryParams.setProperty("q", "*:*", false, true);
		eventQueryParams.setProperty("rows", "" + Integer.MAX_VALUE, false, true);
		eventQueryParams.setProperty("start", "0", false, true);
		eventQueryParams.setProperty("fl", Field.UID, false, false);

		eventQueryParams.setProperty("fq", "contentPool:CalEvent", false, false);
		eventQueryParams.setProperty("fq", Field.COMPANY_ID + ":" + companyId, false, false);
		eventQueryParams.setProperty("fq", Field.ENTRY_CLASS_PK + ":" + eventId, false, false);

		final IRetrievalResponse retrievalResponse = SearchLocalServiceUtil.doAdminSearchInCompany(companyId, eventQueryParams);
		if (retrievalResponse != null) {
			final List<RetrievalHit> hits = retrievalResponse.getHits();
			for (final RetrievalHit retrievalHit : hits) {
				uidsToRemove.add(retrievalHit.getString(Field.UID));
			}
		}
		LOG.debug("Total indexed entries found for event are: " + uidsToRemove.size());
		return uidsToRemove;
	}

}
