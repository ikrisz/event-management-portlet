package com.pfiks.intelligus.events.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.pfiks.intelligus.events.model.event.EventAttendee;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventOrganizer;
import com.pfiks.intelligus.events.model.event.EventTicket;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.model.event.EventbriteDetails;
import com.pfiks.intelligus.util.ContentSecurityLevel;

@Component
public class EventbriteModelUtils {

	private static final Log LOG = LogFactoryUtil.getLog(EventbriteModelUtils.class);

	private final DateTimeFormatter eventbriteDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * Only sets event details and venue details
	 *
	 * @param jsonResponse
	 * @return
	 */
	public Collection<EventModel> getEventbriteEventsList(final JSONObject jsonResponse) {
		final Collection<EventModel> results = Lists.newArrayList();
		final JSONArray eventsJsonArray = jsonResponse.optJSONArray("events");
		if (eventsJsonArray != null) {
			final List<String> alreadyRetrievedEvents = Lists.newArrayList();
			for (int i = 0; i < eventsJsonArray.length(); i++) {
				final EventModel event = getEventbriteDetails(eventsJsonArray.optJSONObject(i), false, false, false, true);
				if (Validator.isNotNull(event) && !alreadyRetrievedEvents.contains(event.getEventbrite().getEventbriteId())) {
					results.add(event);
					alreadyRetrievedEvents.add(event.getEventbrite().getEventbriteId());
				}
			}
		}
		return results;
	}

	private DateTime getEventbriteDateInUTC(final String timezone, final String dateToParse) {
		final DateTime modifiedDateWithTimezone = eventbriteDateFormatter.withZone(DateTimeZone.forID(timezone)).parseDateTime(dateToParse);
		final DateTime modifiedDate = new DateTime(modifiedDateWithTimezone).withZone(DateTimeZone.UTC);
		return modifiedDate;
	}

