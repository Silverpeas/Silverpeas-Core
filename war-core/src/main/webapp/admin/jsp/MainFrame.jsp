<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%@ include file="usefullFunctions.jsp" %>

<%
String			strGoTo				= request.getParameter("goto");
String			preferedWorkSpace	= "";
String			workSpace			= "";
String			language			= "";
ResourceLocator message				= null;

if (strGoTo != null)
{
            HttpSession Session = request.getSession();
            Session.putValue("goto",strGoTo);
}

MainSessionController	m_MainSessionCtrl	= (MainSessionController) session.getAttribute("SilverSessionController");


if (m_MainSessionCtrl == null)
{
%>
	<script> 
	top.location="../../Login.jsp";
	</script>
<%
}
else
{
	language			= m_MainSessionCtrl.getFavoriteLanguage();
	preferedWorkSpace	= m_MainSessionCtrl.getFavoriteSpace();
	message				= new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", language);
	workSpace 			= "?SpaceId="+preferedWorkSpace;
%>

<html>
<head>
<title><%=message.getString("GML.popupTitle")%></title>
<script language="javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>
<frameset rows="0,83,*,0" cols="*" border="0" framespacing="0" frameborder="NO"> 
  <frame src="../../clipboard/jsp/Idle.jsp" name="IdleFrame" marginwidth="0" marginheight="0" scrolling="NO" noresize frameborder="NO">
  <frame src="TopBar.jsp<%=workSpace%>" name="topFrame" marginwidth="0" marginheight="0" scrolling="NO" noresize frameborder="NO">
  <frame src="frameBottom.jsp<%=workSpace%>" name="bottomFrame" marginwidth="0" marginheight="0" scrolling="NO" noresize frameborder="NO">
  <frame src="javascript.htm" name="scriptFrame" marginwidth="0" marginheight="0" scrolling="NO" noresize frameborder="NO">
</frameset><noframes></noframes>
</html>
<%
}
%>