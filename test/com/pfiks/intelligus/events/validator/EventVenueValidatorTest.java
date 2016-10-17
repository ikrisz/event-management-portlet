package com.pfiks.intelligus.events.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.model.event.EventVenue;

public class EventVenueValidatorTest extends EventValidatorTest {

    @Test
    public void testThat_onlineVenue_isValid() {
	event.setVenue(anOnlineVenue());
	validate();
	assertThat_allErrors_NeverContain("venue.name-required");
    }

    @Test
    public void testThat_previousVenueSelection_isValid() {
	event.setVenue(anExistingVenue());
	validate();
	assertThat_EventbriteErrors_NeverContain("venue.name-required");
    }

    @Test
    public void testThat_locationVenue_venueName_alwaysMadatory() {
	final EventVenue venue = aNewVenue();
	venue.setName(StringPool.BLANK);
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.name-required");
    }

    @Test
    public void testThat_locationVenue_venueName_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setName(StringUtils.rightPad(venue.getName(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.name-too-long");
    }

    @Test
    public void testThat_locationVenue_venueAddressOne_alwaysMadatory() {
	final EventVenue venue = aNewVenue();
	venue.setAddressLineOne(StringPool.BLANK);
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.addressLineOne-required");
    }

    @Test
    public void testThat_locationVenue_venueAddressOne_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setAddressLineOne(StringUtils.rightPad(venue.getAddressLineOne(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.addressLineOne-too-long");
    }

    @Test
    public void testThat_locationVenue_venueAddressTwo_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setAddressLineTwo(StringUtils.rightPad(venue.getAddressLineTwo(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.addressLineTwo-too-long");
    }

    @Test
    public void testThat_locationVenue_venueCity_alwaysMadatory() {
	final EventVenue venue = aNewVenue();
	venue.setCity(StringPool.BLANK);
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.city-required");
    }

    @Test
    public void testThat_locationVenue_venueCity_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setCity(StringUtils.rightPad(venue.getCity(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.city-too-long");
    }

    @Test
    public void testThat_locationVenue_venueRegionState_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setRegionState(StringUtils.rightPad(venue.getRegionState(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.regionState-too-long");
    }

    @Test
    public void testThat_locationVenue_venueZip_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setZip(StringUtils.rightPad(venue.getZip(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.zip-too-long");
    }

    @Test
    public void testThat_locationVenue_venueCountry_alwaysMadatory() {
	final EventVenue venue = aNewVenue();
	venue.setCountry(StringPool.BLANK);
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.country-required");
    }

    @Test
    public void testThat_locationVenue_venueCountry_alwaysMaxLenght_100() {
	final EventVenue venue = aNewVenue();
	venue.setCountry(StringUtils.rightPad(venue.getCountry(), 101));
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.country-too-long");
    }

    @Test
    public void testThat_locationVenue_venueRegionState_mandatory_whenCountryIsUSA() {
	final EventVenue venue = aNewVenue();
	venue.setCountry("US");
	venue.setRegionState(StringPool.BLANK);
	event.setVenue(venue);
	validate();
	assertThat_allErrors_AlwaysContain("venue.regionState.usa-required");
    }

    @Test
    public void testThat_locationVenue_usa_isValid() {
	final EventVenue venue = aNewVenue();
	venue.setCountry("US");
	event.setVenue(venue);
	validate();
	assertThat_allErrors_NeverContain("venue.name-required");
	assertThat_allErrors_NeverContain("venue.name-too-long");
	assertThat_allErrors_NeverContain("venue.addressLineOne-required");
	assertThat_allErrors_NeverContain("venue.addressLineOne-too-long");
	assertThat_allErrors_NeverContain("venue.addressLineTwo-too-long");
	assertThat_allErrors_NeverContain("venue.city-required");
	assertThat_allErrors_NeverContain("venue.city-too-long");
	assertThat_allErrors_NeverContain("venue.regionState-too-long");
	assertThat_allErrors_NeverContain("venue.zip-too-long");
	assertThat_allErrors_NeverContain("venue.country-required");
	assertThat_allErrors_NeverContain("venue.country-too-long");
	assertThat_allErrors_NeverContain("venue.regionState.usa-required");
    }
}
