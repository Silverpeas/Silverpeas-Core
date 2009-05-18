<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Group"%>

<%@ include file="check.jsp" %>


<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
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
	String componentURL = (String)request.getAttribute("myComponentURL");
	UserDetail[] userDetails = (UserDetail[])request.getAttribute("UserR");
	Group[] groups = (Group[])request.getAttribute("GroupR");
	PairObject hostComponentNameObject = (PairObject) request.getAttribute("HostComponentName");
	String hostSpaceName = (String) request.getAttribute("HostSpaceName");
	String hostComponentName = (String) hostComponentNameObject.getFirst(); 
%>

<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>

<%
	browseBar.setDomainName(hostSpaceName);
	browseBar.setComponentName(hostComponentName);

	out.println(window.printBefore());
	out.println(frame.printBefore());

	//button
	Button cancelButton = (Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=window.close();", false);
	Button validateButton = (Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=validateUsers();", false);


	//Icons
	String noColorPix = resource.getIcon("alertUserPeas.px");
%>

			
<FORM name="EDform" Action="<%=componentURL%>ToAlert" METHOD="POST">
<CENTER>
<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
	<tr> 
		<td nowrap>
			<table border="0" cellspacing="0" cellpadding="0" class="contourintfdcolor" width="100%"><!--tabl1-->
				<%
				if (userDetails.length > 0)
				{
				%>
				<TR>
					<TD align="center" class="txttitrecol" colspan="2">
						<%=resource.getString("GML.users")%>
					</TD>
				</TR>
				<TR>
					<TD colspan="2" align="center" class="intfdcolor" height="1" width="70%"><img src="<%=noColorPix%>"></TD>
				</TR>
				
				<%
				for(int i=0; i < userDetails.length; i++)
				{
					UserDetail userDetail = userDetails[i];
					String actorName = userDetail.getFirstName() + " " + userDetail.getLastName();
					%>
					<TR>
						<TD align="center" colspan="2">
							<%=actorName%>
						</TD>
					</TR>
					<%
				}
				%>
				<TR width="70%">
					<TD colspan="2" align="center" class="intfdcolor"  height="1" width="70%"><img src="<%=noColorPix%>"></TD>
				</TR>
				<%
				}
				%>
				<%
				if (groups.length > 0)
				{
				%>
				<TR>
					<TD align="center" class="txttitrecol" colspan="2">
						<%=resource.getString("GML.groupes")%>
					</TD>
				</TR>
				<TR>
					<TD colspan="2" align="center" class="intfdcolor" height="1" width="70%"><img src="<%=noColorPix%>"></TD>
				</TR>

				<%
				for(int i=0; i < groups.length; i++)
				{
					Group group = groups[i];
					String groupName = group.getName();
					%>
					<TR>
						<TD align="center" colspan="2">
							<%=groupName%>
						</TD>
					</TR>
					<%
				}
				%>
				<TR width="70%">
					<TD colspan="2" align="center" class="intfdcolor"  height="1" width="70%"><img src="<%=noColorPix%>"></TD>
				</TR>
				<%
				}
				%>
				<TR>
					<TD colspan="2" align="center" class="txtlibform">
						<b><%=resource.getString("AuthorMessage")%></b> : <BR><textarea cols="80" rows="8" name="messageAux"></textarea>
					</TD>
				</TR>
			</TABLE>
		</td>
	</tr>
</table>
</CENTER>
</FORM>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(validateButton);
	buttonPane.addButton(cancelButton);
	buttonPane.setHorizontalPosition();
	out.println("<BR><center>"+buttonPane.print()+"<br></center>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>




</BODY>
</HTML>