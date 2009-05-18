<%@ include file="check.jsp" %>

<%
   String formName = (String) request.getAttribute("formName");
   String elementId = (String) request.getAttribute("elementId");
   String elementName = (String) request.getAttribute("elementName");
   //String userId = (String) request.getAttribute("userId");
   //String userName = (String) request.getAttribute("userName");
   
   UserDetail 	user 	= (UserDetail) request.getAttribute("user");
   UserDetail[] users 	= (UserDetail[]) request.getAttribute("users");
   
   String userId = "";
   String userName = "";
   if (user != null)
   {
	   userId = user.getId();
	   userName = user.getDisplayedName();
   }
   else if (users != null)
   {
	   for (int u=0; u<users.length; u++)
	   {
		   user = users[u];
		   userId += user.getId()+",";
		   userName += user.getDisplayedName()+"\\n";
	   }
   }
   
%>

<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT language='Javascript'>
function resetOpener()
{
	window.opener.document.forms['<%=formName%>'].elements['<%=elementId%>'].value="<%=userId%>";
	window.opener.document.forms['<%=formName%>'].elements['<%=elementName%>'].value="<%=userName%>";
	window.close();
}
</SCRIPT>
</HEAD>
<BODY onload="javascript:resetOpener();">
</BODY>
</HTML>
