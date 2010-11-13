<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%

WAComponent[] m_ListComponents = (WAComponent[]) request.getAttribute("ListComponents");
String 		spaceId				= (String) request.getAttribute("CurrentSpaceId");

browseBar.setSpaceId(spaceId);
browseBar.setClickable(false);
browseBar.setExtraInformation(resource.getString("JSPP.creationInstance"));
%>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript">
$(document).ready(function() 
{
   // By suppling no content attribute, the library uses each elements title attribute by default
   $('a[title]').qtip({
      content: {
         text: false // Use each elements title attribute
      },
      style: 'silverpeas',
	  position: {
		  corner: {
			target: 'topRight',
			tooltip: 'bottomLeft'
		  },
		  adjust: {
			  screen: true
		  }
	  }
   });
});
</script>
<style type="text/css">
.component-icon {
	margin: 2px;
}
</style>
</HEAD>
<BODY onLoad="javascript:window.resizeTo(750,700)">
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
				<a href="CreateInstance?ComponentNum=<%=nI%>" title="<%=m_ListComponents[nI].getDescription()%>"><img src="<%=iconsPath%>/util/icons/component/<%=m_ListComponents[nI].getName()%>Small.gif" class="component-icon" alt=""/></a>
			</TD>
			<TD align="left">
				<a href="CreateInstance?ComponentNum=<%=nI%>" title="<%=m_ListComponents[nI].getDescription()%>"><%=m_ListComponents[nI].getLabel()%></a>
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
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close();", false));
	out.println(buttonPane.print());
%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>