<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.authentication.*"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%
String sURI = request.getRequestURI();
String sServletPath = request.getServletPath();
String sPathInfo = request.getPathInfo();
if(sPathInfo != null)
    sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
String m_context = ".."+ sURI.substring(0,sURI.lastIndexOf(sServletPath));

String errorCode = request.getParameter("ErrorCode");
String domainId	 = request.getParameter("DomainId");
	
ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
String loginPage = general.getString("loginPage");
		if (loginPage.equals("")){
			loginPage = m_context+"/defaultLogin.jsp";
		}
loginPage += "?DomainId="+domainId+"&ErrorCode="+errorCode+"&logout="+request.getParameter("logout");
		
response.sendRedirect(loginPage);
%>