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
// Invalidate the previous session
session.invalidate();

// Get a LoginPasswordAuthentication object
LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();

// list of domains
Hashtable domains = lpAuth.getAllDomains();
ArrayList domainsIds = lpAuth.getDomainsIds();
%>
<html>
<head>
<title>silverpeas - Corporate portal organizer</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<form name="EDform" action="/silverpeas/AuthenticationServlet" method="POST">
<input type=hidden name="do_it" value="decode_input_image">
<input type=hidden name="DEFAULT_ACTION2" value="jf_inscriptions_req">
Login : <input type="text" name="Login" size="9" maxlength="20">
<br>
PassWord : <input type="password" name="Password" size="9" maxlength="20">
<br>
Domain : <select name="DomainId" size="1">
	<%
	if (domains==null ||domains.size()==0)
	{ %>
	<option> --- </option>
	<%  }
	else
	{
		for (int i = 0 ; i < domainsIds.size(); i++)
		{ 
	%>
		<option value="<%=((String)domainsIds.get(i))%>"> <%=domains.get((String)domainsIds.get(i))%></option>
	<%  }
	}
	%>
</select>
<input type="submit" value="Valider">
</form>
</body>
</html>