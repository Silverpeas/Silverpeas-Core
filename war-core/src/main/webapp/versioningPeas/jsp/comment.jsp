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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
String okLabel = messages.getString("ok");
String nokLabel = messages.getString("cancel");
String commentsLabel = messages.getString("comments");//"Comments";
String pleaseFill = messages.getString("pleaseFill");
String mandatoryField = m_context+"/util/icons/mandatoryField.gif";
String action = request.getParameter("action");
%>

<html>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<head>

<script language="Javascript">

function submitComment(action)
{
    var cm = document.all.comment.value;
    if ( cm == null || cm == "" )
    {
        alert("<%=pleaseFill%>");
    }
    else
    {
		    if ( action == "validate" || action == "refuse" )
		    {
		        window.opener.document.forms[0].comment.value = cm;
		        window.opener.document.forms[0].elements['action'].value=action;
		        window.opener.document.forms[0].submit();
						window.close();
		    }
		}
}
</script>
</head>
<body onLoad="document.all.comment.focus();">
<%
Board board = gef.getBoard();
out.println(window.printBefore());
%>
<form>
<P>
<table>
        <tr>
                <td class="txtlibform" valign="top">
                        <%=commentsLabel%> :
                </td>
                <td align=left valign="baseline">
                        <textarea id="comment" rows="5" cols="50" ></textarea>  &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
                </td>
        </tr>
</table>
<%

        ButtonPane buttonPane = gef.getButtonPane();
        buttonPane.addButton(gef.getFormButton(okLabel, "javascript:submitComment('"+action+"');", false));
        buttonPane.addButton(gef.getFormButton(nokLabel, "javascript:window.close();", false));

%>
        <center>
<%
        out.println(buttonPane.print());
%>
        </center>
</form>

<%
        out.println(board.printAfter());
        out.println(window.printAfter());

%>

</body>
</html>