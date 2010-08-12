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
    	$('#modalDialog').dialog('open');
    	document.redirection.action = url;
    	setTimeout("document.redirection.submit();", 500);
    <%
    }
    %>
}
$(document).ready(function(){
	$("#modalDialog").dialog({
  	  	autoOpen: false,
        modal: true,
        height: 'auto',
        width: 200,
        open: function(event, ui) { 
			$(".ui-dialog-titlebar-close").hide();
			$(".ui-dialog-titlebar").hide();}
        });
  });
</script>
</HEAD>
<BODY onload="javascript:QuitAndRefresh()">
<div id="modalDialog" style="display:none">
	<center><table><tr><td align="center" class="txtnote"><%=resource.getString("selectionPeas.inProgress")%></td></tr><tr><td><br/></td></tr><tr><td align="center"><img src="<%=resource.getIcon("selectionPeas.inProgress")%>" alt=""/></td></tr></table></center>
</div>
<form name="redirection" method="POST">
</form>
</BODY>
</HTML>