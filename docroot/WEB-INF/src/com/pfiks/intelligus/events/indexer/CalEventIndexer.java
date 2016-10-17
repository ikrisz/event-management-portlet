package com.pfiks.intelligus.events.indexer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletURL;

import com.google.common.collect.Lists;
import com.liferay.portal.kernel.cal.DayAndPosition;
import com.liferay.portal.kernel.cal.Recurrence;
import com.liferay.portal.kernel.cal.TZSRecurrence;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Projection;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionList;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.BaseIndexer;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchEngineUtil;
import com.liferay.portal.kernel.search.Summary;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.GroupConstants;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.portlet.calendar.service.CalEventLocalServiceUtil;
import com.pfiks.intelligus.portal.SearchConstants;
import com.pfiks.intelligus.search.service.IndexerLocalServiceUtil;
import com.pfiks.intelligus.util.ContentSecurityLevel;
import com.pfiks.intelligus.util.CustomExpandoFieldsNames;

public class CalEventIndexer extends BaseIndexer {

	private static Log LOG = LogFactoryUtil.getLog(CalEventIndexer.class);

	public static final String[] CLASS_NAMES = { CalEvent.class.getName() };

	public static final String PORTLET_ID = PortletKeys.CALENDAR;

	@Override
	public String[] getClassNames() {
		return CLASS_NAMES;
	}

	@Override
	public String getPortletId() {
		return PORTLET_ID;
	}

	@Override
	public boolean isPermissionAware() {
		return _PERMISSION_AWARE;
	}

	protected void addReindexCriteria(final DynamicQuery dynamicQuery, final long companyId) {

		final Property property = PropertyFactoryUtil.forName("companyId");

		dynamicQuery.add(property.eq(companyId));
	}

	@Override
	protected void doDelete(final Object obj) throws Exception {
		final CalEvent event = (CalEvent) obj;

		deleteDocument(event.getCompanyId(), event.getEventId());
	}

	@Override
	protected Document doGetDocument(final Object obj) throws Exception {
		final CalEvent event = (CalEvent) obj;

		final Document document = getBaseModelDocument(PORTLET_ID, event);

		document.addText(Field.DESCRIPTION, HtmlUtil.extractText(event.getDescription()));
		document.addText(Field.TITLE, event.getTitle());
		document.addKeyword(Field.TYPE, event.getType());

		try {
			LOG.debug("Indexing CalEvent with eventId: " + event.getEventId());
			document.addText(Field.CONTENT, event.getDescription());
			document.addKeyword(SearchConstants.TAG_ENTRIES, document.getValues(Field.ASSET_TAG_NAMES));
			setDocumentPrivacy(document, event);
			addEventExpandoFields(document, event);

			document.addKeyword("allDay", event.isAllDay() ? "true" : "false");
			document.addKeyword("modifiedDate", IndexerLocalServiceUtil.formatDate(event.getStartDate()));

			if (event.isRepeating()) {
				// Create duplicate documents, one for each event recurrence.
				// Leave the original document as it is
				final Collection<Document> recurrentEventDocuments = getRecurrentEventDocuments(document, event);
				if (!recurrentEventDocuments.isEmpty()) {
					for (final Document document2 : recurrentEventDocuments) {
						try {
							IndexerLocalServiceUtil.postProcessDocument(document2, obj);
						} catch (final Exception e) {
							LOG.warn("Exception when invoking base post processor " + e.getMessage(), e);
						}
						SearchEngineUtil.addDocument(getSearchEngineId(), event.getCompanyId(), document2);
					}
				}
			}
			// Add dates to the current document
			document.addDate(SearchConstants.START_DATE, event.getStartDate());
			// End date is calculated based on the startDate
			final Calendar cal = Calendar.getInstance();
			cal.setTime(event.getStartDate());
			if (event.isAllDay()) {
				cal.set(Calendar.HOUR_OF_DAY, 23);
				cal.set(Calendar.MINUTE, 59);
			} else {
				cal.add(Calendar.HOUR_OF_DAY, event.getDurationHour());
				cal.add(Calendar.MINUTE, event.getDurationMinute());
			}
			document.addDate(SearchConstants.END_DATE, cal.getTime());

		} catch (final Exception e) {
			LOG.error("Exception processing event for indexer", e);
		} finally {
			// Perform common tasks
			try {
				IndexerLocalServiceUtil.postProcessDocument(document, obj);
			} catch (final Exception e) {
				LOG.warn("Exception when invoking base post processor " + e.getMessage());
			}
		}
		return document;
	}

