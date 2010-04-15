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