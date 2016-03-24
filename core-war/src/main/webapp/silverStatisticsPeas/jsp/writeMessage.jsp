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
<%@ include file="checkSilverStatistics.jsp" %>
<%@ page import="org.silverpeas.core.admin.user.model.UserDetail"%>

<%
    String action = (String)request.getAttribute("action");
    if ("Close".equals(action))
    {
%>
<HTML>
<BODY onload="javascript:window.close();">
</BODY>
</HTML>
<%
    }
    else
    {
%>

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<script language="JavaScript">
function validateUsers() {
	document.EDform.submit();
}
</script>
</HEAD>
<%
	UserDetail userDetail = null;
	if (action.equals("NotifyUser"))
		userDetail = (UserDetail)request.getAttribute("userDetail");

	String formAction = "";
	if (action.equals("NotifyUser"))
		formAction = "ToAlert?theUserId="+userDetail.getId();
	else
		formAction = "ToAlertAllUsers";
%>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>

<%
	out.println(window.printBefore());
	out.println(frame.printBefore());
	out.println(board.printBefore());

	//button
	Button cancelButton = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=window.close();", false);
	Button validateButton = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=validateUsers();", false);
%>

<CENTER>
	<table border="0" cellspacing="0" cellpadding="0" width="100%">
		<FORM name="EDform" Action="<%=formAction%>" METHOD="POST">
		<TR>
			<TD align="center" class="txttitrecol">
				<%=resources.getString("GML.users")%>
			</TD>
		</TR>
        <TR>
            <TD align="center">
            <%if (action.equals("NotifyUser")) {%>
				<%=userDetail.getFirstName() + " " + userDetail.getLastName()%>
			<%} else {%>
				<%=resources.getString("silverStatisticsPeas.usersWithSession")%>
			<%}%>
            </TD>
        </TR>
		<TR>
			<TD align="center" class="txtlibform">
				<br><b><%=resources.getString("silverStatisticsPeas.AuthorMessage")%></b> : <BR><textarea cols="100" rows="6" name="messageAux"></textarea>
			</TD>
		</TR>
		</FORM>
	</TABLE>
</CENTER>
<%
	out.println(board.printAfter());

	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	buttonPane.setHorizontalPosition();
	out.println("<BR><center>"+buttonPane.print()+"</center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</BODY>
</HTML>
<%
    }
%>