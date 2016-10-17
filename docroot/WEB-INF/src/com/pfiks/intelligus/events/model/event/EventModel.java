package com.pfiks.intelligus.events.model.event;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.liferay.portal.kernel.util.UnicodeFormatter;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portlet.calendar.model.CalEvent;
import com.pfiks.intelligus.util.ContentSecurityLevel;

public class EventModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean hasEditPermission;

	@Expose
	@SerializedName("id")
	private Long eventId;

	@Expose
	@SerializedName("uid")
	private String eventUid;

	private Long companyId;
	private Long groupId;
	private Long userId;

	@Expose
	private String title;

	private String summary;

	private String description;

	/**
	 * No longer used. Need to use EventVenue instead
	 */
	@Deprecated
	private String location;

	private ContentSecurityLevel securityLevel = ContentSecurityLevel.GROUP;
	private boolean featuredEvent;

	private CalEvent calEvent;
	private User user;

	@Expose
	private EventDates dates;

	private EventVenue venue;
	private EventOrganizer organizer;
	private EventbriteDetails eventbrite;

	public EventModel() {
		dates = new EventDates();
		venue = new EventVenue();
		organizer = new EventOrganizer();
		eventbrite = new EventbriteDetails();
	}

	public String getDescriptionForEditor() {
		return UnicodeFormatter.toString(description);
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(final Long eventId) {
		this.eventId = eventId;
	}

	public String getEventUid() {
		return eventUid;
	}

	public void setEventUid(final String eventUid) {
		this.eventUid = eventUid;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(final Long companyId) {
		this.companyId = companyId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(final Long groupId) {
		this.groupId = groupId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(final Long userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	private ContentSecurityLevel getSecurityLevel() {
		return Validator.isNotNull(securityLevel) ? securityLevel : ContentSecurityLevel.GROUP;
	}

	public String getSecurityLevelKey() {
		return getSecurityLevel().getKey();
	}

	public void setSecurityLevel(final ContentSecurityLevel securityLevel) {
		this.securityLevel = securityLevel;
	}

	public boolean isSecurityLevelPublic() {
		return getSecurityLevel().equals(ContentSecurityLevel.PUBLIC);
	}

	public boolean isSecurityLevelGroup() {
		return getSecurityLevel().equals(ContentSecurityLevel.GROUP);
	}

	public boolean isSecurityLevelNetwork() {
		return getSecurityLevel().equals(ContentSecurityLevel.NETWORK);
	}

	public boolean isFeaturedEvent() {
		return featuredEvent;
	}

	public void setFeaturedEvent(final boolean featuredEvent) {
		this.featuredEvent = featuredEvent;
	}

	public CalEvent getCalEvent() {
		return calEvent;
	}

	public void setCalEvent(final CalEvent calEvent) {
		this.calEvent = calEvent;
	}

	public EventDates getDates() {
		return dates;
	}

	public void setDates(final EventDates dates) {
		this.dates = dates;
	}

	public EventVenue getVenue() {
		return venue;
	}

	public EventbriteDetails getEventbrite() {
		return eventbrite;
	}

	public EventOrganizer getOrganizer() {
		return organizer;
	}

	public void setVenue(final EventVenue venue) {
		this.venue = venue;
	}

	public void setOrganizer(final EventOrganizer organizer) {
		this.organizer = organizer;
	}

	public void setEventbrite(final EventbriteDetails eventbrite) {
		this.eventbrite = eventbrite;
	}

	public boolean isHasEditPermission() {
		return hasEditPermission;
	}

	public void setHasEditPermission(final boolean hasEditPermission) {
		this.hasEditPermission = hasEditPermission;
	}

	public User getUser() {
		return user;
	}

	public void setUser(final User user) {
		this.user = user;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(final String summary) {
		this.summary = summary;
	}

	/**
	 * This field should no longer be used. Kept for migrated events
	 * @param location
	 */
	@Deprecated
	public String getLocation() {
		return location;
	}

	/**
	 * This field should no longer be used. Kept for migrated events
	 * @param location
	 */
	@Deprecated
	public void setLocation(final String location) {
		this.location = location;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof EventModel) {
			final EventModel event = (EventModel) obj;
			return event.getEventId() == eventId && event.getGroupId() == groupId && event.getTitle().equals(title);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
