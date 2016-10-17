package com.pfiks.intelligus.events.validator;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.model.event.PaymentMethod;

public class PaymentMethodValidatorTest extends EventValidatorTest {

    @Before
    public void setEventAsnew() {
	event.getEventbrite().setEventbriteId(StringPool.BLANK);
    }

    @Test
    public void testThat_paypalEmail_isValidatedOnlyForNewEvents_mandatory() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setPaypalEmail(StringPool.BLANK);
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.payment.paypalEmail-required");
    }

    @Test
    public void testThat_paypalEmail_isValidatedOnlyForNewEvents_validEmailAddress() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setPaypalEmail("invalid@test");
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.payment.paypalEmail-invalid");
    }

    @Test
    public void testThat_paypalEmail_isValidatedOnlyForNewEvents_maxLenght_75() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setPaypalEmail(StringUtils.rightPad(payment.getPaypalEmail(), 76, "com"));
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.payment.paypalEmail-too-long");
    }

    @Test
    public void testThat_paypalCashInstructions_isValidatedOnlyForNewEvents_onlyIfCashAccepted_maxLenght_200() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setCashInstructions(StringUtils.rightPad(payment.getCashInstructions(), 201));
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.payment.instructions.cash-too-long");
    }

    @Test
    public void testThat_paypalCashInstructions_isNotValidated_ifCashNotAccepted_maxLenght_200() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setCashAccepted(false);
	payment.setCashInstructions(StringUtils.rightPad(payment.getCashInstructions(), 201));
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_allErrors_NeverContain("eventbrite.payment.instructions.cash-too-long");
    }

    @Test
    public void testThat_paypalCheckInstructions_isValidatedOnlyForNewEvents_onlyIfCheckAccepted_maxLenght_200() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setCheckInstructions(StringUtils.rightPad(payment.getCheckInstructions(), 201));
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.payment.instructions.check-too-long");
    }

    @Test
    public void testThat_paypalCheckInstructions_isNotValidated_ifCheckNotAccepted_maxLenght_200() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setCheckInstructions(StringUtils.rightPad(payment.getCheckInstructions(), 201));
	payment.setCheckAccepted(false);
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_allErrors_NeverContain("eventbrite.payment.instructions.check-too-long");
    }

    @Test
    public void testThat_paypalInvoiceInstructions_isValidatedOnlyForNewEvents_OnlyIfInvoiceAccepted_maxLenght_200() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setInvoiceInstructions(StringUtils.rightPad(payment.getInvoiceInstructions(), 201));
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_EventbriteErrors_AlwaysContain("eventbrite.payment.instructions.invoice-too-long");
    }

    @Test
    public void testThat_paypalInvoiceInstructions_isNotValidated_ifInvoiceNotAccepted_maxLenght_200() {
	final PaymentMethod payment = aPaymentMethod();
	payment.setInvoiceInstructions(StringUtils.rightPad(payment.getInvoiceInstructions(), 201));
	payment.setInvoiceAccepted(false);
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_allErrors_NeverContain("eventbrite.payment.instructions.invoice-too-long");
    }

    @Test
    public void testThat_paymentMethod_isValid() {
	final PaymentMethod payment = aPaymentMethod();
	event.getEventbrite().setPayment(payment);
	validate_onlyEventbrite();
	assertThat_allErrors_NeverContain("eventbrite.payment.paypalEmail-required");
	assertThat_allErrors_NeverContain("eventbrite.payment.paypalEmail-invalid");
	assertThat_allErrors_NeverContain("eventbrite.payment.paypalEmail-too-long");
	assertThat_allErrors_NeverContain("eventbrite.payment.instructions.cash-too-long");
	assertThat_allErrors_NeverContain("eventbrite.payment.instructions.check-too-long");
	assertThat_allErrors_NeverContain("eventbrite.payment.instructions.invoice-too-long");
    }

}
