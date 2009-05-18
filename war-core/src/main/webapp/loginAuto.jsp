<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.kmelia.control.ejb.KmeliaHelper"%>
<%@ page import="com.stratelia.webactiv.kmelia.KmeliaSecurity"%>
<%@ page import="com.stratelia.silverpeas.authentication.LoginPasswordAuthentication"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%
HttpSession 			httpSession		= request.getSession();
//System.out.println("SessionId = "+httpSession.getId());
String 					redirection 	= (String) httpSession.getValue("gotoNew");
//System.out.println("redirection = "+redirection);
ResourceLocator 		mesLook		= new ResourceLocator("com.silverpeas.lookSilverpeasV5.multilang.lookBundle", "fr");

String componentId 	= (String) httpSession.getValue("RedirectToComponentId");
String spaceId 		= (String) httpSession.getValue("RedirectToSpaceId");
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script language="JavaScript1.2">
<!--
    function login()
    {
    	document.EDform.submit();
    }
-->
</script>
<LINK REL="stylesheet" TYPE="text/css" HREF="util/styleSheets/globalSP.css">
<LINK REL="stylesheet" TYPE="text/css" HREF="util/styleSheets/globalSP_SilverpeasV5.css">
<style>
.fondTop {
	background-image: url("admin/jsp/icons/silverpeasV5/fond.jpg");
	background-repeat: repeat-x;
}
</style>
</head>
<body class="fondTop">

<% 
boolean isAnonymousAccessAuthorized = false;
if (redirection == null && componentId == null && spaceId == null)
{
	isAnonymousAccessAuthorized = true;
} 
else
{
	OrganizationController 	organization		= new OrganizationController();
	ResourceLocator 		settings		 	= new ResourceLocator("com.stratelia.webactiv.util.viewGenerator.settings.SilverpeasV5", "");
	String 					guestId 			= settings.getString("guestId");
	
	if (guestId != null)
	{
		if (componentId != null)
		{
			if (organization.isComponentAvailable(componentId, guestId))
			{
				isAnonymousAccessAuthorized = true;
			}
			
			if (isAnonymousAccessAuthorized && redirection != null && componentId.startsWith("kmelia"))
			{
				String objectId = KmeliaHelper.extractObjectIdFromURL(redirection);
				//System.out.println("objectId = "+objectId);
				//System.out.println("objectType = "+KmeliaHelper.extractObjectTypeFromURL(redirection));
				String objectType = KmeliaHelper.extractObjectTypeFromURL(redirection);
				if ("Publication".equals(objectType))
				{
					KmeliaSecurity security = new KmeliaSecurity(organization);
					isAnonymousAccessAuthorized = security.isAccessAuthorized(componentId, guestId, objectId);
				}
			}
		}
		else if (spaceId != null)
		{
			if (organization.isSpaceAvailable(spaceId, guestId))
			{
				isAnonymousAccessAuthorized = true;
			}
		}
	}
}

//System.out.println("loginAuto.jsp : isAnonymousAccessAuthorized = "+isAnonymousAccessAuthorized);
	
if (isAnonymousAccessAuthorized) { %>
	<form name="EDform" action="AuthenticationServlet" method="POST">
		<input type="hidden" name="Login" value="guest">
		<input type="hidden" name="Password" value="guest">
		<input type="hidden" name="DomainId" value="0">
	</form>
	
	<script language="javascript1.2">
		document.EDform.submit();
	</script>
<% } else { 
	//------------------------------------------------------------------
	// domains and domainsIds are used by 'selectDomain.jsp.inc'
	// Get a LoginPasswordAuthentication object
	LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();

	// list of domains
	Hashtable domains = lpAuth.getAllDomains();
	ArrayList domainsIds = lpAuth.getDomainsIds();
	//------------------------------------------------------------------
%>
		<br><br>
		<br><br>
		<br><br>
		<br><br>
		<TABLE ALIGN=CENTER CELLPADDING=2 CELLSPACING=0 BORDER=0 CLASS=viewGeneratorLines>
	  	<tr><td>
	  	<TABLE ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
	  	<tr><td align="center">
	  		<br>
			<table border="0" cellpadding="0" cellspacing="2" align="center">
				<tr><td style="color: #888888;"><%=mesLook.getString("lookSilverpeasV5.anonymousUnauthorized")%></td></tr>
			</table>
			<br>
			<table border="0" cellpadding="0" cellspacing="2" align="center" width="200">
				<form name="EDform" action="AuthenticationServlet" method="POST">
				<tr><td><%=mesLook.getString("lookSilverpeasV5.login")%> : </td><td align="right"><%@ include file="inputLogin.jsp" %></td></tr>
			    <tr><td><%=mesLook.getString("lookSilverpeasV5.password")%> : </td><td align="right"><%@ include file="inputPassword.jsp" %></td></tr>
				<% if (domainsIds.size() == 1) { %>
						<tr><td colspan="2"><input type="hidden" name="DomainId" value="0"></td></tr>
					<% } else { %>
						<tr><td><%=mesLook.getString("lookSilverpeasV5.domain")%> : </td><td><%@ include file="selectDomain.jsp.inc" %></td></tr>
					<% } %>
			    <tr>
			        <td colspan="2" align="right"><input type="button" value="<%=mesLook.getString("lookSilverpeasV5.login")%>" class="moninput" onClick="login();"></td>
			    </tr>
				</form>
			</table>
			<br>
		</td></tr></table>
		</td></tr></table>
<% } %>

</body>
</html>