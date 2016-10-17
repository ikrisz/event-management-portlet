package com.pfiks.intelligus.events.controller;

import javax.annotation.Resource;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.portlet.HandlerExceptionResolver;
import org.springframework.web.portlet.ModelAndView;

import com.google.common.base.Throwables;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventNotFoundException;
import com.pfiks.intelligus.events.exception.EventPermissionException;
import com.pfiks.intelligus.events.exception.EventbriteErrorException;
import com.pfiks.intelligus.events.exception.EventbriteException;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;

/**
 * This class intercepts all the exceptions. Redirects to custom error page
 *
 * @author Ilenia Zedda
 *
 */
public class EventsExceptionResolver implements HandlerExceptionResolver {

    private static final Log LOG = LogFactoryUtil.getLog(EventsExceptionResolver.class);

    @Resource
    private ConfigurationUtils utils;

    public void setUtils(final ConfigurationUtils utils) {
	this.utils = utils;
    }

    @Override
    public ModelAndView resolveException(final RenderRequest request, final RenderResponse response, final Object arg2, final Exception exception) {
	return manageException(utils.getCompanyId(request), exception);
    }

    @Override
    public ModelAndView resolveException(final ResourceRequest request, final ResourceResponse response, final Object arg2, final Exception exception) {
	return manageException(utils.getCompanyId(request), exception);
    }

    private ModelAndView manageException(long companyId, final Exception exception) {
	String articleId = StringPool.BLANK;
	String errorMessage = StringPool.BLANK;
	boolean includeException = false;
	if (isEventbriteError(exception)) {
	    errorMessage = exception.getMessage();
	} else if (isPermissionException(exception)) {
	    articleId = ConfigurationConstants.ERROR_PERMISSION_PAGE_ARTICLE_ID;
	} else if (isEventNotFoundException(exception)) {
	    articleId = ConfigurationConstants.ERROR_EVENT_NOT_FOUND_PAGE_ARTICLE_ID;
	} else if (isEventbriteException(exception)) {
	    includeException = true;
	    articleId = ConfigurationConstants.ERROR_EVENTBRITE_PAGE_ARTICLE_ID;
	} else {
	    includeException = true;
	    articleId = ConfigurationConstants.ERROR_EXCEPTION_PAGE_ARTICLE_ID;
	}
	LOG.error("Exception occurred. Redirecting to page: " + articleId);
	return getErrorPage(companyId, articleId, errorMessage, includeException, exception);
    }

    private boolean isEventbriteError(final Exception exception) {
	return exception instanceof EventbriteErrorException;
    }

    private boolean isEventNotFoundException(final Exception exception) {
	return exception instanceof EventNotFoundException;
    }

    private boolean isEventbriteException(final Exception exception) {
	return exception instanceof EventbriteException;
    }

    private boolean isPermissionException(final Exception exception) {
	return exception instanceof EventPermissionException;
    }

    private ModelAndView getErrorPage(final long companyId, final String articleId, final String errorMessage, final boolean includeException, final Exception exception) {
	final ModelAndView res = new ModelAndView();
	res.setViewName("error/error");
	res.addObject("errorMessage", errorMessage);
	if (StringUtils.isNotBlank(articleId)) {
	    res.addObject("groupId", getGroupId(companyId));
	    res.addObject("articleId", articleId);
	}
	if (includeException) {
	    res.addObject("exceptionMessage", Throwables.getRootCause(exception).getMessage());
	    res.addObject("exceptionStacktrace", Throwables.getStackTraceAsString(exception));
	    LOG.warn("Exception: ", Throwables.getRootCause(exception));
	}
	return res;
    }

    private long getGroupId(long companyId) {
	long groupId = 0;
	try {
	    groupId = utils.getGlobalGroupId(companyId);
	} catch (NestableException e) {
	    //
	}
	return groupId;
    }

}