	public EventModel getEventbriteDetails(JSONObject eventJson, final boolean includeTickets, final boolean includeOrganizer, final boolean includeModifiedDate,
			final boolean onlyValidEventStatus) {
		EventModel result = null;
		try {
			final JSONObject optJSONObject = eventJson.optJSONObject("event");
			if (optJSONObject != null) {
				eventJson = optJSONObject;
			}
			final String status = eventJson.optString("status");
			if (isAcceptedStatus(onlyValidEventStatus, status)) {
				result = new EventModel();
				final EventbriteDetails eventbrite = result.getEventbrite();
				eventbrite.setStatus(status.toLowerCase());
				eventbrite.setEventbriteId(getId(eventJson));
				result.setTitle(eventJson.optString("title"));
				result.setDescription(eventJson.optString("description"));
				final String repeatingEvent = eventJson.optString("repeats");
				eventbrite.setRecurrent(repeatingEvent.equalsIgnoreCase("yes"));
				final String privacyValue = eventJson.optString("privacy");
				final boolean publicEvent = privacyValue.equalsIgnoreCase("Public");
				if (publicEvent) {
					result.setSecurityLevel(ContentSecurityLevel.PUBLIC);
				} else {
					result.setSecurityLevel(ContentSecurityLevel.GROUP);
				}

				eventbrite.setUrl(eventJson.optString("url"));

				final String timezone = eventJson.optString("timezone");

				final DateTime startDate = getEventbriteDateInUTC(timezone, eventJson.optString("start_date"));
				result.getDates().setStartDate(startDate);

				final DateTime endDate = getEventbriteDateInUTC(timezone, eventJson.optString("end_date"));
				result.getDates().setEndDate(endDate);

				final int durationHour = Hours.hoursBetween(startDate, endDate).getHours();
				if (durationHour > 24) {
					eventbrite.setMultiday(true);
				}

				if (includeModifiedDate) {
					final DateTime modifiedDate = getEventbriteDateInUTC(timezone, eventJson.optString("modified"));
					eventbrite.setModifiedDate(modifiedDate);
				}

				result.setVenue(getVenueDetails(eventJson));

				if (includeTickets) {
					try {
						final JSONArray ticketsJsonArray = eventJson.optJSONArray("tickets");
						if (ticketsJsonArray != null) {
							final JSONObject jsonFirstTicket = ticketsJsonArray.optJSONObject(0).optJSONObject("ticket");

							eventbrite.setCurrency(jsonFirstTicket.optString("currency"));
							final DateTime ticketsEndDate = eventbriteDateFormatter.parseDateTime(jsonFirstTicket.optString("end_date"));
							eventbrite.setTicketsEndDate(ticketsEndDate);
							eventbrite.setTicketsEndMinute(ticketsEndDate.getMinuteOfHour());
							eventbrite.setTicketsEndHour(ticketsEndDate.getHourOfDay());
							eventbrite.setTicketsEndDay(ticketsEndDate.getDayOfMonth());
							eventbrite.setTicketsEndMonth(getMonthValueForLiferay(ticketsEndDate.getMonthOfYear()));
							eventbrite.setTicketsEndYear(ticketsEndDate.getYear());

							final DateTime ticketsStartDate = eventbriteDateFormatter.parseDateTime(jsonFirstTicket.optString("start_date"));
							eventbrite.setTicketsStartDate(ticketsStartDate);
							eventbrite.setTicketsStartMinute(ticketsStartDate.getMinuteOfHour());
							eventbrite.setTicketsStartHour(ticketsStartDate.getHourOfDay());
							eventbrite.setTicketsStartDay(ticketsStartDate.getDayOfMonth());
							eventbrite.setTicketsStartMonth(getMonthValueForLiferay(ticketsStartDate.getMonthOfYear()));
							eventbrite.setTicketsStartYear(ticketsStartDate.getYear());
						}
					} catch (final Exception e) {
						LOG.warn("Unable to add currency, ticketStartDate and ticketEndDate to EventModel. " + Throwables.getRootCause(e).getMessage());
					}
					eventbrite.setTickets(getTickets(eventJson));
				}

				if (includeOrganizer) {
					result.setOrganizer(getOrganizerDetails(eventJson));
				}

				result.setEventbrite(eventbrite);
			}
		} catch (final Exception e) {
			LOG.warn("Unable to convert json into EventModel. " + Throwables.getRootCause(e).getMessage());
		}
		return result;
	}

	private boolean isAcceptedStatus(final boolean onlyValidEventStatus, final String status) {
		if (onlyValidEventStatus) {
			return "live".equalsIgnoreCase(status) || "started".equalsIgnoreCase(status);
		} else {
			return true;
		}
	}

	private int getMonthValueForLiferay(final int val) {
		return val - 1;
	}

	// Parse tickets
	private List<EventTicket> getTickets(final JSONObject jsonResponse) {
		final List<EventTicket> results = Lists.newArrayList();
		final JSONArray ticketsJsonArray = jsonResponse.optJSONArray("tickets");
		if (ticketsJsonArray != null) {
			for (int i = 0; i < ticketsJsonArray.length(); i++) {
				final EventTicket ticket = getTicketDetails(ticketsJsonArray.optJSONObject(i));
				if (Validator.isNotNull(ticket)) {
					results.add(ticket);
				}
			}
		}
		return results;
	}

