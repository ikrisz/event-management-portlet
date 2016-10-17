package com.pfiks.intelligus.events.service.impl.eventbrite;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.liferay.portal.kernel.util.Validator;

class RequestParameters {

    private static final DateTimeFormatter EVENTBRITE_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private final List<NameValuePair> nvps;

    public RequestParameters() {
	nvps = Lists.newArrayList();
    }

    /**
     * If value is blank, it will be skipped
     */
    public void addParam(final String key, final String value) {
	if (StringUtils.isNotBlank(value)) {
	    nvps.add(new BasicNameValuePair(key, value));
	}
    }

    /**
     * If value is blank, it will be added anyway as an empty string
     */
    public void addParamAlways(final String key, final String value) {
	nvps.add(new BasicNameValuePair(key, StringUtils.trimToEmpty(value)));
    }

    /**
     * If value is blank, it will be skipped. If the value is true, it will add
     * "1" If the value is false, it will add "0"
     */
    public void addBooleanParam(final String key, final Boolean value) {
	if (Validator.isNotNull(value)) {
	    final String valueConverted = value ? "1" : "0";
	    nvps.add(new BasicNameValuePair(key, valueConverted));
	}
    }

    /**
     * If value is blank, it will be skipped
     * 
     * @param key
     * @param dateTime
     */
    public void addDateParam(final String key, final DateTime date) {
	if (Validator.isNotNull(date)) {
	    final String dateTime = date.toString(EVENTBRITE_DATE_FORMATTER);
	    nvps.add(new BasicNameValuePair(key, dateTime));
	}
    }

    public boolean hasParams() {
	return nvps != null && !nvps.isEmpty();
    }

    public UrlEncodedFormEntity getParams() throws UnsupportedEncodingException {
	return new UrlEncodedFormEntity(nvps);
    }
}
