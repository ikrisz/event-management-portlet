package com.pfiks.intelligus.events.model.event;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.util.portlet.PortletProps;

public class EventVenue {
    private static final String defaultCountryCode = GetterUtil.getString(PortletProps.get("event.default.country"), StringPool.BLANK);

    private boolean online;
    private String venueId;
    private String name;
    private String addressLineOne;
    private String addressLineTwo;
    private String city;
    private String regionState;
    private String zip;
    private String country;

    public EventVenue() {
	country = defaultCountryCode;
    }

    public boolean isOnline() {
	return online;
    }

    public void setOnline(final boolean online) {
	this.online = online;
    }

    public String getVenueId() {
	return venueId;
    }

    public void setVenueId(final String venueId) {
	this.venueId = venueId;
    }

    public String getName() {
	return name;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public String getAddressLineOne() {
	return addressLineOne;
    }

    public void setAddressLineOne(final String addressLineOne) {
	this.addressLineOne = addressLineOne;
    }

    public String getAddressLineTwo() {
	return addressLineTwo;
    }

    public void setAddressLineTwo(final String addressLineTwo) {
	this.addressLineTwo = addressLineTwo;
    }

    public String getCity() {
	return city;
    }

    public void setCity(final String city) {
	this.city = city;
    }

    public String getRegionState() {
	return regionState;
    }

    public void setRegionState(final String regionState) {
	this.regionState = regionState;
    }

    public String getZip() {
	return zip;
    }

    public void setZip(final String zip) {
	this.zip = zip;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(final String country) {
	this.country = country;
    }

}
