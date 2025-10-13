<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>

<%
String 		m_SousEspace 		= (String) request.getAttribute("SousEspace");
SpaceInst[] brothers 			= (SpaceInst[]) request.getAttribute("brothers");
String 		spaceId				= (String) request.getAttribute("CurrentSpaceId");
boolean isUserAdmin = (Boolean)request.getAttribute("isUserAdmin");
boolean isComponentSpaceQuotaActivated = isUserAdmin && JobStartPagePeasSettings.COMPONENTS_IN_SPACE_QUOTA_ENABLED;
long    defaultDataStorageQuota = JobStartPagePeasSettings.DATA_STORAGE_IN_SPACE_QUOTA_DEFAULT_MAX_COUNT;
boolean isDataStorageQuotaActivated = isUserAdmin && JobStartPagePeasSettings.DATA_STORAGE_IN_SPACE_QUOTA_ENABLED;
boolean isInheritanceEnable = JobStartPagePeasSettings.IS_INHERITANCE_ENABLED &&
        (Boolean) request.getAttribute("inheritanceSupported");
String  action = (String) request.getAttribute("Action");

	browseBar.setSpaceId(spaceId);
	if (m_SousEspace == null) {
    browseBar.setComponentName(resource.getString("JSPP.creationSpace"));
  } else {
    browseBar.setPath(resource.getString("JSPP.creationSubSpace"));
  }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resource.getString("GML.popupTitle")%></title>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript">
/*****************************************************************************/
function B_VALIDER_ONCLICK() {
  ifCorrectFormExecute(function() {
    document.infoSpace.submit();
  });
}

/*****************************************************************************/

function ifCorrectFormExecute(callback) {
  let errorMsg = "";
  let errorNb = 0;

  const name = stripInitialWhitespace(document.infoSpace.NameObject.value);
  if (isWhitespace(name)) {
    errorMsg += "  - '<%=resource.getString("GML.name")%>' <%=resource.getString("MustContainsText")%>\n";
    errorNb++;
  }

  const textAreaLength = 400;
  const s = document.infoSpace.Description.value;
  if (! (s.length <= textAreaLength)) {
      errorMsg += "  - '<%=resource.getString("GML.description")%>' <%=resource.getString("ContainsTooLargeText")+"400 "+resource.getString("Characters")%>\n";
      errorNb++;
    }

  <% if (isComponentSpaceQuotaActivated) { %>
  const componentSpaceQuota = document.infoSpace.ComponentSpaceQuota.value;
  if (isWhitespace(componentSpaceQuota)) {
    errorMsg += "  - '<%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%>' <%=resource.getString("MustContainsText")%>\n";
    errorNb++;
  }
  <% } %>

  <% if (isDataStorageQuotaActivated) { %>
  const dataStorageQuota = document.infoSpace.DataStorageQuota.value;
  if (isWhitespace(dataStorageQuota)) {
    errorMsg += "  - '<%=resource.getString("JSPP.dataStorageQuota")%>' <%=resource.getString("MustContainsText")%>\n";
    errorNb++;
  }
  <% } %>


  switch (errorNb) {
    case 0 :
      callback.call(this);
      break;
    case 1 :
      errorMsg = "<%=resource.getString("ThisFormContains")%> 1 <%=resource.getString("GML.error")%> : \n" + errorMsg;
      jQuery.popup.error(errorMsg);
      break;
    default :
      errorMsg = "<%=resource.getString("ThisFormContains")%> " + errorNb + " <%=resource.getString("GML.errors")%> :\n" + errorMsg;
      jQuery.popup.error(errorMsg);
  }
}
</script>
</head>
<body onload="document.infoSpace.NameObject.focus();" class="page_content_admin">
<form name="infoSpace" action="<%= action %>" method="post">

<%
out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>

		<table border="0" cellspacing="0" cellpadding="5" width="100%">
			<%=I18NHelper.getFormLine(resource)%>
			<tr>
				<td class="txtlibform"><%=resource.getString("GML.name")%> :</td>
				<td><input type="text" name="NameObject" size="60" maxlength="60" value=""/>&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"/></td>
			</tr>
			<tr>
				<td class="txtlibform" valign="top"><%=resource.getString("GML.description")%> :</td>
				<td><textarea name="Description" rows="4" cols="60"></textarea></td>
			</tr>
			<tr>
				<td class="txtlibform"><%=resource.getString("JSPP.SpacePlace")%> :</td>
				<td valign="top">
                    <select name="SpaceBefore" id="SpaceBefore">
                        <%
                            for (int i = 0; i < brothers.length; i++) {
                                out.println("<option value=\"" + brothers[i].getId() + "\">" + brothers[i].getName() + "</option>");
                            }
                        %>
                        <option value="-1" selected="selected"><%=resource.getString("JSPP.PlaceLast")%></option>
                    </select>
				</td>
			</tr>
      <% if (isComponentSpaceQuotaActivated) { %>
        <tr>
          <td class="txtlibform"><%=resource.getString("JSPP.componentSpaceQuotaMaxCount")%> :</td>
          <td><input type="text" name="ComponentSpaceQuota" size="5" maxlength="4" value="0"/>&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"/> <%=resource.getString("JSPP.componentSpaceQuotaMaxCountHelp")%></td>
        </tr>
      <% } %>
      <% if (isDataStorageQuotaActivated) { %>
        <tr>
          <td class="txtlibform"><%=resource.getString("JSPP.dataStorageQuota")%> :</td>
          <td><input type="text" name="DataStorageQuota" size="9" maxlength="10" value="<%=defaultDataStorageQuota%>">&nbsp;<img src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5" border="0"> <%=resource.getString("JSPP.dataStorageQuotaHelp")%></td>
        </tr>
      <% } %>
      <% if (isInheritanceEnable && spaceId != null) { %>
      <tr>
        <td class="txtlibform" nowrap="nowrap" valign="top"><%=resource.getString("JSPP.inheritanceBlockedComponent") %> :</td>
        <td align="left" valign="top" width="100%">
          <input type="radio" name="InheritanceBlocked" value="true"/> <%=resource.getString("JSPP.inheritanceSpaceNotUsed")%><br/>
          <input type="radio" name="InheritanceBlocked" value="false" checked="checked" /> <%=resource.getString("JSPP.inheritanceSpaceUsed")%>
        </td>
      </tr>
      <% } %>
			<tr align="left">
				<td colspan="2"><img border="0" src="<%=resource.getIcon("mandatoryField")%>" width="5" height="5"/> : <%=resource.getString("GML.requiredField")%></td>
			</tr>
		</table>

<%
		out.println(board.printAfter());

		ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
		buttonPane.addButton(gef.getFormButton(resource.getString("GML.cancel"), "javascript:onclick=history.back()", false));
		out.println("<br/>"+buttonPane.print());

		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</form>
</body>
</html>