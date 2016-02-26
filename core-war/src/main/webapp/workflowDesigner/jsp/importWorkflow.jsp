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
<%@ taglib prefix="designer" uri="/WEB-INF/workflowEditor.tld" %>

<HTML>
<HEAD>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/workflowDesigner/jsp/JavaScript/forms.js"></script>

<script language="javascript">

	function sendData()
	{
		if ( isCorrectlyFilled() )
		{
			document.importWorkflowForm.submit();
		}
	}

	function isCorrectlyFilled()
	{
	var errorMsg = "";
	var errorNb = 0;
	var xmlFile = stripInitialWhitespace(document.importWorkflowForm.xmlFile.value);

	if (xmlFile == "")
	{
		errorMsg+="  - '<%=resource.getString("workflowDesigner.import.filename")%>' <%=resource.getString("GML.MustBeFilled")%>\n";
		errorNb++;
	}

	switch(errorNb)
	{
		case 0 :
		result = true;
		break;
		case 1 :
		errorMsg = "<%=resource.getString("GML.ThisFormContains")%> 1 <%=resource.getString("GML.error").toLowerCase()%> : \n" + errorMsg;
		window.alert(errorMsg);
		result = false;
		break;
		default :
		errorMsg = "<%=resource.getString("GML.ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
		window.alert(errorMsg);
		result = false;
		break;
	}
	return result;
	}

</script>
</HEAD>

<BODY leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
<%
	browseBar.setDomainName(resource.getString("workflowDesigner.toolName") );
	browseBar.setComponentName(resource.getString("workflowDesigner.importWorkflow"), "#" );

    out.println(window.printBefore());
    out.println(frame.printBefore());

    //help
    //
    out.println(boardHelp.printBefore());
    out.println("<table border=\"0\"><tr>");
    out.println("<td valign=\"absmiddle\"><img border=\"0\" src=\""+resource.getIcon("workflowDesigner.info")+"\"></td>");
    out.println("<td>"+resource.getString("workflowDesigner.help.importWorkflow")+"</td>");
    out.println("</tr></table>");
    out.println(boardHelp.printAfter());
    out.println("<br/>");

    out.println(board.printBefore());
%>
	<FORM NAME="importWorkflowForm" METHOD="POST" ACTION="DoImportWorkflow" enctype="multipart/form-data">
		<table cellpadding=5 cellspacing=2 border=0 width="98%" >
			<tr>
				<td class="txtlibform"><%=resource.getString("workflowDesigner.import.filename")%> :</td>
				<td><input type="file" name="xmlFile" size="30"></td>
			</tr>
		</table>
	</FORM>

<%
	out.println(board.printAfter());
%>
<designer:buttonPane cancelAction="Main" />
<%
    out.println(frame.printAfter());
    out.println(window.printAfter());
%>
</BODY>
</HTML>