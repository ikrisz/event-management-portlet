package com.pfiks.intelligus.events.service.impl;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.liferay.mail.service.MailServiceUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.mail.MailMessage;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.journal.model.JournalArticle;
import com.liferay.portlet.journal.service.JournalArticleLocalServiceUtil;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.model.event.EventDates;
import com.pfiks.intelligus.events.model.event.EventModel;
import com.pfiks.intelligus.events.service.INotificationService;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;
import com.pfiks.intelligus.events.utils.TaglibUtils;

@Service
public class NotificationService implements INotificationService {
    public static final Log LOG = LogFactoryUtil.getLog(NotificationService.class);

    @Resource
    private ConfigurationUtils utils;

    private static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormat.forPattern("dd MMM yyyy HH:mm");
    private static final DateTimeFormatter DAYONLY_DATE_FORMAT = DateTimeFormat.forPattern("dd MMM yyyy");

    @Override
    public void sendEventInvite(List<String> emailAddresses, EventModel event, ThemeDisplay themeDisplay) throws Exception {
	long companyId = themeDisplay.getCompanyId();
	InternetAddress fromAddress = getFromAddress(companyId);
	final String content = getArticleContent(companyId,ConfigurationConstants.INVITE_USERS_ARTICLE_ID);
	final String subject = replaceContent(getArticleFieldValue(content, "subject"), new String[] { "[$EVENT_TITLE$]" }, new String[] { event.getTitle() });

	String eventTime = "";
	final EventDates dates = event.getDates();
	final DateTime startDate = dates.getStartDate();
	if (dates.isAllDay()) {
	    eventTime = startDate.toString(DAYONLY_DATE_FORMAT);
	    eventTime += " - All day";
	} else {
	    eventTime = startDate.toString(FULL_DATE_FORMAT);
	    eventTime = dates.getEndDate().toString(FULL_DATE_FORMAT);
	}
	final String body = replaceContent(getArticleFieldValue(content, "content"), new String[] { "[$EVENT_URL$]", "[$EVENT_TITLE$]", "[$EVENT_TIME$]", "[$EVENT_LOCATION$]" },
		new String[] { event.getEventbrite().getUrl(), event.getTitle(), eventTime, TaglibUtils.getFullVenueLocation(event, themeDisplay) });


	sendMailMessage(subject, body, fromAddress, getInternetAddressesFromStrings(emailAddresses));
    }

    @Override
    public void sendMessageToUser(String receiverEmailAddress, String body, ThemeDisplay themeDisplay) throws Exception {
	final User senderUser = themeDisplay.getUser();
	Company company = themeDisplay.getCompany();
	final String portalName = company.getName();
	long companyId = company.getCompanyId();
	final String content = getArticleContent(companyId,ConfigurationConstants.MESSAGE_USER_ARTICLE_ID);
	final String subject = replaceContent(getArticleFieldValue(content, "subject"), new String[] { "[$PORTAL_NAME$]" }, new String[] { portalName });
	final String emailBody = replaceContent(getArticleFieldValue(content, "content"), new String[] { "[$SENDER_USER_FULLNAME$]", "[$PORTAL_NAME$]", "[$SENDER_USER_EMAIL$]",
	"[$MESSAGE_CONTENT$]" }, new String[] { senderUser.getFullName(), portalName, senderUser.getEmailAddress(), body });

	sendMailMessage(subject, emailBody, getFromAddress(companyId), new InternetAddress[]{new InternetAddress(receiverEmailAddress)});
    }

    @Override
    public void sendImportErrorNotification(long companyId, InternetAddress[] toAddresses, String errors) throws Exception {
	final String content = getArticleContent(companyId,ConfigurationConstants.ERROR_SYNC_EVENT);
	final String subject = getArticleFieldValue(content, "subject");
	final String body = replaceContent(getArticleFieldValue(content, "content"), new String[] { "[$SYNC_ERRORS$]" }, new String[] { errors });

	sendMailMessage(subject, body, getFromAddress(companyId), toAddresses);
    }

    private String getArticleContent(long companyId, String articleId) throws NestableException {
	long globalGroupId = utils.getGlobalGroupId(companyId);
	final JournalArticle article = JournalArticleLocalServiceUtil.getArticle(globalGroupId, articleId);
	if (Validator.isNull(article)) {
	    throw new NestableException("Unable to find Invite Users article. articleId: " + articleId + ", groupId: " + globalGroupId);
	}
	return article.getContent();
    }

    private String replaceContent(String originalString, String[] placeholders, String[] values) {
	return StringUtils.replaceEach(originalString, placeholders, values);
    }

    private String getArticleFieldValue(String xmlToParse, String fieldName) {
	String result = StringPool.BLANK;
	try {
	    final Document document = SAXReaderUtil.read(new StringReader(xmlToParse));
	    final Node node = document.selectSingleNode("/root/dynamic-element[@name='" + fieldName + "']/dynamic-content");
	    if (node != null && node.getText() != null && node.getText().length() > 0) {
		result = node.getText();
	    }
	} catch (final Exception e) {
	    LOG.error("Exception retrieving content from article" + Throwables.getRootCause(e).getMessage(), e);
	}
	return result;
    }

    private InternetAddress[] getInternetAddressesFromStrings(final List<String> emailAddresses) {
	final FluentIterable<InternetAddress> toAddresses = FluentIterable.from(emailAddresses).transform(new Function<String, InternetAddress>() {
	    @Override
	    public InternetAddress apply(final String emailAddress) {
		try {
		    return new InternetAddress(emailAddress);
		} catch (final Exception e) {
		    LOG.error("Invalid email address for userId: " + emailAddress, e);
		    return null;
		}
	    }
	}).filter(Predicates.notNull());

	return toAddresses.toArray(InternetAddress.class);
    }

    private void sendMailMessage(final String mailSubject, final String mailBody, final InternetAddress fromAddress, final InternetAddress[] toAddresses) {
	if (StringUtils.isNotBlank(mailBody) && ArrayUtils.isNotEmpty(toAddresses)) {
	    try {
		final MailMessage message = new MailMessage(fromAddress, mailSubject, mailBody, true);
		message.setTo(toAddresses);
		MailServiceUtil.sendEmail(message);
		if(LOG.isTraceEnabled()){
		    LOG.trace("-------------------------------EventbriteEventPortlet - Sending email-------------------------------");
		    LOG.trace("From : " + fromAddress.getAddress() + " - " + fromAddress.getPersonal());
		    LOG.trace("TO: " + Iterables.toString(Arrays.asList(toAddresses)));
		    LOG.trace("EmailSubject: " + mailSubject);
		    LOG.trace("EmailBody: ");
		    LOG.trace(mailBody);
		    LOG.trace("-------------------------------EventbriteEventPortlet - Email sent----------------------------------");
		}
	    } catch (final Exception e) {
		LOG.error("Exception sending email. ", e);
	    }
	}
    }

    private InternetAddress getFromAddress(long companyId) throws SystemException, UnsupportedEncodingException{
	String fromAddress = PrefsPropsUtil.getString(companyId, PropsKeys.ADMIN_EMAIL_FROM_ADDRESS);
	String fromName = PrefsPropsUtil.getString(companyId, PropsKeys.ADMIN_EMAIL_FROM_NAME);
	return new InternetAddress(fromAddress, fromName);
    }


}
