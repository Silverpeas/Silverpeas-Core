<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ include file="checkVersion.jsp"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="com.stratelia.silverpeas.versioningPeas.multilang.versioning" var="versioningMessages"  />
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><fmt:message key="versioning.warning.checkin.locked.title" bundle="${versioningMessages}" /></title>
</head>
<body>
<fieldset>
  <legend class="txttitrecol"><fmt:message key="versioning.warning.checkin.locked.title" bundle="${versioningMessages}" /></legend>
<%
  ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
  ButtonPane warningButtonPane = gef.getButtonPane();
  warningButtonPane.addButton(gef.getFormButton(messages.getString("versioning.warning.checkin.locked.force"), "javascript:document.addForm.submit();", false));
  warningButtonPane.addButton(gef.getFormButton(messages.getString("close"), "javascript:window.opener.parent.MyMain.location.reload(); window.close();", false));
%>
  <form name="addForm" action="<c:url value="/RVersioningPeas/${param.componentId}/saveOnline" />" method="POST">
    <input type="hidden" name="radio" value="<c:out value="${param.radio}"/>" />
    <input type="hidden" name="action" value="checkin"/>
    <input type="hidden" name="publicationId" value="<c:out value="${param.publicationId}"/>" />
    <input type="hidden" name="componentId" value="<c:out value="${param.componentId}" />" />
    <input type="hidden" name="spaceId" value="<c:out value="${param.spaceId}" />" />
    <input type="hidden" name="documentId" value="<c:out value="${param.documentId}" />" />
    <input type="hidden" name="comments"  value="<c:out value="${param.comments}" />" />
    <input type="hidden" name="force_release" value="true" />
  </form>

<p><fmt:message key="versioning.warning.checkin.locked" bundle="${versioningMessages}" /></p>
<p><center><%=warningButtonPane.print()%></center></p>
</fieldset>
</body>
</html>
