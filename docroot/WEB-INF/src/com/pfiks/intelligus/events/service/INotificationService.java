package com.pfiks.intelligus.events.service;

import java.util.List;

import javax.mail.internet.InternetAddress;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.theme.ThemeDisplay;
import com.pfiks.intelligus.events.model.event.EventModel;

/**
 * Service to send emails notifications using web content articles as templates
 *
 * @author ilenia
 *
 */
public interface INotificationService {

    /**
     * Sends an email to the specified addresses using articleId
     * 'intelligus-events-eventbrite-invite-users' as template
     *
     * @param emailAddresses
     * @param event
     * @param themeDisplay
     * @throws NestableException
     * @throws Exception
     */
    void sendEventInvite(List<String> emailAddresses, EventModel event, ThemeDisplay themeDisplay) throws NestableException, Exception;

    /**
     * Sends an email to the specified addresses using articleId
     * 'intelligus-events-eventbrite-sync-error' as template
     *
     * @param adminEmails
     * @param errorContent
     * @throws NestableException
     * @throws Exception
     */
    void sendImportErrorNotification(long companyId, InternetAddress[] adminEmails, String errorContent) throws NestableException, Exception;

    /**
     * Sends an email to the specified addresses using articleId
     * 'intelligus-events-eventbrite-message-user' as template, and adding the
     * body text in the content
     *
     * @param receiverEmailAddress
     * @param body
     * @param themeDisplay
     * @throws NestableException
     * @throws Exception
     */
    void sendMessageToUser(String receiverEmailAddress, String body, ThemeDisplay themeDisplay) throws NestableException, Exception;

}
