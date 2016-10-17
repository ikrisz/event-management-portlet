package com.pfiks.intelligus.events.service.impl.eventbrite;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.model.event.PaymentMethod;
import com.pfiks.intelligus.events.utils.EventbriteModelUtils;

@Component
public class EventbritePaymentService {

    private static final Log LOG = LogFactoryUtil.getLog(EventbritePaymentService.class);

    @Resource
    private HttpRequestUtil eventbriteApis;

    @Resource
    private EventbriteModelUtils eventbriteModelUtils;

    private static final String USER_KEY_PARAM = "user_key";
    private static final String APP_KEY_PARAM = "app_key";

    private static final String CREATE_PAYMENT = "https://www.eventbrite.com/json/payment_update";

    public void createPaymentMethod(final String eventbriteAppKey, final String userKey, final EventModel event, final String eventbriteId) throws EventbriteException,
	    EventbriteErrorException {
	final RequestParameters params = new RequestParameters();
	params.addParam(APP_KEY_PARAM, eventbriteAppKey);
	params.addParam(USER_KEY_PARAM, userKey);
	params.addParam("event_id", eventbriteId);

	final PaymentMethod payment = event.getEventbrite().getPayment();

	params.addBooleanParam("accept_paypal", payment.isPaypalAccepted());
	params.addParamAlways("paypal_email", payment.getPaypalEmail());

	params.addBooleanParam("accept_check", payment.isCheckAccepted());
	if (payment.isCheckAccepted()) {
	    params.addParamAlways("instructions_check", payment.getCheckInstructions());
	}

	params.addBooleanParam("accept_cash", payment.isCashAccepted());
	if (payment.isCashAccepted()) {
	    params.addParamAlways("instructions_cash", payment.getCashInstructions());
	}

	params.addBooleanParam("accept_invoice", payment.isInvoiceAccepted());
	if (payment.isInvoiceAccepted()) {
	    params.addParamAlways("instructions_invoice", payment.getInvoiceInstructions());
	}

	final JSONObject jsonResponse = eventbriteApis.executeCall(CREATE_PAYMENT, params);
	if (!eventbriteApis.wasRequestSuccessful(jsonResponse)) {
	    final EventbriteError exceptionMessage = eventbriteApis.getContentExceptionMessage(jsonResponse);
	    LOG.info("Unable to create payment type: " + exceptionMessage);
	    throw new EventbriteErrorException("Unable to create payment type in eventbrite: " + exceptionMessage);
	}

    }
}
