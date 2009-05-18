<%
  response.setContentType("application/x-java-jnlp-file");
  response.setHeader("Content-Disposition","inline; filename=launch.jnlp");
%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+" codebase="<c:out value="${pageContext.request.scheme}://${pageContext.request.serverName}"/><c:if test="${pageContext.request.serverPort > 0}"><c:out value=":${pageContext.request.serverPort}"/></c:if><c:out value="${pageContext.request.contextPath}/personalizationPeas/webdav" />" href="../jsp/launch.jsp">
    <information>
        <title>OpenOffice Online Editor Installer</title>
        <vendor>Silverpeas</vendor>
        <homepage href="http://www.silverpeas.com"/>
        <description>A simple java desktop application based on Swing Application Framework</description>
        <description kind="short">OpenOffice Online Editor Installer</description>
        <icon href="logo.PNG" kind="default"/>
        <icon href="splash.png" kind="splash"/>
        <offline-allowed/>
    </information>
    <security>
        <all-permissions/>
    </security>
    <resources>
        <j2se href="http://java.sun.com/products/autodl/j2se" version="1.6+" />
        <jar href="WebdavInstaller.jar" main="true" download="eager"/>
        <jar href="lib/appframework-1.0.3.jar" download="eager"/>
        <jar href="lib/swing-worker-1.1.jar" download="eager"/>
    </resources>
    <application-desc main-class="com.silverpeas.webdavinstaller.WebdavInstallerApp">
    </application-desc>
</jnlp>