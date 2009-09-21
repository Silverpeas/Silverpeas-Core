<%@ include file="check.jsp" %>

<%
   String formName = (String) request.getAttribute("formName");
   String elementId = (String) request.getAttribute("elementId");
   String elementName = (String) request.getAttribute("elementName");
   
   UserDetail[] users 	= (UserDetail[]) request.getAttribute("users");
   
   String userId = "";
   String userName = "";
   UserDetail user = null;

   if (users != null)
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
