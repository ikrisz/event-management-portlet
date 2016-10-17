<%@ tag isELIgnored="false" body-content="scriptless" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>

<%@ attribute name="fieldName" required="true" type="java.lang.String" %>

<liferay-ui:error key="${fieldName}-required" message="error.${fieldName}.required" />
<liferay-ui:error key="${fieldName}-too-long" message="error.${fieldName}.length" />
<liferay-ui:error key="${fieldName}-invalid" message="error.${fieldName}.invalid" />
<liferay-ui:error key="${fieldName}-onlyFuture" message="error.${fieldName}.onlyFuture" />

