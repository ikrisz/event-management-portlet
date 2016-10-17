package com.pfiks.intelligus.events.validator;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.pfiks.intelligus.events.model.RecurrenceTypes;
import com.pfiks.intelligus.events.model.event.EventDates;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventTicket;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.model.event.PaymentMethod;

/**
 * General fields validation:
 * - title: mandatory, max 75
 * - description: mandatory, max 2000
 * - summary: max 2000
 *
 * Dates validation:
 * - startDate: mandatory. If eventbrite is enabled, must be after today's date
 * - endDate: mandatory, after start date
 * 	If recurrency selected (recurrenceType selected)
 * 		- recurrenceType: mandatory
 * 		- recurrencyEndDate: mandatory, must be after event's endDate
 * 	If recurrenceType = Daily:
 * 		- dayInterval: mandatory
 * 	If recurrenceType = Weekly:
 * 		- weekInterval: mandatory
 * 		- daySelectionInterval: mandatory
 * 	If recurrenceType = Montly:
 * 		- monthInterval: mandatory
 * 		- dayInterval: mandatory
 *
 * Venue validation:
 * If event is online, NO validation required
 * If eventbrite is enabled and venueId is set, NO validation required
 * In all other cases:
 * -venueName: mandatory, max 100
 * -venueAddressLineOne: mandatory, max 100
 * -venueAddressLineTwo: max 100
 * -venueCity: mandatory, max 100
 * -venueCountry: mandatory, max 100
 * -venueRegionState: max 100. If country is US, regionState is mandatory
 * -venueZip: max 100
 *
 * Organizer validation:
 * Only if eventbrite is enabled
 * If organizerId is set, NO validation required
 * In all other cases:
 * -organizerName: mandatory, max 100
 *
 * Payment methods:
 * Only if eventbrite is enabled and the event is being created
 * -At least one payment method must be selected
 *
 * Returns Set of error keys
 */
@Component
public class EventValidator {

    public Set<String> validateEvent(final EventModel event) {
	final Set<String> errors = Sets.newHashSet();
	validateGeneralFields(event, errors);
	validateVenue(event, errors);
	validateDates(event, false, errors);
	validateRecurrencyDates(event, errors);
	errors.remove(null);
	return errors;
    }

    public Set<String> validateEventbriteEvent(final EventModel event, final String indexTicketsToRemove, Boolean updateTickets) {
	final Set<String> errors = Sets.newHashSet();
	validateGeneralFields(event, errors);
	validateDates(event, true, errors);

	if (StringUtils.isBlank(event.getVenue().getVenueId())) {
	    validateVenue(event, errors);
	}
	validateEventbriteGeneralFields(event, errors);
	validateOrganizer(event, errors);
	if (updateTickets) {
	    validateTickets(event, indexTicketsToRemove, errors);
	    validateTicketsDates(event, errors);
	}
	// Payment validated only for new events
	if (StringUtils.isBlank(event.getEventbrite().getEventbriteId())) {
	    validatePaymentMethod(event, errors);
	}
	errors.remove(null);
	return errors;
    }

    private void validateGeneralFields(final EventModel event, final Set<String> errors) {
	validateFieldMandatoryWithMaxLenght(errors, event.getTitle(), "title", 75);
	String stripHtml = HtmlUtil.stripHtml(event.getDescription());
	validateFieldMaxLenght(errors, stripHtml, "description", 2000);
	validateFieldMaxLenght(errors, event.getSummary(), "summary", 2000);
    }

    private void validateEventbriteGeneralFields(final EventModel event, final Set<String> errors) {
	validateFieldMandatoryWithMaxLenght(errors, event.getEventbrite().getCurrency(), "eventbrite.currency", 10);
    }

