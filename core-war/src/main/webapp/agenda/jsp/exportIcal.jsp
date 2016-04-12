<%@ page import="org.silverpeas.core.util.file.FileServerUtils" %>
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
	boolean exportDone = false;
	String calendarIcsFileName = "";
	String urlFileCalendar = "";

	if (StringUtil.isDefined((String) request.getAttribute("ExportReturnCode")))
	{
		exportDone = true;
		String returnCode = (String) request.getAttribute("ExportReturnCode");
		if (AgendaSessionController.EXPORT_SUCCEEDED.equals(returnCode))
		{
			 statusMessage = resources.getString("agenda.ExportSucceeded");
			 calendarIcsFileName = AgendaSessionController.AGENDA_FILENAME_PREFIX + agenda.getUserId() + ".ics";
       urlFileCalendar = FileServerUtils.getUrlToTempDir(calendarIcsFileName);
		 }
		else
			 statusMessage = resources.getString("agenda.ExportEmpty");
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel withCheckFormScript="true"/>
<view:includePlugin name="datepicker"/>
<script type="text/javascript">
		function exportIcal() {
			if (isCorrectForm()) {
				$.progressMessage();
				document.exportIcalForm.submit();
			}
		}

		function isCorrectForm() {
			  var errorMsg = "";
			  var errorNb = 0;
			  var beginDate = document.exportIcalForm.StartDate.value;
			  var endDate = document.exportIcalForm.EndDate.value;

			  var yearBegin = extractYear(beginDate, '<%=agenda.getLanguage()%>');
			  var monthBegin = extractMonth(beginDate, '<%=agenda.getLanguage()%>');
			  var dayBegin = extractDay(beginDate, '<%=agenda.getLanguage()%>');

			  var yearEnd = extractYear(endDate, '<%=agenda.getLanguage()%>');
			  var monthEnd = extractMonth(endDate, '<%=agenda.getLanguage()%>');
			  var dayEnd = extractDay(endDate, '<%=agenda.getLanguage()%>');

			  var beginDateOK = false;
			  var endDateOK = false;

			  if (!isWhitespace(beginDate)) {
				    if (isCorrectDate(yearBegin, monthBegin, dayBegin)==false) {
					  errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateDebutNote")%>' <%=agenda.getString("MustContainsCorrectDate")%>\n";
			          errorNb++;
			      }
			      else beginDateOK = true;
			  }
			  if (!isWhitespace(endDate)) {
			      if (isCorrectDate(yearEnd, monthEnd, dayEnd)==false) {
				    errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateFinNote")%>' <%=agenda.getString("MustContainsCorrectDate")%>\n";
			          errorNb++;
			      }
			      else endDateOK = true;
			  }

			  if (beginDateOK && endDateOK) {
				    if (isD1AfterD2(yearEnd, monthEnd, dayEnd, yearBegin, monthBegin, dayBegin)==false) {
			          errorMsg+="  - <%=agenda.getString("TheField")%> '<%=agenda.getString("dateFinNote")%>' <%=agenda.getString("MustContainsPostDateToBeginDate")%>\n";
			          errorNb++;
			      }
			  }

			  switch(errorNb)
			  {
			        case 0 :
			            result = true;
			            break;
			        case 1 :
			            errorMsg = "<%=agenda.getString("ThisFormContains")%> 1 <%=agenda.getString("Error")%> : \n" + errorMsg;
			            window.alert(errorMsg);
			            result = false;
			            break;
			        default :
			            errorMsg = "<%=agenda.getString("ThisFormContains")%> " + errorNb + " <%=agenda.getString("Errors")%> :\n" + errorMsg;
			            window.alert(errorMsg);
			            result = false;
			            break;
			  }
			  return result;
		}
</script>
</head>

<body id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(resources.getString("agenda.ExportIcalCalendar"));
	out.println(window.printBefore());
  Frame frame = graphicFactory.getFrame();

  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<form name="exportIcalForm" action="ExportIcal" method="post">
<% if (exportDone) { %>
	<table width="100%" cellpadding="2" cellspacing="2" border="0">
		<tr>
			<td align="center"><span class="txtlibform"><%=statusMessage%></span></td>
	    </tr>
	    <tr>
		<td align="center"><a href="<%=urlFileCalendar%>"><%=calendarIcsFileName%></a></td>
	    </tr>
	 </table>
<% } else { %>
     <table width="100%" cellpadding="2" cellspacing="2" border="0">
		    <tr>
			<td align="left" class="txtlibform" width="150"><%=resources.getString("dateDebutNote")%> :</td>
			<td><input type="text" class="dateToPick" name="StartDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>"/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
				  </td>
			</tr>
			<tr>
			<td align="left" class="txtlibform"><%=resources.getString("dateFinNote")%> :</td>
				<td><input type="text" class="dateToPick" name="EndDate" size="14" maxlength="<%=DBUtil.getDateFieldLength()%>"/><span class="txtnote">(<%=resources.getString("GML.dateFormatExemple")%>)</span>
				  </td>
		    </tr>
	  </table>
	<% } %>
</form>
<%
	out.println(board.printAfter());

	Button button = null;
	if (exportDone) {
		button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false);
	} else {
		button = graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:exportIcal()", false);
	}
	out.print("<br/><center>"+button.print()+"</center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>