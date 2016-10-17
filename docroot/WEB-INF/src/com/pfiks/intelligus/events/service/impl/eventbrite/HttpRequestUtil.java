package com.pfiks.intelligus.events.service.impl.eventbrite;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.pfiks.intelligus.events.exception.EventbriteException;

@Component
public class HttpRequestUtil {

    private static final Log LOG = LogFactoryUtil.getLog(HttpRequestUtil.class);
    /**
     * This is injected with values from eventbrite-api-errors.xml
     */
    private Map<String, List<EventbriteError>> eventbriteErrorMap;

    public void setEventbriteErrorMap(final Map<String, List<EventbriteError>> eventbriteErrorMap) {
	this.eventbriteErrorMap = eventbriteErrorMap;
    }

    public EventbriteError getContentExceptionMessage(final JSONObject jsonResponse) throws EventbriteException {
	try {
	    final JSONObject error = jsonResponse.getJSONObject("error");
	    final String errorType = error.getString("error_type");
	    String errorMessage = error.getString("error_message");

	    final List<EventbriteError> managedErrors = eventbriteErrorMap.get(errorType);
	    if (managedErrors == null || managedErrors.isEmpty()) {
		throw new EventbriteException("Unable to find mapped error message. ErrorType: " + errorType + ". errorMessage: " + errorMessage);
	    }

	    if (errorMessage.length() > 40) {
		errorMessage = StringUtils.substring(errorMessage, 0, 40);
	    }

	    final String errorToFind = errorMessage;
	    final Optional<EventbriteError> tryFind = Iterables.tryFind(managedErrors, new Predicate<EventbriteError>() {

		@Override
		public boolean apply(final EventbriteError ebe) {
		    return ebe.getErrorMessage().startsWith(errorToFind);
		}
	    });

	    if (tryFind.isPresent()) {
		return tryFind.get();
	    } else {
		throw new EventbriteException("Unable to find mapped error message. ErrorType: " + errorType + ", errorMessage: " + errorMessage);
	    }
	} catch (final JSONException e) {
	    LOG.error(e);
	    throw new EventbriteException(e);
	}
    }

    public JSONObject executeCall(final String url, final RequestParameters params) throws EventbriteException {
	LOG.debug("Excecuting HttpPost to url: " + url);
	CloseableHttpClient httpclient = null;
	CloseableHttpResponse httpResponse = null;
	try {
	    httpclient = HttpClients.createDefault();
	    final HttpPost httpRequest = new HttpPost(url);

	    if (addParamsToRequest(params)) {
		httpRequest.setEntity(params.getParams());
	    }
	    httpResponse = httpclient.execute(httpRequest);
	    final StatusLine statusLine = httpResponse.getStatusLine();
	    LOG.debug("Request completed. StatusCode: " + statusLine.getStatusCode() + " -  " + statusLine.getReasonPhrase());
	    return getJsonFromResponse(httpResponse);
	} catch (final Exception e) {
	    LOG.error("Exception executing call to url: " + url + ". " + Throwables.getRootCause(e));
	    throw new EventbriteException("Exception executing call to url: " + url, e);
	} finally {
	    closeQuietly(httpclient, httpResponse);
	}
    }

    public boolean wasRequestSuccessful(final JSONObject jsonResponse) {
	return Validator.isNull(jsonResponse.opt("error"));
    }

    public boolean errorMessageMatches(final JSONObject jsonResponse, final String messageToMatch) {
	try {
	    final JSONObject error = jsonResponse.getJSONObject("error");
	    final String errorMessage = error.getString("error_message").toLowerCase();
	    return StringUtils.startsWith(errorMessage, messageToMatch.toLowerCase());
	} catch (final JSONException e) {
	    return false;
	}
    }

    public String getErrorType(final JSONObject jsonResponse) {
	try {
	    final JSONObject error = jsonResponse.getJSONObject("error");
	    return error.getString("error_type");
	} catch (final JSONException e) {
	    return StringPool.BLANK;
	}
    }

    private boolean addParamsToRequest(final RequestParameters params) {
	return params != null && params.hasParams();
    }

    private JSONObject getJsonFromResponse(final CloseableHttpResponse response) throws ParseException, IOException, JSONException {
	JSONObject result = null;
	final HttpEntity entity = response.getEntity();
	final String entityString = EntityUtils.toString(entity);
	final String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType();
	if (isJsonResponse(mimeType)) {
	    result = new JSONObject(entityString);
	} else if (isXmlResponse(mimeType)) {
	    result = XML.toJSONObject(entityString);
	}
	EntityUtils.consume(entity);
	if (LOG.isTraceEnabled()) {
	    LOG.trace(result.toString());
	}
	return result;
    }

    private boolean isXmlResponse(final String mimeType) {
	return mimeType.equals(ContentType.APPLICATION_XML.getMimeType()) || mimeType.equals(ContentType.APPLICATION_ATOM_XML.getMimeType());
    }

    private boolean isJsonResponse(final String mimeType) {
	return mimeType.equals(ContentType.APPLICATION_JSON.getMimeType());
    }

    private void closeQuietly(final CloseableHttpClient httpclient, final CloseableHttpResponse response) {
	try {
	    if (response != null) {
		response.close();
	    }
	    if (httpclient != null) {
		httpclient.close();
	    }
	} catch (final IOException e) {
	    LOG.warn("Exception closing Client/Response. " + Throwables.getRootCause(e));
	}
    }

}