    private void validatePaymentMethod(final EventModel event, final Set<String> errors) {
	final PaymentMethod payment = event.getEventbrite().getPayment();

	final String paypalEmail = StringUtils.trimToNull(payment.getPaypalEmail());
	validateFieldMandatoryWithMaxLenght(errors, paypalEmail, "eventbrite.payment.paypalEmail", 75);
	validateFieldEmail(errors, payment.getPaypalEmail(), "eventbrite.payment.paypalEmail");

	if (payment.isCashAccepted()) {
	    validateFieldMaxLenght(errors, payment.getCashInstructions(), "eventbrite.payment.instructions.cash", 200);
	}

	if (payment.isCheckAccepted()) {
	    validateFieldMaxLenght(errors, payment.getCheckInstructions(), "eventbrite.payment.instructions.check", 200);
	}

	if (payment.isInvoiceAccepted()) {
	    errors.add(checkMaxLenght(payment.getInvoiceInstructions(), "eventbrite.payment.instructions.invoice", 200));
	}

    }

    private void validateTickets(final EventModel event, final String indexTicketsToRemove, final Set<String> errors) {
	final List<EventTicket> tickets = event.getEventbrite().getTickets();
	final String[] split = StringUtils.split(indexTicketsToRemove, StringPool.COMMA);
	final Set<String> indexToRemove = Sets.newHashSet(split);
	if (isEmptyTicketList(tickets)) {
	    errors.add("eventbrite.tickets-required");
	} else {
	    final List<EventTicket> validatedTickets = Lists.newArrayList(new EventTicket());
	    int index = 1;
	    for (int i = 0; i < tickets.size(); i++) {
		if (!indexToRemove.contains(String.valueOf(i))) {
		    final EventTicket ticket = tickets.get(i);
		    if (Validator.isNotNull(ticket) && !ticket.isNullTicket()) {
			final String type = ticket.getType();
			validateFieldMandatoryWithMaxLenght(errors, ticket.getName(), "eventbrite.tickets[" + index + "].name", 75);
			validateFieldMandatory(errors, type, "eventbrite.tickets[" + index + "].type");
			validateTicketPrice(errors, index, ticket, ticket.getPrice(), type);
			validateTicketQuantity(errors, index, type, ticket.getQuantityAvailable());
			validatedTickets.add(ticket);
			index++;
		    }
		}
	    }
	    event.getEventbrite().setTickets(validatedTickets);

	    if (isEmptyTicketList(validatedTickets) || validatedTickets.size() == 1) {
		errors.add("eventbrite.tickets-required");
	    }
	}
    }

    private boolean isEmptyTicketList(final List<EventTicket> validatedTickets) {
	return validatedTickets == null || validatedTickets.isEmpty();
    }

    private void validateTicketQuantity(final Set<String> errors, final int index, final String type, final String quantity) {
	final String ticketQuantityAvailable = StringUtils.trimToNull(quantity);
	if (StringUtils.isNotBlank(ticketQuantityAvailable)) {
	    if (!NumberUtils.isDigits(ticketQuantityAvailable)) {
		errors.add("eventbrite.tickets[" + index + "].quantityAvailable-invalid");
	    } else if (Integer.valueOf(ticketQuantityAvailable) <= 0) {
		errors.add("eventbrite.tickets[" + index + "].quantityAvailable-invalid");
	    }
	} else if (!type.equalsIgnoreCase("donation")) {
	    errors.add("eventbrite.tickets[" + index + "].quantityAvailable-required");
	}
    }

    private void validateTicketPrice(final Set<String> errors, final int index, final EventTicket ticket, final String ticketPrice, final String type) {
	if (type.equalsIgnoreCase("paid")) {
	    if (StringUtils.isBlank(StringUtils.trimToNull(ticketPrice))) {
		errors.add("eventbrite.tickets[" + index + "].price-required");
	    } else {
		if (!NumberUtils.isNumber(ticketPrice)) {
		    errors.add("eventbrite.tickets[" + index + "].price-invalid");
		} else if (Double.valueOf(ticketPrice) <= 0.0) {
		    errors.add("eventbrite.tickets[" + index + "].price-invalid");
		}
	    }
	} else {
	    ticket.setPrice("0.00");
	}
    }

