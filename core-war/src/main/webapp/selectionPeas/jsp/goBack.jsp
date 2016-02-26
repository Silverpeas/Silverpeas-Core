<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
	String url = (String) request.getAttribute("HostUrl");
%>
<HTML>
<HEAD>
<view:looknfeel/>
<script type="text/javascript">
function QuitAndRefresh()
{
    <%
    boolean toPopup = ((Boolean)request.getAttribute("ToPopup")).booleanValue();
    if (toPopup)
    {
    %>
	window.opener.location.href="<%=url%>";
        window.close();
    <%
    }
    else
    {
    %>
	$.progressMessage();
	setTimeout("document.forms['redirection'].submit();", 500);
    <%
    }
    %>
}
</script>
</HEAD>
<BODY onload="javascript:QuitAndRefresh()">
<form name="redirection" action="<%=url %>" method="post">
</form>
<view:progressMessage />
</BODY>
</HTML>