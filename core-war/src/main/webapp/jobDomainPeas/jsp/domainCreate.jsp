<%--

    Copyright (C) 2000 - 2017 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %><%--

    Copyright (C) 2000 - 2017 Silverpeas

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
<%
    Board board = gef.getBoard();

    Domain domObject = (Domain)request.getAttribute("domainObject");
    String action =(String)request.getAttribute("action");

    if (action.equals("domainCreate"))
    {
        browseBar.setComponentName(resource.getString("JDP.domainAdd") + "...");
    }
    else
    {
        browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
        browseBar.setPath(resource.getString("JDP.domainUpdate") + "...");
    }

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
<script language="JavaScript">
function SubmitWithVerif(verifParams)
{
    var namefld = stripInitialWhitespace(document.domainForm.domainName.value);
    var driverfld = stripInitialWhitespace(document.domainForm.domainDriver.value);
    var propsfld = stripInitialWhitespace(document.domainForm.domainProperties.value);
    var authfld = stripInitialWhitespace(document.domainForm.domainAuthentication.value);
    var urlfld = stripInitialWhitespace(document.domainForm.silverpeasServerURL.value);
    var errorMsg = "";

    if (verifParams)
    {
         if (isWhitespace(namefld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.name")+resource.getString("JDP.missingFieldEnd")); %>";
         if (isWhitespace(driverfld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.class")+resource.getString("JDP.missingFieldEnd")); %>";
         if (isWhitespace(propsfld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.properties")+resource.getString("JDP.missingFieldEnd")); %>";
         if (isWhitespace(authfld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.serverAuthentification")+resource.getString("JDP.missingFieldEnd")); %>";
         if (isWhitespace(urlfld))
            errorMsg = "<% out.print(resource.getString("JDP.missingFieldStart")+resource.getString("JDP.silverpeasServerURL")+resource.getString("JDP.missingFieldEnd")); %>";
    }
    if (errorMsg == "")
    {
        document.domainForm.submit();
    }
    else
    {
      jQuery.popup.error(errorMsg);
    }
}
</script>
</head>
<body>

<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<form name="domainForm" action="<%=action%>" method="POST">
    <table CELLPADDING="5" CELLSPACING="0" BORDER="0" WIDTH="100%">
        <tr>
            <td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
            <td><input type="text" name="domainName" size="70" maxlength="99" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getName())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
            <td><input type="text" name="domainDescription" size="70" maxlength="399" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getDescription())%>"></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resource.getString("JDP.class")%> :</td>
            <td><input type="text" name="domainDriver" size="70" maxlength="99" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getDriverClassName())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resource.getString("JDP.properties")%> :</td>
            <td><input type="text" name="domainProperties" size="70" maxlength="99" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getPropFileName())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resource.getString("JDP.serverAuthentification")%> :</td>
            <td><input type="text" name="domainAuthentication" size="70" maxlength="99" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getAuthenticationServer())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"></td>
        </tr>
        <tr>
            <td class="txtlibform"><%=resource.getString("JDP.silverpeasServerURL")%> :</td>
            <td><input type="text" name="silverpeasServerURL" size="70" maxlength="399" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getSilverpeasServerURL())%>">&nbsp;<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"></td>
        </tr>
        <tr>
            <td class="txtlibform"></td>
            <td><%=resource.getString("JDP.silverpeasServerURLEx")%></td>
        </tr>
		<% if (domObject.getId() == null || !domObject.getId().equals("0")) { %>
        <tr>
            <td class="txtlibform"><%=resource.getString("JDP.serverTimeStamp")%> :</td>
            <td><input type="text" name="domainTimeStamp" size="70" maxlength="99" VALUE="<%=WebEncodeHelper.javaStringToHtmlString(domObject.getTheTimeStamp())%>"></td>
        </tr>
		<% } else if (JobDomainSettings.usersInDomainQuotaActivated) {%>
        <tr>
          <td class="txtlibform"><%=resource.getString("JDP.userDomainQuotaMaxCount")%> :</td>
          <td>
            <input type="text" name="userDomainQuotaMaxCount" size="40" maxlength="399" value="<%=domObject.getUserDomainQuota().getMaxCount()%>"/>&nbsp;<img src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/> <%=resource.getString("JDP.userDomainQuotaMaxCountHelp")%>
          </td>
        </tr>
        <% } %>
        <tr>
            <td colspan="2">(<img border="0" src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"> : <%=resource.getString("GML.requiredField")%>)</td>
        </tr>
    </table>

<%
out.println(board.printAfter());
%>
</form>
<br/>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif(true)", false));
      bouton.addButton(gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
		  out.println(bouton.print());
		%>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
	%>

</body>
</html>