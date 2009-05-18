<%@ include file="checkSilverStatistics.jsp" %>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>

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
<%
out.println(gef.getLookStyleSheet());
%>

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