    private void validateOrganizer(final EventModel event, final Set<String> errors) {
	if (StringUtils.isBlank(event.getOrganizer().getOrganizerId())) {
	    validateFieldMandatoryWithMaxLenght(errors, event.getOrganizer().getName(), "organizer.name", 100);
	}
    }

    private void validateVenue(final EventModel event, final Set<String> errors) {
	final EventVenue venue = event.getVenue();
	if (!venue.isOnline()) {

	    validateFieldMandatoryWithMaxLenght(errors, venue.getName(), "venue.name", 100);
	    validateFieldMandatoryWithMaxLenght(errors, venue.getAddressLineOne(), "venue.addressLineOne", 100);
	    validateFieldMandatoryWithMaxLenght(errors, venue.getCity(), "venue.city", 100);
	    validateFieldMandatoryWithMaxLenght(errors, venue.getCountry(), "venue.country", 100);

	    validateFieldMaxLenght(errors, venue.getAddressLineTwo(), "venue.addressLineTwo", 100);
	    validateFieldMaxLenght(errors, venue.getRegionState(), "venue.regionState", 100);
	    validateFieldMaxLenght(errors, venue.getZip(), "venue.zip", 100);

	    if (StringUtils.isNotBlank(venue.getCountry()) && venue.getCountry().equalsIgnoreCase("US")) {
		validateFieldMandatory(errors, venue.getRegionState(), "venue.regionState.usa");
	    }
	}
    }

    private void validateDates(final EventModel event, final boolean eventbriteEnabled, final Set<String> errors) {
	final EventDates dates = event.getDates();

	final DateTime eventStartDate = dates.getStartDate();
	final DateTime eventEndDate = dates.getEndDate();

	validateMandatoryDate(errors, eventStartDate, "dates.startDate");
	validateMandatoryDate(errors, eventEndDate, "dates.endDate");
	validateDateOneShouldNotBeBeforeDateTwo(errors, eventEndDate, eventStartDate, "dates.endDate");

	// If eventbrite enabled, startdate must be after now, as eventbrite
	// doesn't allow past dates
	if (eventbriteEnabled) {
	    validateDateOneShouldNotBeBeforeDateTwo(errors, eventStartDate, DateTime.now(), "dates.startDate.eventbrite");
	}
    }

    private void validateTicketsDates(final EventModel event, final Set<String> errors) {
	final DateTime ticketStartDate = event.getEventbrite().getTicketsStartDate();
	final DateTime ticketsEndDate = event.getEventbrite().getTicketsEndDate();

	validateMandatoryDate(errors, ticketStartDate, "eventbrite.tickets.startDate");
	validateMandatoryDate(errors, ticketsEndDate, "eventbrite.tickets.endDate");

	validateDateOneShouldNotBeBeforeDateTwo(errors, ticketsEndDate, ticketStartDate, "eventbrite.tickets.endDate");

	validateDateOneShouldNotBeBeforeDateTwo(errors, ticketStartDate, DateTime.now(), "eventbrite.tickets.startDate");

	final DateTime eventEndDate = event.getDates().getEndDate();
	validateDateOneShouldNotBeBeforeDateTwo(errors, eventEndDate, ticketStartDate, "eventbrite.tickets.startDate");
	validateDateOneShouldNotBeBeforeDateTwo(errors, eventEndDate, ticketsEndDate, "eventbrite.tickets.endDate");
    }

    /**
     * Adds "-invalid" to label
     */
    private void validateDateOneShouldNotBeBeforeDateTwo(final Set<String> errors, final DateTime dateOne, final DateTime dateTwo, final String fieldName) {
	try {
	    if (dateOne.isBefore(dateTwo)) {
		errors.add(fieldName + "-invalid");
	    }
	} catch (final NullPointerException e) {
	    // Ignore null dates
	}
    }

