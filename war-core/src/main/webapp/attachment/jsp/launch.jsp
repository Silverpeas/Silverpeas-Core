<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%  response.setContentType("application/x-java-jnlp-file");
  response.setHeader("Content-Disposition","inline; filename=launch.jnlp");
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="java.security.Key"%>
<%@ page import="javax.crypto.Cipher"%>
<%@ page import="javax.crypto.spec.IvParameterSpec"%>
<%@ page import="javax.crypto.spec.SecretKeySpec"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>

<%! 
	private final static byte[] KEY = new byte[] { -23, -75, -2, -17, 79, -94, -125, -14 };
	private final static String DIGITS = "0123456789abcdef";

	/**
	 * Return length many bytes of the passed in byte array as a hex string.
	 * 
	 * @param data the bytes to be converted.
	 * @return a hex representation of length bytes of data.
	 */
	String toHex(byte[] data)
	{
	    StringBuffer  buf = new StringBuffer();
	    
	    for (int i = 0; i != data.length; i++)
	    {
	        int v = data[i] & 0xff;
	        
	        buf.append(DIGITS.charAt(v >> 4));
	        buf.append(DIGITS.charAt(v & 0xf));
	    }
	    
	    return buf.toString();
	}
%>

<%
	String sURI = request.getRequestURI();
	String sRequestURL = request.getRequestURL().toString();
	String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());

	String httpServerBase = GeneralPropertiesManager.getGeneralResourceLocator().getString("httpServerBase", m_sAbsolute);
	pageContext.setAttribute("httpServerBase", httpServerBase);
	
	String terminal = request.getParameter("terminal");
	String login = null;
	String encPassword = null;
	
	/*
	 * First time, called by silverpeas : user information present into HTTP session
	 */
	if (terminal == null) {
		MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
		login = m_MainSessionCtrl.getCurrentUserDetail().getLogin();
		String password = (String) session.getAttribute("Silverpeas_pwdForHyperlink");
		
	    Cipher cipher = Cipher.getInstance("DES");
	    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "DES"));
	    byte[] cipherText = cipher.doFinal(password.getBytes("UTF-8"));
	    
	    encPassword = toHex(cipherText);
	}

	/*
	 * Second time, called by javaws : user information present into HTTP request as parameter
	 */
	else {
		login = request.getParameter("login");	
		encPassword = request.getParameter("encPassword");	
	}
%>

<c:set var="login"><%=java.net.URLEncoder.encode(login, "UTF-8")%></c:set>
<c:set var="encPassword"><%=java.net.URLEncoder.encode(encPassword, "UTF-8")%></c:set>
<fmt:setLocale value="${userLanguage}" />
<view:setBundle basename="com.stratelia.webactiv.util.attachment.Attachment" var="attachmentConfig" />
<fmt:message key="ms.office.installation.path" bundle="${attachmentConfig}" var="msoffice_path" scope="request"/>


<c:set var="baseUrl"><c:out value="${pageScope.httpServerBase}"/></c:set>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+" codebase="<c:out value="${baseUrl}${pageContext.request.contextPath}/attachment/webdav" />" >
    <information>
        <title>Edition WebDAV</title>
        <vendor>Silverpeas</vendor>
        <homepage href="http://www.silverpeas.com"/>
        <description>A simple java webstart application to launch OpenOffice Online edition</description>
        <description kind="short">OpenOffice Online Editor Launcher</description>
        <icon href="logo.PNG" kind="default"/>
        <icon href="logo.PNG" kind="splash"/>
        <offline-allowed/>
    </information>
    <security>
        <all-permissions/>
    </security>
	<update check="timeout" policy="always"/>
    <resources>
        <j2se href="http://java.sun.com/products/autodl/j2se" version="1.6+" />
        <jar href="OpenOfficeLauncher.jar" download="eager"/>
        <jar href="xercesImpl-2.6.2.jar" download="eager"/>
        <jar href="commons-codec-1.3.jar" download="eager"/>
        <jar href="commons-httpclient-3.0.1.jar" download="eager"/>
        <jar href="jackrabbit-jcr-commons-1.6.0.jar" download="eager"/>
        <jar href="jackrabbit-webdav-1.6.0.jar" download="eager"/>
        <jar href="jcl-over-slf4j-1.5.6.jar" download="eager"/>
        <jar href="slf4j-log4j12-1.5.6.jar" download="eager"/>
        <jar href="slf4j-api-1.5.6.jar" download="eager"/>
        <jar href="log4j-1.2.15.jar" download="eager"/>
    </resources>
    <application-desc main-class="com.silverpeas.openoffice.Launcher">
      <argument><%=java.net.URLEncoder.encode(request.getParameter("documentUrl"), "UTF-8") %></argument>
      <argument><%=java.net.URLEncoder.encode((String)request.getAttribute("msoffice_path"), "UTF-8") %></argument>
      <argument><%=java.net.URLEncoder.encode(login, "UTF-8") %></argument>
      <argument><%=java.net.URLEncoder.encode(encPassword, "UTF-8") %></argument>
    </application-desc>
</jnlp>