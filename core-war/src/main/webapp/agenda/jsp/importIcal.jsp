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

<%@ include file="checkAgenda.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%

	SettingBundle settings = agenda.getSettings();
	String statusMessage = "";
	boolean importDone = false;
	if (StringUtil.isDefined((String) request.getAttribute("ImportReturnCode")))
	{
		importDone = true;
		%>
		<script type="text/javascript">>
				window.opener.location.href = "<%=agenda.getCurrentViewType()%>";
		</script>
		<%
		String returnCode = (String) request.getAttribute("ImportReturnCode");
		if (AgendaSessionController.IMPORT_SUCCEEDED.equals(returnCode))
		{
			 statusMessage = resources.getString("agenda.ImportSucceeded");
		 }
		else if (AgendaSessionController.IMPORT_FAILED.equals(returnCode))
			 statusMessage = resources.getString("agenda.ImportFailed");
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("agenda.ImportIcalCalendar") %></title>
<view:looknfeel/>
<script type="text/javascript">
function importIcal() {
	if (document.importIcalForm.fileCalendar.value != "") {
		$.progressMessage();
		document.importIcalForm.submit();
	}
}
</script>
</head>
<body id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(resources.getString("agenda.ImportIcalCalendar"));
	out.println(window.printBefore());
  Frame frame = graphicFactory.getFrame();

  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<form name="importIcalForm" action="ImportIcal" method="post" enctype="multipart/form-data">
<% if (importDone) { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="center" colspan="2" class="txtlibform">
							<%=statusMessage%>
						      </td>
						    </tr>
						 </table>
	<% } else { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
			      <tr>
				      <td align="left" colspan="2">
						<span class="txtlibform"><%=resources.getString("agenda.ImportFileCalendar")%></span>
						<br/><br/>
							<input type="file" name="fileCalendar" size="50" value=""/>
							<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" align="bottom" alt="<%=resources.getString("GML.requiredField")%>"/>
				      </td>
				 </tr>
				<tr>
				<td colspan="2" nowrap="nowrap">
						<span class="txtnote">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" alt="<%=resources.getString("GML.requiredField")%>"/>&nbsp;:&nbsp;<%=resources.getString("GML.requiredField")%>) <img src="icons/1px.gif" width="20" height="1" alt=""/></span>
			</td>
			    </tr>
			</table>
	<% } %>
</form>
<%
	out.println(board.printAfter());

	Button button = null;
	if (importDone) {
		button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false);
	} else {
		button = graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:importIcal()", false);
	}

	out.print("<br/><center>"+button.print()+"</center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>