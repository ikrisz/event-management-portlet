package com.pfiks.intelligus.events.utils;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.pfiks.intelligus.events.model.event.EventAttendee;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.EventVenue;
import com.pfiks.intelligus.events.model.event.EventbriteDetails;
import com.pfiks.intelligus.util.PrivacySettingKeys;
import com.pfiks.intelligus.util.ProfilePrivacySettingUtil;

@Component
public class TaglibUtils {
    private static final Log LOG = LogFactoryUtil.getLog(TaglibUtils.class);

    public static boolean isDaySelectedAsRecurrence(final EventModel event, final Integer value) {
	boolean result = false;
	if (Validator.isNotNull(event.getDates()) && Validator.isNotNull(event.getDates().getRecurrenceDaySelectionInterval())) {
	    result = event.getDates().getRecurrenceDaySelectionInterval().contains(value);
	}
	return result;
    }

    public static String getFullVenueLocation(final EventModel event, final ThemeDisplay themeDisplay) {
	String result = StringPool.BLANK;
	final EventVenue venue = event.getVenue();
	if (Validator.isNotNull(venue) && !venue.isOnline()) {
	    result = getString(venue.getName(), venue.getAddressLineOne(), venue.getAddressLineTwo(), venue.getCity(), venue.getRegionState(), venue.getZip(),
		    getCountryNameBasedOnLocale(themeDisplay.getLocale(), venue.getCountry()));
	    if (StringUtils.isBlank(result)) {
		result = event.getLocation();
	    }
	}
	return result;
    }

    public static String getGoogleMapAddress(final EventModel event) {
	String result = StringPool.BLANK;
	final EventVenue venue = event.getVenue();
	if (Validator.isNotNull(venue) && !venue.isOnline()) {
	    result = getString(StringPool.BLANK, venue.getAddressLineOne(), venue.getAddressLineTwo(), venue.getCity(), venue.getRegionState(), venue.getZip(), venue.getCountry());
	    if (StringUtils.isBlank(result)) {
		result = event.getLocation();
	    }
	}
	result = StringUtils.replace(result, StringPool.NEW_LINE, StringPool.SPACE);
	return result;
    }

    public static String getShortVenueLocation(final EventModel event, final ThemeDisplay themeDisplay) {
	String result = StringPool.BLANK;
	final EventVenue venue = event.getVenue();
	if (Validator.isNotNull(venue) && !venue.isOnline()) {
	    result = getString(venue.getName(), venue.getCity(), venue.getRegionState(), getCountryNameBasedOnLocale(themeDisplay.getLocale(), venue.getCountry()));
	    if (StringUtils.isBlank(result)) {
		result = event.getLocation();
	    }
	}
	return result;
    }

    public static String getAttendeePortraitUrl(EventAttendee attendee, ThemeDisplay themeDisplay) {
	String result = themeDisplay.getPathImage() + "/user_male_portrait?img_id=0";
	try {
	    User liferayUser = attendee.getUser();
	    if (Validator.isNotNull(liferayUser) && ProfilePrivacySettingUtil.display(PrivacySettingKeys.TYPE_PROFILE_PICTURE, liferayUser.getUserId(), themeDisplay.getUserId())) {
		result = liferayUser.getPortraitURL(themeDisplay);
	    }
	} catch (final NestableException e) {
	    LOG.warn("Unable to retrieve user portrait id " + e.getMessage());
	}
	return result;
    }

    public static String getCurrencySymbol(EventbriteDetails eventbrite) {
	String eventCurrency = eventbrite.getCurrency();

	Currency cur = Currency.getInstance(eventCurrency);
	String currencySymbol = cur.getSymbol();
	if (StringUtils.isNotBlank(currencySymbol)) {
	    return currencySymbol;
	}
	return eventCurrency;
    }

    private static String getString(final String venue, final String... addressValues) {
	final String address = Joiner.on(StringPool.COMMA + StringPool.SPACE).skipNulls().join(getTrimmedValues(addressValues));

	return Joiner.on(StringPool.SPACE + StringPool.PIPE + StringPool.SPACE).skipNulls().join(Lists.newArrayList(StringUtils.trimToNull(venue), address));
    }

    private static String getCountryNameBasedOnLocale(final Locale userLocale, final String countryCodeToFind) {
	if (Validator.isNotNull(userLocale) && StringUtils.isNotBlank(countryCodeToFind)) {
	    final Locale obj = new Locale(StringPool.BLANK, countryCodeToFind);
	    return obj.getDisplayCountry(userLocale);
	}
	return StringPool.BLANK;
    }

    private static List<String> getTrimmedValues(final String... vals) {
	final List<String> results = Lists.newLinkedList();
	for (final String value : vals) {
	    results.add(StringUtils.trimToNull(value));
	}
	return results;
    }

}
