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
	boolean synchroDone = false;
	String urlIcalendar = "";
	String loginIcalendar = "";
	String pwdIcalendar = "";
	String charset = settings.getString("defaultCharset");

	if (StringUtil.isDefined((String) request.getAttribute("UrlIcalendar")))
		urlIcalendar = (String) request.getAttribute("UrlIcalendar");
	if (StringUtil.isDefined((String) request.getAttribute("LoginIcalendar")))
		loginIcalendar = (String) request.getAttribute("LoginIcalendar");
	if (StringUtil.isDefined((String) request.getAttribute("PwdIcalendar")))
		pwdIcalendar = (String) request.getAttribute("PwdIcalendar");
	if (StringUtil.isDefined((String) request.getAttribute("defaultCharset")))
		charset = (String) request.getAttribute("defaultCharset");

	if (StringUtil.isDefined((String) request.getAttribute("SynchroReturnCode")))
	{
		synchroDone = true;
		%>
		<script type="text/javascript">
				window.opener.location.href = "<%=agenda.getCurrentViewType()%>";
		</script>
		<%

		String returnCode = (String) request.getAttribute("SynchroReturnCode");
		if (agenda.SYNCHRO_SUCCEEDED.equals(returnCode))
		{
			 statusMessage = resources.getString("agenda.SynchroSucceeded");
		 }
		else
			 statusMessage = resources.getString("agenda.SynchroFailed");
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("agenda.SynchroIcalCalendar") %></title>
<view:looknfeel/>
<script type="text/javascript">
function synchroIcal() {
	if (document.synchroIcalForm.UrlIcalendar.value.indexOf("http") == 0) {
		$.progressMessage();
		document.synchroIcalForm.submit();
	}
}
</script>
</head>
<body id="agenda">
<%
	Window window = graphicFactory.getWindow();

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setComponentName(agenda.getString("agenda"));
	browseBar.setPath(resources.getString("agenda.SynchroIcalCalendar"));
	out.println(window.printBefore());
  Frame frame = graphicFactory.getFrame();

  out.println(frame.printBefore());
  out.println(board.printBefore());
%>
<form name="synchroIcalForm" action="SynchroIcal" method="post">
<% if (synchroDone) { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="center" colspan="2">
							<span class="txtlibform"><%=statusMessage%></span>
						      </td>
						    </tr>
						 </table>
	<% } else { %>
             <table width="100%" cellpadding="5" cellspacing="2" border="0">
					      <tr>
						      <td align="left">
							<span class="txtlibform"><%=resources.getString("agenda.SynchroRemoteUrl")%></span>
						      </td>
						      <td>
							<input maxlength="200" size="80" type="text" name="UrlIcalendar" value="<%=urlIcalendar%>"/>
								<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" align="top" alt=""/>
						      </td>
						    </tr>
					      <tr>
						      <td align="left">
							<span class="txtlibform"><%=resources.getString("agenda.SynchroRemoteLogin")%></span>
						      </td>
						      <td>
							<input maxlength="50" size="50" type="text" name="LoginIcalendar" value="<%=loginIcalendar%>"/>
						      </td>
						    </tr>
					      <tr>
						      <td align="left">
							<span class="txtlibform"><%=resources.getString("agenda.SynchroRemotePwd")%></span>
						      </td>
						      <td>
							<input maxlength="50" size="50" type="password" name="PwdIcalendar" value="<%=pwdIcalendar%>"/>
						      </td>
						    </tr>
					      <tr>
						      <td align="left">
							<span class="txtlibform"><%=resources.getString("agenda.SynchroCharset")%></span>
						      </td>
						      <td>
							<select name="Charset">
                      <option value="UTF-8">UTF-8</option>
								<option value="ISO-8859-1">ISO-8859-1</option>
								<option value="US-ASCII">US-ASCII</option>
							</select>
						      </td>
						    </tr>
								<tr>
			            <td colspan="2" nowrap="nowrap">
								    <span class="txtlnote">(<img src="<%=settings.getString("mandatoryFieldIcon")%>" width="5" height="5" alt=""/>&nbsp;:&nbsp;<%=resources.getString("GML.requiredField")%>) <img src="icons/1px.gif" width="20" height="1" alt=""/></span>
				</td>
			         </tr>
						 </table>
	<% } %>
	</form>
<%
	out.print(board.printAfter());
	ButtonPane buttonPane = graphicFactory.getButtonPane();
	if (!synchroDone)
	{
		Button buttonValidate = graphicFactory.getFormButton(resources.getString("GML.validate"), "javascript:synchroIcal();", false);
		buttonPane.addButton(buttonValidate);
	}
	Button buttonClose = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close();", false);
	buttonPane.addButton(buttonClose);
	out.print("<br/><center>"+buttonPane.print()+"</center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
<view:progressMessage/>
</body>
</html>