	@Override
	protected Summary doGetSummary(final Document document, final Locale locale, final String snippet, final PortletURL portletURL) {

		final String title = document.get(Field.TITLE);

		String content = snippet;

		if (Validator.isNull(snippet)) {
			content = StringUtil.shorten(document.get(Field.DESCRIPTION), 200);
		}

		final String eventId = document.get(Field.ENTRY_CLASS_PK);

		portletURL.setParameter("struts_action", "/calendar/view_event");
		portletURL.setParameter("eventId", eventId);

		return new Summary(title, content, portletURL);
	}

	@Override
	protected void doReindex(final Object obj) throws Exception {
		final CalEvent event = (CalEvent) obj;

		final Document document = getDocument(event);

		SearchEngineUtil.updateDocument(getSearchEngineId(), event.getCompanyId(), document);
	}

	@Override
	public String getSearchEngineId() {
		final String intelligusEngineId = "INTELLIGUS_ENGINE";
		if (SearchEngineUtil.getSearchEngine(intelligusEngineId) != null) {
			return intelligusEngineId;
		} else {
			return SearchEngineUtil.getDefaultSearchEngineId();
		}
	}

	@Override
	protected void doReindex(final String className, final long classPK) throws Exception {
		final CalEvent event = CalEventLocalServiceUtil.getEvent(classPK);

		doReindex(event);
	}

	@Override
	protected void doReindex(final String[] ids) throws Exception {
		final long companyId = GetterUtil.getLong(ids[0]);

		reindexEvents(companyId);
	}

	@Override
	protected String getPortletId(final SearchContext searchContext) {
		return PORTLET_ID;
	}

	protected void reindexEvents(final long companyId) throws Exception {
		final DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(CalEvent.class, PortalClassLoaderUtil.getClassLoader());

		final Projection minEventIdProjection = ProjectionFactoryUtil.min("eventId");
		final Projection maxEventIdProjection = ProjectionFactoryUtil.max("eventId");

		final ProjectionList projectionList = ProjectionFactoryUtil.projectionList();

		projectionList.add(minEventIdProjection);
		projectionList.add(maxEventIdProjection);

		dynamicQuery.setProjection(projectionList);

		addReindexCriteria(dynamicQuery, companyId);

		@SuppressWarnings("unchecked")
		final List<Object[]> results = CalEventLocalServiceUtil.dynamicQuery(dynamicQuery);

		final Object[] minAndMaxEventIds = results.get(0);

		if (minAndMaxEventIds[0] == null || minAndMaxEventIds[1] == null) {
			return;
		}

		final long minEventId = (Long) minAndMaxEventIds[0];
		final long maxEventId = (Long) minAndMaxEventIds[1];

		long startEventId = minEventId;
		long endEventId = startEventId + DEFAULT_INTERVAL;

		while (startEventId <= maxEventId) {
			reindexEvents(companyId, startEventId, endEventId);

			startEventId = endEventId;
			endEventId += DEFAULT_INTERVAL;
		}
	}

	protected void reindexEvents(final long companyId, final long startEventId, final long endEventId) throws Exception {

		final DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(CalEvent.class, PortalClassLoaderUtil.getClassLoader());

		final Property property = PropertyFactoryUtil.forName("eventId");

		dynamicQuery.add(property.ge(startEventId));
		dynamicQuery.add(property.lt(endEventId));

		addReindexCriteria(dynamicQuery, companyId);

		@SuppressWarnings("unchecked")
		final List<CalEvent> events = CalEventLocalServiceUtil.dynamicQuery(dynamicQuery);

		if (events.isEmpty()) {
			return;
		}

		final Collection<Document> documents = new ArrayList<Document>(events.size());

		for (final CalEvent event : events) {
			final Document document = getDocument(event);

			documents.add(document);
		}

		SearchEngineUtil.updateDocuments(getSearchEngineId(), companyId, documents);
	}

	private static final boolean _PERMISSION_AWARE = true;

