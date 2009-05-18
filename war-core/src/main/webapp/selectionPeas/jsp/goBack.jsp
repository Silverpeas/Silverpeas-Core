<%@ include file="check.jsp" %>

<HTML>
<HEAD>
<%
out.println(gef.getLookStyleSheet());
%>
<link type="text/css" rel="stylesheet" href="<%=m_context%>/util/styleSheets/modal-message.css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script> 

<script language='Javascript'>
function QuitAndRefresh()
{
	url = "<%=(String) request.getAttribute("HostUrl")%>";
	
    <%
    boolean toPopup = ((Boolean)request.getAttribute("ToPopup")).booleanValue();
    if (toPopup)
    {
    %>
    	window.opener.location.href=url;
        window.close();
    <%
    }
    else
    {
    %>
    	displayStaticMessage();
    	document.redirection.action = url;
    	setTimeout("document.redirection.submit();", 500);
    <%
    }
    %>
}
</script>
</HEAD>
<BODY onload="javascript:QuitAndRefresh()">

<script type="text/javascript">
messageObj = new DHTML_modalMessage();	// We only create one object of this class
messageObj.setShadowOffset(5);	// Large shadow

<!-- avec le script modal-message.js -->
function displayStaticMessage()
{
	messageObj.setHtmlContent("<center><table><tr><td align=\"center\" class=\"txtnote\"><%=resource.getString("selectionPeas.inProgress")%></td></tr><tr><td><br/></td></tr><tr><td align=\"center\"><img src=\"<%=resource.getIcon("selectionPeas.inProgress")%>\"/></td></tr></table></center>");
	messageObj.setSize(300,120);
	messageObj.setCssClassMessageBox(false);
	messageObj.setShadowDivVisible(true);	// Disable shadow for these boxes	
	messageObj.display();
}

function closeMessage()
{
	messageObj.close();	
}
</script>
<form name="redirection" method="POST">
</form>
</BODY>
</HTML>