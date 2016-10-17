package com.pfiks.intelligus.events.constants;

public interface ConfigurationConstants {

    /* Scope of the events to show */
    String SCOPE_GROUP = "scope.group";
    String SCOPE_PUBLIC = "scope.public";
    String CONFIG_SCOPE = "preferences_scope";

    /* Default view mode */
    String VIEW_LIST = "viewMode.list";
    String VIEW_CALENDAR = "viewMode.calendar";
    String CONFIG_VIEW_MODE = "preferences_view";

    /* Max events to show */
    String MAX_EVENTS = "preferences_maxEvents";

    /* Featured events settings */
    String SHOW_FEATURED_EVENTS = "preferences_showFeaturedEvents";
    String MAX_FEATURED_EVENTS = "preferences_maxFeaturedEvents";

    /* Eventbrite settings */
    String EVENTBRITE_ENABLED = "preferences_eventbriteEnabled";
    String EVENTBRITE_APPLICATION_KEY = "preferences_eventbriteApplicationKey";
    String EVENTBRITE_USER_KEY = "preferences_eventbriteUserKey";

    String DISABLE_LIFERAY_CREATION = "preferences_liferayCreationDisabled";

    /* Default configuration values */
    String DEFAULT_CONFIG_SCOPE = SCOPE_GROUP;
    String DEFAULT_CONFIG_VIEW_MODE = VIEW_LIST;
    String DEFAULT_FEATURED_ENABLED = "false";
    String DEFAULT_CONFIG_NUMBER = "10";
    String DEFAULT_LIFERAY_CREATION_DISABLE = "false";

    /* Web content ids */

    /**
     * Web content article id for the error page content for un-handled
     * exceptions
     */
    String ERROR_EXCEPTION_PAGE_ARTICLE_ID = "intelligus-events-internal-error-page";

    /** Web content article id for the error page content for permission errors */
    String ERROR_PERMISSION_PAGE_ARTICLE_ID = "intelligus-events-permission-error-page";

    /** Web content article id for the error page content for permission errors */
    String ERROR_EVENTBRITE_PAGE_ARTICLE_ID = "intelligus-events-eventbrite-error-page";

    /**
     * Web content article id for the error page content for not found events
     * errors
     */
    String ERROR_EVENT_NOT_FOUND_PAGE_ARTICLE_ID = "intelligus-events-notfound-error-page";

    /** Web content article id for the sync of events from eventbrite */
    String ERROR_SYNC_EVENT = "intelligus-events-eventbrite-sync-error";

    /**
     * Web content article id used as email template for inviting users to an
     * eventbrite event
     */
    String INVITE_USERS_ARTICLE_ID = "intelligus-events-eventbrite-invite-users";

    /**
     * Web content article id used as email template for sending a private
     * message to a user an eventbrite event
     */
    String MESSAGE_USER_ARTICLE_ID = "intelligus-events-eventbrite-message-user";

}