	// POST PROCESSOR METHODS....
	private Collection<Document> getRecurrentEventDocuments(final Document document, final CalEvent event) {
		final Collection<Document> results = Lists.newArrayList();
		try {
			final TZSRecurrence recurrence = event.getRecurrenceObj();
			final int recurrenceType = recurrence.getFrequency();

			final Calendar eventStartDate = Calendar.getInstance();
			eventStartDate.setTime(event.getStartDate());

			final Calendar recurrenceEndDate = recurrence.getUntil();
			if (recurrenceEndDate != null) {// Only if recurrence End date is
				// set, otherwise infinite loop
				recurrenceEndDate.set(Calendar.HOUR_OF_DAY, eventStartDate.get(Calendar.HOUR_OF_DAY));
				recurrenceEndDate.set(Calendar.MINUTE, eventStartDate.get(Calendar.MINUTE));

				final Collection<Date> startDates = Lists.newArrayList();
				if (recurrenceType == Recurrence.DAILY) {
					startDates.addAll(dailyRecurrence(recurrence, eventStartDate, recurrenceEndDate));
				} else if (recurrenceType == Recurrence.WEEKLY) {
					startDates.addAll(weeklyRecurrence(recurrence, eventStartDate, recurrenceEndDate));
				} else if (recurrenceType == Recurrence.MONTHLY) {
					startDates.addAll(montlyRecurrence(recurrence, eventStartDate, recurrenceEndDate));
				}

				int count = 1;
				final String originalUid = document.getUID();

				for (final Date date : startDates) {

					final String updatedUidValue = originalUid + "_" + count;
					final Document updatedDoc = makeCopyOfOriginalDoc(document, updatedUidValue, event, date);
					results.add(updatedDoc);
					count++;
				}
			}
		} catch (final Exception e) {
			LOG.warn("Exception retrieving recurrent dates for event", e);
		}
		return results;
	}

	private Collection<Date> montlyRecurrence(final TZSRecurrence recurrence, final Calendar dateToUpdate, final Calendar recurrenceEndDate) {
		final Collection<Date> results = Lists.newArrayList();
		// Every x months
		final int monthsInterval = recurrence.getInterval();
		// This is the day number of when is recurring, e.g. on the 15th of
		// every month
		final int[] byMonthDay = recurrence.getByMonthDay();
		final int dayNumberOfMonth = byMonthDay[0];

		while (dateToUpdate.before(recurrenceEndDate)) {
			dateToUpdate.add(Calendar.MONTH, monthsInterval);
			final int maxDayOfMonth = dateToUpdate.getActualMaximum(Calendar.DAY_OF_MONTH);
			if (maxDayOfMonth >= dayNumberOfMonth) {
				dateToUpdate.set(Calendar.DAY_OF_MONTH, dayNumberOfMonth);
				if (dateToUpdate.before(recurrenceEndDate)) {
					results.add(dateToUpdate.getTime());
				}
			}
		}
		return results;
	}

	private Collection<Date> weeklyRecurrence(final TZSRecurrence recurrence, final Calendar dateToUpdate, final Calendar recurrenceEndDate) {
		final Collection<Date> results = Lists.newArrayList();
		// Every x weeks
		final int weeksInterval = recurrence.getInterval();
		// This is when is recurring. e.g. M, T, W, T, F, S, S
		final DayAndPosition[] byDay = recurrence.getByDay();

		while (dateToUpdate.before(recurrenceEndDate)) {
			dateToUpdate.add(Calendar.WEEK_OF_YEAR, weeksInterval);
			for (final DayAndPosition dayAndPosOfWeek : byDay) {
				dateToUpdate.set(Calendar.DAY_OF_WEEK, dayAndPosOfWeek.getDayOfWeek());
				if (dateToUpdate.before(recurrenceEndDate)) {
					results.add(dateToUpdate.getTime());
				}
			}
		}
		return results;
	}

	private Collection<Date> dailyRecurrence(final TZSRecurrence recurrence, final Calendar dateToUpdate, final Calendar recurrenceEndDate) {
		final Collection<Date> results = Lists.newArrayList();

		final int daysInterval = recurrence.getInterval();

		while (dateToUpdate.before(recurrenceEndDate)) {
			dateToUpdate.add(Calendar.DAY_OF_YEAR, daysInterval);
			if (dateToUpdate.before(recurrenceEndDate)) {
				results.add(dateToUpdate.getTime());
			}
		}
		return results;
	}

