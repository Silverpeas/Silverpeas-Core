<%@ page isELIgnored="false"%>
<%  response.setContentType("application/x-java-jnlp-file");
  response.setHeader("Content-Disposition","inline; filename=launch.jnlp");
%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
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

<c:set var="login"><%=java.net.URLEncoder.encode(login)%></c:set>
<c:set var="encPassword"><%=java.net.URLEncoder.encode(encPassword)%></c:set> 

<c:set var="baseUrl"><c:out value="${pageScope.httpServerBase}"/></c:set>
<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+" codebase="<c:out value="${baseUrl}${pageContext.request.contextPath}/attachment/webdav" />"
    href="<c:out value="${baseUrl}${pageContext.request.contextPath}/attachment/jsp/launch.jsp?${pageContext.request.queryString}&terminal=TERM&login=${login}&encPassword=${encPassword}"/>" >
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
        <jar href="OpenOfficeLauncher.jar" main="true" download="eager"/>
		<jar href="commons-codec-1.3.jar" main="false" download="eager"/>
        <jar href="commons-httpclient.jar" main="false" download="eager"/>
        <jar href="commons-logging-1.0.4.jar" main="false" download="eager"/>
        <jar href="jackrabbit-webdav-1.4.jar" main="false" download="eager"/>
        <jar href="slf4j-log4j12-1.5.0.jar" main="false" download="eager"/>
        <jar href="slf4j-api-1.5.0.jar" main="false" download="eager"/>
        <jar href="log4j-1.2.15.jar" main="false" download="eager"/>
    </resources>
    <application-desc main-class="com.silverpeas.openoffice.Launcher">
      <argument><%=java.net.URLEncoder.encode(request.getParameter("documentUrl"), "UTF-8") %></argument>
	  <argument><%=java.net.URLEncoder.encode(login, "UTF-8") %></argument>
      <argument><%=java.net.URLEncoder.encode(encPassword, "UTF-8") %></argument>
    </application-desc>
</jnlp>