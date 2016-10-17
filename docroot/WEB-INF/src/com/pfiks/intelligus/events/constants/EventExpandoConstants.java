package com.pfiks.intelligus.events.constants;

import com.pfiks.intelligus.networks.portal.util.NetworkKeys;
import com.pfiks.intelligus.util.CustomExpandoFieldsNames;

/**
 * Expando fields used for CalEvent
 *
 * @author Ilenia Zedda
 *
 */
public interface EventExpandoConstants {

	String SECURITY_LEVEL = CustomExpandoFieldsNames.SECURITY_LEVEL;

	/**
	 * @Deprecated - Use SECURITY_LEVEL instead
	 */
	@Deprecated
	String PUBLIC_FLAG = "public";

	String FEATURED = "featured";

	String ONLINE_EVENT = "online";

	String SUMMARY = "summary";

	String NETWORK_ID = NetworkKeys.NETWORK_ID;

	/**
	 * This field should no longer be used. Kept for migrated events - it is not read-only.
	 * New location fields to be used instead:
	 * Venue, Address1, Address2, City, Region/state, Zipcode, country
	 */
	@Deprecated
	String LOCATION = "location";

	String VENUE = "event-venue";

	String ADDRESS_1 = "event-address-1";

	String ADDRESS_2 = "event-address-2";

	String CITY = "event-city";

	String REGION_STATE = "event-region";

	String ZIP_CODE = "event-zip";

	String COUNTRY = "event-country";

	String EVENTBRITE_ID = "eventbrite-eventId";

	String EVENTBRITE_USER_API = "eventbrite-user-apikey";

}