	private void addEventExpandoFields(final Document document, final CalEvent event) {
		final Map<String, Serializable> attributes = event.getExpandoBridge().getAttributes();
		for (final Entry<String, Serializable> attribute : attributes.entrySet()) {
			final String attributeName = attribute.getKey().replace(StringPool.SPACE, StringPool.DASH);
			if (documentDoesNotAlreadyHaveTheField(document, attributeName)) {
				final Serializable value = attribute.getValue();
				try {
					final String valueToAdd = value.toString().toLowerCase();
					if (valueToAdd != null && !valueToAdd.isEmpty()) {
						document.addText(attributeName, valueToAdd);
					}
				} catch (final Exception e) {
					LOG.debug("Exception adding expando attribute " + attributeName + " to indexer: " + e.getMessage());
				}
			}
		}
	}

	private boolean documentDoesNotAlreadyHaveTheField(final Document document, final String attributeName) {
		final String field = document.get(attributeName);
		return field == null || field.isEmpty();
	}

	private void setDocumentPrivacy(final Document document, final CalEvent event) throws SystemException {
		final String securityLevel = getSecurityLevelExpandoField(event);
		Boolean publicFlag = false;
		final Group group = GroupLocalServiceUtil.fetchGroup(event.getGroupId());
		if (Validator.isNotNull(group) && group.getType() == GroupConstants.TYPE_SITE_PRIVATE) {
			document.addKeyword(SearchConstants.PRIVACY, "secret");

		} else {
			if (securityLevel.equalsIgnoreCase(ContentSecurityLevel.PUBLIC.getKey())) {
				document.addKeyword(SearchConstants.PRIVACY, "public");
				publicFlag = true;

			} else if (securityLevel.equalsIgnoreCase(ContentSecurityLevel.NETWORK.getKey())) {
				document.addKeyword(SearchConstants.PRIVACY, "network");
				final Long networkId = getNetworkIdExpandoField(event);
				if (networkId > 0) {
					document.addText(SearchConstants.NETWORK_ID, String.valueOf(networkId));
				}

			} else if (securityLevel.equalsIgnoreCase(ContentSecurityLevel.NETWORK_INTRANET.getKey())) {
				document.addKeyword(SearchConstants.PRIVACY, "network_intranet");
				final Long networkId = getNetworkIdExpandoField(event);
				if (networkId > 0) {
					document.addText(SearchConstants.NETWORK_ID, String.valueOf(networkId));
				}

			} else {
				//Default privacy settings is group
				document.addKeyword(SearchConstants.PRIVACY, "workspace_members");
			}
		}

		document.addKeyword("public", publicFlag);
	}

	private String getSecurityLevelExpandoField(final CalEvent calEvent) {
		try {
			return GetterUtil.getString(calEvent.getExpandoBridge().getAttribute(CustomExpandoFieldsNames.SECURITY_LEVEL), ContentSecurityLevel.GROUP.getKey());
		} catch (final Exception e) {
			return ContentSecurityLevel.GROUP.getKey();
		}
	}

	private Long getNetworkIdExpandoField(final CalEvent event) {
		try {
			return GetterUtil.getLong(event.getExpandoBridge().getAttribute(SearchConstants.NETWORK_ID), 0);
		} catch (final Exception e) {
			return 0L;
		}
	}

	private Document makeCopyOfOriginalDoc(final Document documentToCopy, final String uid, final CalEvent event, final Date startDate) {
		final Document document = new DocumentImpl();
		for (final Field field : documentToCopy.getFields().values()) {
			document.add(field);
		}
		document.remove(Field.UID);
		document.add(new Field(Field.UID, uid));

		// Add dates to the current document
		document.addDate(SearchConstants.START_DATE, startDate);
		// End date is calculated based on the startDate
		final Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		if (event.isAllDay()) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
		} else {
			cal.add(Calendar.HOUR_OF_DAY, event.getDurationHour());
			cal.add(Calendar.MINUTE, event.getDurationMinute());
		}
		final Date endDate = cal.getTime();
		document.addDate(SearchConstants.END_DATE, endDate);

		return document;
	}

}
