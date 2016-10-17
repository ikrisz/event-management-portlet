<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<div class="container-fluid">
	<div class="panel panel-default">
		<div class="panel-heading">
			<h3 class="panel-title">
				<liferay-ui:message key="page.title.error"/>
			</h3>
		</div>
		<div class="panel-body">
			<div class="row-fluid">
				<div class="span12">
					<c:if test="${not empty articleId}">
						<liferay-ui:journal-article articleId="${articleId}" groupId="${groupId}"/>
					</c:if>
					
					<c:if test="${not empty errorMessage}">
						<liferay-ui:message key="${errorMessage}"/>
					</c:if>
										
					<c:if test="${not empty exceptionMessage || not empty exceptionStacktrace}">
						<a class="" href="javascript:void(0);" onclick="toggleErrorContentDiv();">
							<liferay-ui:message key="info.view.error.details"/>
						</a>
						<div id="errorContent" class="alert-content hide">
							<p><c:out value="${exceptionMessage}"/></p>
							<p><c:out value="${exceptionStacktrace}"/></p>
						</div>
					</c:if>
				</div>
			</div>
		</div>
		<div class="panel-footer">
			<portlet:renderURL var="backURL" windowState="normal"/>
			<a href="${backURL}" class="btn btn-default">
				<span class="icon-share-alt"></span>&nbsp;
				<liferay-ui:message key="button.back"/>
			</a>
		</div>
	</div>
</div>
