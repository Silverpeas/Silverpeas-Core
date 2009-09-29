<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="check.jsp" %>
<%
    Board board = gef.getBoard();

    Group  grObject = (Group)request.getAttribute("groupObject");
    String action =(String)request.getAttribute("action");
    String groupsPath = (String)request.getAttribute("groupsPath");

    browseBar.setDomainName(resource.getString("JDP.jobDomain"));
    browseBar.setComponentName(Encode.javaStringToHtmlString((String)request.getAttribute("domainName")), (String)request.getAttribute("domainURL"));
    browseBar.setPath(groupsPath);
%>
<html>
<head>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/overlib.js"></script>
<script language="JavaScript">
function SubmitWithVerif(verifParams)
{
    var namefld = stripInitialWhitespace(document.groupForm.groupName.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(namefld)) 
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("GML.name")+resource.getString("JDP.missingFieldEnd")); %>";
    }
    if (errorMsg == "")
    {
        document.groupForm.submit();
    }
    else
    {
        window.alert(errorMsg);
    }
}
</script>
</head>
<body marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<center>
<%
out.println(board.printBefore());
%>
<form name="groupForm" action="<%=action%>" method="POST">
    <input type="hidden" name="Idgroup" value="<% if (grObject.getId() != null) out.print(grObject.getId()); %>">
    <input type="hidden" name="Idparent" value="<% if (grObject.getSuperGroupId() != null) out.print(grObject.getSuperGroupId()); %>">
    <table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
                    <tr>			
                        <td valign="baseline" align=left class="txtlibform">
                            <%=resource.getString("GML.name")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="groupName" size="50" maxlength="99" VALUE="<%=Encode.javaStringToHtmlString(grObject.getName())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
                        </td>
                    </tr>
                    <tr>			
                        <td valign="baseline" align=left class="txtlibform">
                            <%=resource.getString("GML.description")%> :
                        </td>
                        <td align=left valign="baseline">
                            <input type="text" name="groupDescription" size="50" maxlength="399" VALUE="<%=Encode.javaStringToHtmlString(grObject.getDescription())%>"> 
                        </td>
                    </tr>
                    <tr>			
                    	<td valign="baseline" align="left" class="txtlibform"><%=resource.getString("JDP.synchroRule")%> :</td>
                    	<td align="left" valign="baseline">
                        	<input type="text" name="groupRule" size="50" maxlength="100" VALUE="<%=Encode.javaStringToHtmlString(grObject.getRule())%>">
                        	<img border="0" align="absmiddle" src="<%=resource.getIcon("JDP.info")%>" onmouseover="return overlib('<%=Encode.javaStringToJsString(resource.getString("JDP.synchroRuleInfo"))%>', STICKY, CAPTION, '<%=resource.getString("JDP.synchroRuleAvail")%>', CLOSETEXT, '<%=resource.getString("GML.close")%>');" onmouseout="return nd();">
                        </td>
                    </tr>
                    <tr> 
                        <td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> 
                  : <%=resource.getString("GML.requiredField")%>)
              </td>
          </tr>
    </table>

<%
out.println(board.printAfter());
%>
</form>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
          bouton.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "groupContent", false));
		  out.println(bouton.print());
		%>
</center>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>
</body>
</html>