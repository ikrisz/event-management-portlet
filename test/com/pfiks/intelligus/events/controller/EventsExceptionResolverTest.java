package com.pfiks.intelligus.events.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.portlet.ModelAndView;

import com.pfiks.intelligus.events.ClassesConstructorsTest;
import com.pfiks.intelligus.events.constants.ConfigurationConstants;
import com.pfiks.intelligus.events.exception.EventException;
import com.pfiks.intelligus.events.exception.EventPermissionException;
import com.pfiks.intelligus.events.exception.EventbriteException;

public class EventsExceptionResolverTest extends ClassesConstructorsTest {

    private EventsExceptionResolver eventsExceptionResolver;
    private final RenderRequest request = mock(RenderRequest.class);
    private final RenderResponse response = mock(RenderResponse.class);

    @Before
    public void setUp() {
	eventsExceptionResolver = new EventsExceptionResolver();
	eventsExceptionResolver.setUtils(configurationUtils);
    }

    @Test
    public void testThatExceptionsAreManaged() {
	final ModelAndView mav = manageException(aGeneralException());
	assertThat(mav.getViewName(), is("error/error"));
    }

    @Test
    public void testThatPermissionExceptionsAreManagedSerapately() {
	final ModelAndView mav = manageException(aPermissionException());
	assertThat(mav.getViewName(), is("error/error"));
	assertThat((String) mav.getModel().get("articleId"), is(ConfigurationConstants.ERROR_PERMISSION_PAGE_ARTICLE_ID));
    }

    @Test
    public void testThatEventbriteExceptionsAreManagedSerapately() {
	final ModelAndView mav = manageException(anEventbriteException());
	assertThat(mav.getViewName(), is("error/error"));
	assertThat((String) mav.getModel().get("articleId"), is(ConfigurationConstants.ERROR_EVENTBRITE_PAGE_ARTICLE_ID));
    }

    @Test
    public void testThatEventExceptionsAreManagedSerapately() {
	final ModelAndView mav = manageException(anEventException());
	assertThat(mav.getViewName(), is("error/error"));
	assertThat((String) mav.getModel().get("articleId"), is(ConfigurationConstants.ERROR_EXCEPTION_PAGE_ARTICLE_ID));
    }

    private EventbriteException anEventbriteException() {
	return new EventbriteException();
    }

    private EventException anEventException() {
	return new EventException();
    }

    private ModelAndView manageException(final Exception exception) {
	return eventsExceptionResolver.resolveException(request, response, null, exception);
    }

    private EventPermissionException aPermissionException() {
	return new EventPermissionException();
    }

    private Exception aGeneralException() {
	return new Exception();
    }

}
