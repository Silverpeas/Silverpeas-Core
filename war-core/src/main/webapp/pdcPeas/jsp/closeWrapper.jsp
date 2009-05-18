<%@ include file="checkPdc.jsp" %>

<%
   String formName = (String) request.getAttribute("formName");
   String elementId = (String) request.getAttribute("elementId");
   String elementName = (String) request.getAttribute("elementName");
   String userIds = (String) request.getAttribute("userIds");
   String userNames = (String) request.getAttribute("userNames");
%>

<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT language='Javascript'>
function resetOpener()
{
	window.opener.document.forms['<%=formName%>'].elements['<%=elementId%>'].value="<%=userIds%>";
	window.opener.document.forms['<%=formName%>'].elements['<%=elementName%>'].value="<%=userNames%>";
	window.close();
}
</SCRIPT>
</HEAD>
<BODY onload="javascript:resetOpener();">
</BODY>
</HTML>
