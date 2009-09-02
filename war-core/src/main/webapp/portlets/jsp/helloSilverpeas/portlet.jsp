<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle"/>

<fmt:message key="portlets.portlet.helloSilverpeas.welcome"/>
