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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%@ page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@ page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%
Form 				formUpdate 	= (Form) request.getAttribute("XMLForm");
DataRecord 			data 		= (DataRecord) request.getAttribute("XMLData");
String				xmlFormName = (String) request.getAttribute("XMLFormName");
PagesContext		context		= (PagesContext) request.getAttribute("PagesContext");

context.setBorderPrinted(false);
context.setFormIndex("0");

%>
<HTML>
<HEAD>
<view:looknfeel/>
<% formUpdate.displayScripts(out, context); %>
<script language="javaScript">
function B_VALIDER_ONCLICK()
{
	if (isCorrectForm())
	{
		document.myForm.submit();
	}
}

function B_ANNULER_ONCLICK()
{
	window.close();
}
</script>
</HEAD>
<BODY class="yui-skin-sam">
<%
    Board board = gef.getBoard();
    out.println(board.printBefore());
%>
<FORM NAME="myForm" METHOD="POST" ACTION="Update" ENCTYPE="multipart/form-data">
	<%
		formUpdate.display(out, context, data);
	%>
	<input type="hidden" name="Name" value="<%=xmlFormName%>">
</FORM>
<%
	out.println(board.printAfter());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
    out.println("<br><center>"+buttonPane.print()+"</center>");
%>
</BODY>
<script language="javascript">
	document.myForm.elements[1].focus();
</script>
</HTML>