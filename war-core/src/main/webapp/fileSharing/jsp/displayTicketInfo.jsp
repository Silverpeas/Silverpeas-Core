<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setBundle basename="com.silverpeas.external.filesharing.multilang.fileSharingBundle"/>
<fmt:setBundle basename="com.silverpeas.external.filesharing.settings.fileSharingIcons" var="icons" />
<html>
<head>
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
<style type="text/css">
td { font-family: "Verdana", "Arial", sans-serif; font-size: 10px}
</style>
</head>

<c:set var="attachment" value="${requestScope.attAttachment}" />
<c:set var="document" value="${requestScope.attDocument}" />
<c:set var="documentVersion" value="${requestScope.attDocumentVersion}" />
<c:set var="key" value="${requestScope.Key}" />

<body>
<br/>
<center>
<c:if test="${attachment!=null}">
<table>
	<tr><td><fmt:message key="fileSharing.nameFile" /> :</td><td><c:out value="${attachment.logicalName}"/></td></tr>
	<tr><td><fmt:message key="fileSharing.sizeFile" /> :</td><td><c:out value="${attachment.attachmentFileSize}"/></td></tr>
	<tr><td><fmt:message key="fileSharing.downloadLink" /> :</td><td><a href="<c:url value="/LinkFile/Key/${requestScope.Key}/${attachment.logicalName}" />" ><fmt:message key="fileSharing.downloadLink" /></a></td></tr>
</table>
</c:if>
<c:if test="${document!=null}">
<table>
	<tr><td><fmt:message key="fileSharing.nameFile" /> :</td><td><c:out value="${document.name}"/> v<c:out value="${documentVersion.majorNumber}"/>.<c:out value="${documentVersion.minorNumber}"/> (<c:out value="${documentVersion.logicalName}"/>)</td></tr>
	<tr><td><fmt:message key="fileSharing.sizeFile" /> :</td><td><c:out value="${documentVersion.displaySize}"/></td></tr>
	<tr><td><fmt:message key="fileSharing.downloadLink" /> :</td><td><a href="<c:url value="/LinkFile/Key/${requestScope.Key}/${documentVersion.logicalName}" />" ><fmt:message key="fileSharing.downloadLink" /></a></td></tr>
</table>
</c:if>
</center>
</body>
</html>