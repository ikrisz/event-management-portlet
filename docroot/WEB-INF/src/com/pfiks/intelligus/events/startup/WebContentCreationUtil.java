package com.pfiks.intelligus.events.startup;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.liferay.portal.kernel.exception.NestableException;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.pfiks.intelligus.portal.service.JournalArticleCreationLocalServiceUtil;

/**
 * Utility class to create web content articles and web content velocity
 * templates
 *
 * @author Ilenia Zedda
 *
 */
class WebContentCreationUtil {

    private static long companyId;
    private static long groupId;
    private static long userId;

    WebContentCreationUtil(final long currentCompanyId) throws NestableException {
	companyId = currentCompanyId;
	final Company company = CompanyLocalServiceUtil.getCompanyById(companyId);
	groupId = company.getGroup().getGroupId();
	userId = company.getDefaultUser().getUserId();
    }

    /**
     * Creates a new web content article in global group. No Structure and no
     * template set. If an existing web content article is found with the same
     * urlTitle, the creation is skipped
     *
     * @param articleId
     *            will be assigned as both articleId and urlTitle
     * @throws NestableException
     */
    public void createMissingWebContentArticle(final String articleId) throws NestableException {
	final String articleTitle = StringUtils.replaceChars(articleId, "-", " ");
	final String articleTitleUrl = articleId;
	final String articleXmlContent = getContentFromFile(articleId, ".xml");
	JournalArticleCreationLocalServiceUtil.getOrCreateJournalArticleWithSpecificArticleId(articleId, articleTitle, articleTitleUrl, articleXmlContent, userId, groupId,
		companyId);
    }

    private String getContentFromFile(final String fileName, final String extension) throws NestableException {
	try {
	    final ClassLoader classLoader = getClass().getClassLoader();
	    final InputStream resourceAsStream = classLoader.getResourceAsStream("/resources/" + fileName + extension);
	    final byte[] bytes = FileUtil.getBytes(resourceAsStream);
	    return new String(bytes);
	} catch (final IOException e) {
	    throw new NestableException("Exception retrieving content for file : " + fileName, e);
	}
    }

}
