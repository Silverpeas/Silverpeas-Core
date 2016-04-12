<%@ page import="org.silverpeas.core.admin.domain.synchro.SynchroDomainReport" %>
<%@ page import="org.silverpeas.core.util.logging.Level" %><%--

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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    Domain domObject = (Domain)request.getAttribute("domainObject");

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath(resource.getString("JDP.domainSynchro") + "...");
%>
<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<script language='JavaScript'>
function ValidForm(){
	SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=' + document.domainForm.IdTraceLevel.value, 'SynchroDomainReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
	document.domainForm.submit();
}
</script>
<form name="domainForm" action="domainSynchro" method="POST">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                    <tr>
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("GML.name")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="domainName" size="50" maxlength="20" VALUE="<%=EncodeHelper.javaStringToHtmlString(domObject.getName())%>">
                        </td>
                    </tr>
                    <tr>
                        <td valign="baseline" align=left  class="txtlibform">
                            <%=resource.getString("GML.description")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="domainDescription" size="50" maxlength="20" VALUE="<%=EncodeHelper.javaStringToHtmlString(domObject.getDescription())%>">
                        </td>
                    </tr>
			<tr>
				<td valign="baseline" align=left  class="txtlibform">
                          <%=resource.getString("JDP.traceLevel")%> :
		        </td>
				<td valign="baseline" align=left>
				 <select name="IdTraceLevel" size="1">
					<option value=<%=Level.DEBUG%>>Debug</option>
					<option value=<%=Level.INFO%>>Info</option>
					<option value=<%=Level.WARNING%> selected>Warning</option>
					<option value=<%=Level.ERROR%>>Error</option>
	             </select>
				</td>
			</tr>
    </table>
<%
out.println(board.printAfter());
%>
</form>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:ValidForm()", false));
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>