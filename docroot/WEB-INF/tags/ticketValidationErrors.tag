<%@ tag isELIgnored="false" body-content="scriptless" %>
<%@ taglib prefix="liferay-ui" uri="http://liferay.com/tld/ui" %>

<%@ attribute name="fieldKey" required="true" type="java.lang.String" %>
<%@ attribute name="fieldName" required="true" type="java.lang.String" %>

<liferay-ui:error key="${fieldKey}-required" message="error.${fieldName}.required" />
<liferay-ui:error key="${fieldKey}-too-long" message="error.${fieldName}.length" />
<liferay-ui:error key="${fieldKey}-invalid" message="error.${fieldName}.invalid" />

