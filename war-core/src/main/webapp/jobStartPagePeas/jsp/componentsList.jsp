<%@ include file="check.jsp" %>
<%

String m_SpaceName = (String) request.getAttribute("currentSpaceName");
WAComponent[] m_ListComponents = (WAComponent[]) request.getAttribute("ListComponents");
String m_SubSpace = (String) request.getAttribute("nameSubSpace");

 	browseBar.setDomainName(resource.getString("JSPP.manageHomePage"));
 	if (m_SubSpace == null) //je suis sur un espace
 		browseBar.setComponentName(m_SpaceName);
 	else {
 		browseBar.setComponentName(m_SpaceName + " > " + m_SubSpace);
 	}
	browseBar.setExtraInformation(resource.getString("JSPP.creationInstance"));
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/overlib.js"></script>
<script language="JavaScript">
function B_ANNULER_ONCLICK() {
	window.close();
}
</script>
</HEAD>

<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 onLoad="javascript:window.resizeTo(750,700)">
<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<br>
	<TABLE width="70%" align="center" border=0 cellPadding=0 cellSpacing=0>
		<%
        String currentSuite = "";
		for(int nI=0; m_ListComponents!= null && nI < m_ListComponents.length; nI++)
		{
            if (m_ListComponents[nI].isVisible())
            {
                if ((!currentSuite.equalsIgnoreCase(m_ListComponents[nI].getSuite())) && (m_ListComponents[nI].getSuite() != null))
                {
                    currentSuite = m_ListComponents[nI].getSuite();
                    %>
		<TR>
			<TD colspan="2" align="center" class="txttitrecol">&nbsp;</TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"></TD>
		</TR>
		<TR>
			<TD align="center" class="txttitrecol">
				&nbsp;
			</TD>
			<TD align="center" class="txttitrecol">
				 <%=currentSuite.substring(3)%> 
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"></TD>
		</TR>
		<TR>
			<TD colspan="2" align="center" height="2"><img src="<%=resource.getIcon("JSPP.px")%>"></TD>
		</TR>
        <%
                }
		%>
		<TR>
			<TD align="center" width="30">
				<a href="CreateInstance?ComponentNum=<%=nI%>" onmouseover="return overlib('<%=Encode.javaStringToJsString(m_ListComponents[nI].getDescription())%>', CAPTION, '<%=Encode.javaStringToJsString(m_ListComponents[nI].getLabel())%>');" onmouseout="return nd();"><IMG SRC="<%=iconsPath%>/util/icons/component/<%=m_ListComponents[nI].getName()%>Small.gif" border="0"></a>
			</TD>
			<TD align="left">
				<a href="CreateInstance?ComponentNum=<%=nI%>" onmouseover="return overlib('<%=Encode.javaStringToJsString(m_ListComponents[nI].getDescription())%>', CAPTION, '<%=Encode.javaStringToJsString(m_ListComponents[nI].getLabel())%>');" onmouseout="return nd();"><%=m_ListComponents[nI].getLabel()%></a>
			</TD>
		</TR>
		<%
            }
		}
		%>
	</TABLE>

<%
out.println(board.printAfter());
%>
<br><br>
<%
		  ButtonPane buttonPane = gef.getButtonPane();
		  buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
		  out.println(buttonPane.print());

%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>