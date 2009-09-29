<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
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