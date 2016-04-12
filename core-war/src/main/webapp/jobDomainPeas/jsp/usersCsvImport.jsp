<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %><%--

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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.jobDomainPeas.multilang.jobDomainPeasBundle"/>

<%
	Domain 		domObject 		= (Domain)request.getAttribute("domainObject");

    Board board = gef.getBoard();

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath((String)request.getAttribute("groupsPath"));
%>
<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
<script language="JavaScript">

function SubmitWithVerif(verifParams)
{
    var csvFilefld = stripInitialWhitespace(document.csvFileForm.file_upload.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(csvFilefld)) {
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.csvFile")+resource.getString("JDP.missingFieldEnd")); %>";
         } else {
			var ext = csvFilefld.substring(csvFilefld.length - 4);

	    if (ext.toLowerCase() != ".csv") {
			errorMsg = "<% out.print(resource.getString("JDP.errorCsvFile")); %>";
		}
		}
    }
    if (errorMsg == "")
    {
        document.csvFileForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}

</script>
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
<form name="csvFileForm" action="usersCsvImport" method="POST" enctype="multipart/form-data">
    <table cellpadding="5" cellspacing="0" border="0" width="100%">
        <tr>
            <td valign="baseline" align=left  class="txtlibform">
                <%=resource.getString("JDP.csvFile") %> :
            </td>
            <td align=left valign="baseline">
                <input type="file" name="file_upload" size="50" maxlength="50" VALUE="">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5">
            </td>
        </tr>
        <tr id="sendEmailTRid">
            <td class="txtlibform"><fmt:message key="JDP.sendEmail" /></td>
            <td>
                <input type="checkbox" name="sendEmail" id="sendEmailId" value="true" />&nbsp;<fmt:message key="GML.yes" /> <br/>
            </td>
        </tr>
        <tr>
            <td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5">
      : <%=resource.getString("GML.requiredField")%>)</td>
        </tr>
    </table>

</form>
<%
out.println(board.printAfter());
%>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
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