	public EventTicket getTicketDetails(JSONObject ticketJson) {
		EventTicket result = null;
		try {
			final JSONObject optJSONObject = ticketJson.optJSONObject("ticket");
			if (optJSONObject != null) {
				ticketJson = optJSONObject;
			}
			result = new EventTicket();
			result.setTicketId(getId(ticketJson));
			result.setName(ticketJson.optString("name"));
			final Integer ticketType = ticketJson.optInt("type", 0);
			final Double displayPrice = ticketJson.optDouble("display_price", 0.0);
			if (ticketType == 1) {
				result.setType("donation");
			} else {
				if (displayPrice > 0.0) {
					result.setType("paid");
					result.setPrice(String.valueOf(displayPrice));
				} else {
					result.setType("free");
				}
			}

			result.setQuantitySold(String.valueOf(ticketJson.optInt("quantity_sold", 0)));

			String quantity = String.valueOf(ticketJson.optInt("quantity_available", 0));
			if (StringUtils.isBlank(quantity) || quantity.equals("0")) {
				quantity = String.valueOf(ticketJson.optInt("max", 0));
			}
			result.setQuantityAvailable(quantity);

		} catch (final Exception e) {
			LOG.warn("Unable to convert json into EventTicket: " + Throwables.getRootCause(e).getMessage());
			result = null;
		}
		return result;
	}

	// Parse organizers
	public Collection<EventOrganizer> getOrganizersList(final JSONObject jsonResponse) {
		final Collection<EventOrganizer> results = Lists.newArrayList();
		final JSONArray organizersJsonArray = jsonResponse.optJSONArray("organizers");
		if (organizersJsonArray != null) {
			for (int i = 0; i < organizersJsonArray.length(); i++) {
				final EventOrganizer organizer = getOrganizerDetails(organizersJsonArray.optJSONObject(i));
				if (Validator.isNotNull(organizer)) {
					results.add(organizer);
				}
			}
		}
		return results;
	}

	public EventOrganizer getOrganizerDetails(JSONObject organizerJson) {
		EventOrganizer result = null;
		try {
			final JSONObject optJSONObject = organizerJson.optJSONObject("organizer");
			if (optJSONObject != null) {
				organizerJson = optJSONObject;
			}
			final String organizerId = getId(organizerJson);
			String name = organizerJson.optString("name");
			if (isValidOrganizer(organizerId, name)) {
				result = new EventOrganizer();
				name = StringUtils.replace(name, StringPool.PLUS, StringPool.SPACE);
				result.setOrganizerId(organizerId);
				result.setName(name);
			}
		} catch (final Exception e) {
			LOG.warn("Unable to convert json into EventOrganizer: " + Throwables.getRootCause(e).getMessage());
			result = null;
		}
		return result;
	}

	private boolean isValidOrganizer(final String id, final String name) {
		return StringUtils.isNotBlank(id) && StringUtils.isNotBlank(name);
	}

	// Parse venues
	public Collection<EventVenue> getVenuesList(final JSONObject jsonResponse) {
		final Collection<EventVenue> results = Lists.newArrayList();
		final JSONArray venuesJsonArray = jsonResponse.optJSONArray("venues");
		if (venuesJsonArray != null) {
			for (int i = 0; i < venuesJsonArray.length(); i++) {
				final EventVenue venue = getVenueDetails(venuesJsonArray.optJSONObject(i));
				if (Validator.isNotNull(venue)) {
					results.add(venue);
				}
			}
		}
		return results;
	}

	public EventVenue getVenueDetails(JSONObject venueJson) {
		EventVenue result = null;
		try {
			final JSONObject optJSONObject = venueJson.optJSONObject("venue");
			if (optJSONObject != null) {
				venueJson = optJSONObject;
			}
			final String id = getId(venueJson);
			final String name = venueJson.optString("name");
			final String address1 = venueJson.optString("address");
			final String city = venueJson.optString("city");
			final String country = venueJson.optString("country_code");
			final String region = venueJson.optString("region");
			if (isValidVenue(id, name, address1, city, country, region)) {
				result = new EventVenue();
				result.setVenueId(id);
				result.setName(name);
				result.setAddressLineOne(address1);
				result.setAddressLineTwo(venueJson.optString("address_2"));
				result.setCity(city);
				result.setRegionState(region);
				result.setZip(venueJson.optString("postal_code"));
				result.setCountry(country);
			}
		} catch (final Exception e) {
			LOG.warn("Unable to convert json into EventVenue: " + Throwables.getRootCause(e).getMessage());
			result = null;
		}
		return result;
	}

