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
<%@ include file="check.jsp" %>
<%
  Board board = gef.getBoard();

  Domain domObject = (Domain)request.getAttribute("domainObject");
  String action =(String)request.getAttribute("action");

  if (action.equals("domainSQLCreate")) {
    browseBar.setComponentName(resource.getString("JDP.domainSQLAdd") + "...");
  } else {
	browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath(resource.getString("JDP.domainSQLUpdate") + "...");
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">
function SubmitWithVerif() {
    var namefld = stripInitialWhitespace(document.domainForm.domainName.value);
    var urlfld = stripInitialWhitespace(document.domainForm.silverpeasServerURL.value);
    var errorMsg = "";

    if (isWhitespace(namefld)) {
       errorMsg = "<% out.print(resource.getString("JDP.name")); %>";
    } else if (isWhitespace(urlfld)) {
       errorMsg = "<% out.print(resource.getString("JDP.silverpeasServerURL")); %>";
    } else {
       <% if (JobDomainSettings.usersInDomainQuotaActivated) { %>
	       var maxCount = stripInitialWhitespace(document.domainForm.userDomainQuotaMaxCount.value);
	       if (isWhitespace(maxCount)) {
		errorMsg = "<% out.print(resource.getString("JDP.userDomainQuotaMaxCount")); %>";
	       }
	   <% } %>
    }

    if (errorMsg == "") {
        document.domainForm.submit();
    } else {
        window.alert("<% out.print(resource.getString("JDP.missingFieldStart")); %>"
            + errorMsg
            + "<% out.print(resource.getString("JDP.missingFieldEnd")); %>");
    }
}
</script>
</head>
<body>

<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<form name="domainForm" action="<%=action%>" method="post">
<%
out.println(board.printBefore());
%>
    <table cellpadding="5" cellspacing="0" width="100%">
                    <tr>
                        <td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
                        <td>
                            <input type="text" name="domainName" size="40" maxlength="99" value="<%=EncodeHelper.javaStringToHtmlString(domObject.getName())%>"/>&nbsp;<img src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="txtlibform"><%=resource.getString("GML.description")%> :</td>
                        <td>
                            <input type="text" name="domainDescription" size="40" maxlength="399" value="<%=EncodeHelper.javaStringToHtmlString(domObject.getDescription())%>"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="txtlibform"><%=resource.getString("JDP.silverpeasServerURL")%> :</td>
                        <td>
                            <input type="text" name="silverpeasServerURL" size="40" maxlength="399" value="<%=EncodeHelper.javaStringToHtmlString(domObject.getSilverpeasServerURL())%>"/>&nbsp;<img src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/> <%=resource.getString("JDP.silverpeasServerURLEx")%>
                        </td>
                    </tr>
                    <% if (JobDomainSettings.usersInDomainQuotaActivated) { %>
	                    <tr>
	                        <td class="txtlibform"><%=resource.getString("JDP.userDomainQuotaMaxCount")%> :</td>
	                        <td>
	                            <input type="text" name="userDomainQuotaMaxCount" size="40" maxlength="399" value="<%=domObject.getUserDomainQuota().getMaxCount()%>"/>&nbsp;<img src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/> <%=resource.getString("JDP.userDomainQuotaMaxCountHelp")%>
	                        </td>
	                    </tr>
                    <% } %>
                    <tr>
                        <td colspan="2"><img src="<%=resource.getIcon("JDP.mandatory")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%></td>
                    </tr>
    </table>
<%
out.println(board.printAfter());
%>
</form>
<%
  ButtonPane bouton = gef.getButtonPane();
  bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:SubmitWithVerif()", false));
  bouton.addButton(gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
  out.println(bouton.print());
  out.println(frame.printAfter());
  out.println(window.printAfter());
%>
</body>
</html>