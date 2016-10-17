package com.pfiks.intelligus.events;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.mockito.Mockito;

import com.liferay.portal.kernel.bean.BeanLocator;
import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.configuration.ConfigurationFactory;
import com.liferay.portal.kernel.configuration.ConfigurationFactoryUtil;
import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.PortletClassLoaderUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ClassNameLocalService;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portal.service.CompanyLocalService;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.Portal;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.calendar.model.CalEvent;
import com.liferay.util.portlet.PortletProps;
import com.pfiks.intelligus.events.utils.ConfigurationUtils;
import com.pfiks.intelligus.search.service.SearchLocalService;

public class ClassesConstructorsTest {

    private static Company mockCompany;
    private static Group mockGroup;
    private static User mockUser;
    protected static ThemeDisplay themeDisplay;
    protected static UserLocalService userLocalService;
    protected static ConfigurationUtils configurationUtils;

    protected static String defaultEventbriteUserKey = "eventbriteUserKey";

    @BeforeClass
    public static void beforeClass() throws NestableException {
	mockBeanLocator();

	mockClassNameLocalService();
	mockCompanyLocalService();
	mockSearchLocalService();
	mockUserLocalService();

	mockConfiguration();
	mockPortalUtil();

	mockPortletProps();

	mockGetDefaultCompany();
	mockGetDefaultGroup();
	mockGetDefaultUser();
	mockGetThemeDisplayValues();

	mockConfigurationUtils();
    }

    private static void mockConfigurationUtils() {
	configurationUtils = new ConfigurationUtils();
    }

    private static void mockGetThemeDisplayValues() {
	themeDisplay = mock(ThemeDisplay.class);
	when(themeDisplay.getUserId()).thenReturn(0L);
    }

    private static void mockGetDefaultGroup() throws PortalException, SystemException {
	mockGroup = Mockito.mock(Group.class);
	when(mockCompany.getGroup()).thenReturn(mockGroup);
	when(mockGroup.getGroupId()).thenReturn(0L);
    }

    private static void mockGetDefaultUser() throws PortalException, SystemException {
	when(mockCompany.getDefaultUser()).thenReturn(mockUser);
	when(mockUser.getUserId()).thenReturn(0L);
    }

    private static void mockGetDefaultCompany() throws PortalException, SystemException {
	when(PortalUtil.getDefaultCompanyId()).thenReturn(0L);
	mockCompany = Mockito.mock(Company.class);
	when(CompanyLocalServiceUtil.getCompanyById(0L)).thenReturn(mockCompany);
    }

    private static void mockBeanLocator() {
	final BeanLocator mockBeanLocator = Mockito.mock(BeanLocator.class);
	PortalBeanLocatorUtil.setBeanLocator(mockBeanLocator);
    }

    private static void mockPortalUtil() {
	final Portal mockPortal = Mockito.mock(Portal.class);
	final PortalUtil pu = new PortalUtil();
	pu.setPortal(mockPortal);
	when(PortalBeanLocatorUtil.locate(PortalUtil.class.getName())).thenReturn(pu);
    }

    private static void mockConfiguration() {
	final ConfigurationFactory mockConfigurationFactory = Mockito.mock(ConfigurationFactory.class);
	ConfigurationFactoryUtil.setConfigurationFactory(mockConfigurationFactory);
	final Configuration mockConfiguration = Mockito.mock(Configuration.class);
	when(ConfigurationFactoryUtil.getConfiguration(PortletClassLoaderUtil.getClassLoader(), "portlet")).thenReturn(mockConfiguration);
    }

    private static void mockPortletProps() {
	when(PortletProps.get("eventbrite.enabled")).thenReturn("true");
	when(PortletProps.get("eventbrite.appkey")).thenReturn("");
	when(PortletProps.get("eventbrite.api.userkey")).thenReturn(defaultEventbriteUserKey);
    }

    private static void mockUserLocalService() throws PortalException, SystemException {
	mockUser = Mockito.mock(User.class);
	userLocalService = Mockito.mock(UserLocalService.class);
	when(PortalBeanLocatorUtil.locate(UserLocalService.class.getName())).thenReturn(userLocalService);
	when(UserLocalServiceUtil.getUser(0L)).thenReturn(mockUser);
    }

    private static void mockCompanyLocalService() {
	final CompanyLocalService mockCompanyLocalService = Mockito.mock(CompanyLocalService.class);
	when(PortalBeanLocatorUtil.locate(CompanyLocalService.class.getName())).thenReturn(mockCompanyLocalService);
    }

    private static void mockClassNameLocalService() {
	final ClassNameLocalService mockClassNameLocalService = Mockito.mock(ClassNameLocalService.class);
	when(PortalBeanLocatorUtil.locate(ClassNameLocalService.class.getName())).thenReturn(mockClassNameLocalService);

	when(ClassNameLocalServiceUtil.getClassNameId(CalEvent.class)).thenReturn(0L);
    }

    private static void mockSearchLocalService() {
	final SearchLocalService mockSearchLocalService = Mockito.mock(SearchLocalService.class);
	when(PortalBeanLocatorUtil.locate(SearchLocalService.class.getName())).thenReturn(mockSearchLocalService);
    }

}