	public List<EventAttendee> getAttendeesList(final long companyId, final JSONObject jsonResponse, final EventModel event) {
		final Map<String, EventAttendee> results = Maps.newHashMap();
		final JSONArray attendeesJsonArray = jsonResponse.optJSONArray("attendees");
		if (attendeesJsonArray != null) {
			final List<EventTicket> tickets = event.getEventbrite().getTickets();
			for (int i = 0; i < attendeesJsonArray.length(); i++) {
				final EventAttendee attendee = getAttendeeDetails(companyId, attendeesJsonArray.optJSONObject(i), tickets);
				if (Validator.isNotNull(attendee)) {
					if (results.containsKey(attendee.getEmailAddress())) {
						final EventAttendee previousResult = results.get(attendee.getEmailAddress());
						previousResult.addTicketsPurchased(attendee.getTicketsPurchased());
						results.put(attendee.getEmailAddress(), previousResult);
					} else {
						results.put(attendee.getEmailAddress(), attendee);
					}
				}
			}
		}
		return Lists.newArrayList(results.values());
	}

	public EventAttendee getAttendeeDetails(final long companyId, JSONObject attendeeJson, final List<EventTicket> tickets) {
		EventAttendee result = null;
		try {
			final JSONObject optJSONObject = attendeeJson.optJSONObject("attendee");
			if (optJSONObject != null) {
				attendeeJson = optJSONObject;
			}
			result = new EventAttendee();
			result.setFirstName(attendeeJson.optString("first_name"));
			result.setLastName(attendeeJson.optString("last_name"));
			final String email = attendeeJson.optString("email");
			result.setEmailAddress(email);
			final String ticketId = attendeeJson.optString("ticket_id");
			final EventTicket ticket = findTicketPurchased(tickets, ticketId);
			if (ticket != null) {
				ticket.setQuantitySold(attendeeJson.optString("quantity"));
				result.addTicketPurchased(ticket);
			}
			setLiferayUserAttendee(companyId, result);
		} catch (final Exception e) {
			LOG.warn("Unable to convert json into EventAttendee: " + Throwables.getRootCause(e).getMessage());
			result = null;
		}
		return result;
	}

	private void setLiferayUserAttendee(final long companyId, final EventAttendee result) {
		try {
			final User userByEmailAddress = UserLocalServiceUtil.getUserByEmailAddress(companyId, result.getEmailAddress());
			result.setUser(userByEmailAddress);
			result.setFirstName(userByEmailAddress.getFirstName());
			result.setLastName(userByEmailAddress.getLastName());
		} catch (final Exception e) {
			LOG.warn("Unable to find Liferay user for attendee email: " + Throwables.getRootCause(e).getMessage());
		}
	}

	private EventTicket findTicketPurchased(final List<EventTicket> tickets, final String ticketId) {
		final Optional<EventTicket> tryFind = Iterables.tryFind(tickets, new Predicate<EventTicket>() {
			@Override
			public boolean apply(final EventTicket arg0) {
				return arg0.getTicketId().equals(ticketId);
			}
		});
		if (tryFind.isPresent()) {
			return tryFind.get();
		}
		return null;
	}

	private boolean isValidVenue(final String venueId, final String name, final String address1, final String city, final String country, final String region) {
		boolean isValid = StringUtils.isNotBlank(venueId) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(address1) && StringUtils.isNotBlank(city)
				&& StringUtils.isNotBlank(country);
		if (country.equalsIgnoreCase("US")) {
			isValid = isValid && StringUtils.isNotBlank(region);
		}
		return isValid;
	}

	public String getId(final JSONObject json) {
		return String.valueOf(json.opt("id"));
	}

	public String getIdFromCreateResponse(final JSONObject json) {
		return getId(json.optJSONObject("process"));
	}

}