    /**
     * Adds "-required" to label
     */
    private void validateMandatoryDate(final Set<String> errors, final DateTime dateToValidate, final String fieldName) {
	if (Validator.isNull(dateToValidate)) {
	    errors.add(fieldName + "-required");
	}
    }

    private void validateRecurrencyDates(final EventModel event, final Set<String> errors) {
	final EventDates dates = event.getDates();
	final RecurrenceTypes eventRecurrence = dates.getRecurrenceType();
	if (isRecurrentEvent(eventRecurrence)) {
	    final DateTime recurrenceEndDate = dates.getRecurrenceEndDate();
	    validateMandatoryDate(errors, recurrenceEndDate, "dates.recurrenceEndDate");
	    validateDateOneShouldNotBeBeforeDateTwo(errors, recurrenceEndDate, dates.getEndDate(), "dates.recurrenceEndDate");

	    final boolean invalidDayInterval = isInvalidNumber(dates.getRecurrenceDayInterval());
	    final boolean invalidWeekInterval = isInvalidNumber(dates.getRecurrenceWeekInterval());
	    final boolean invalidMonthInterval = isInvalidNumber(dates.getRecurrenceMonthInterval());
	    final boolean invalidDayIntervalSelection = dates.getRecurrenceDaySelectionInterval() == null || dates.getRecurrenceDaySelectionInterval().isEmpty();

	    if (eventRecurrence.equals(RecurrenceTypes.WEEKLY)) {
		if (invalidWeekInterval) {
		    errors.add("dates.recurrenceWeekInterval-required");
		}
		if (invalidDayIntervalSelection) {
		    errors.add("dates.recurrenceDayInterval-required");
		}
	    } else {
		if (invalidDayInterval) {
		    errors.add("dates.recurrenceDayInterval-required");
		}
		if (eventRecurrence.equals(RecurrenceTypes.MONTHLY) && invalidMonthInterval) {
		    errors.add("dates.recurrenceMonthInterval-required");
		}
	    }
	}
    }

    private boolean isRecurrentEvent(final RecurrenceTypes eventRecurrence) {
	return eventRecurrence != null && !eventRecurrence.equals(RecurrenceTypes.NONE);
    }

    private boolean isInvalidNumber(final int numberToCheck) {
	return Integer.valueOf(numberToCheck) == null || numberToCheck <= 0;
    }

    private void validateFieldMaxLenght(final Set<String> errors, final String value, final String label, final int maxLenght) {
	errors.add(checkMaxLenght(value, label, maxLenght));
    }

    private void validateFieldMandatory(final Set<String> errors, final String value, final String label) {
	errors.add(isRequired(value, label));
    }

    /**
     * Adds "-required" to label
     * Adds "-too-long" to label
     */
    private void validateFieldMandatoryWithMaxLenght(final Set<String> errors, final String value, final String label, final int maxLenght) {
	errors.add(isRequired(value, label));
	errors.add(checkMaxLenght(value, label, maxLenght));
    }

    /**
     * Adds "-invalid" to label
     */
    private void validateFieldEmail(final Set<String> errors, final String value, final String label) {
	if (StringUtils.isNotBlank(value) && !Validator.isEmailAddress(value)) {
	    errors.add(label + "-invalid");
	}
    }

    /**
     * Adds "-too-long" to label
     */
    private String checkMaxLenght(final String value, final String label, final int maxLenght) {
	String result = null;
	if (StringUtils.isNotBlank(value) && value.length() > maxLenght) {
	    result = label + "-too-long";
	}
	return result;
    }

    /**
     * Adds "-required" to label
     */
    private String isRequired(final String value, final String label) {
	String result = null;
	if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
	    result = label + "-required";
	}
	return result;
    }

}
