<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ include file="check.jsp" %>
<%
  Domain domObject = (Domain) request.getAttribute("domainObject");
  UserFull userObject = (UserFull) request.getAttribute("userObject");
  String groupsPath = (String) request.getAttribute("groupsPath");
  boolean isDomainRW = (Boolean) request.getAttribute("isDomainRW");
  boolean isDomainSync = (Boolean) request.getAttribute("isDomainSync");
  boolean isUserRW = (Boolean) request.getAttribute("isUserRW");
  boolean isX509Enabled = (Boolean) request.getAttribute("isX509Enabled");
  boolean isGroupManager = (Boolean) request.getAttribute("isOnlyGroupManager");
  boolean isUserManageableByGroupManager =
      (Boolean) request.getAttribute("userManageableByGroupManager");

  String thisUserId = userObject.getId();

  if (domObject != null) {
    browseBar.setComponentName(getDomainLabel(domObject, resource),
        "domainContent?Iddomain=" + domObject.getId());
  }

  if (groupsPath != null && groupsPath.length() > 0) {
    browseBar.setPath(groupsPath);
  }

  if (isDomainRW && isUserRW && (!isGroupManager || isUserManageableByGroupManager)) {
    operationPane
        .addOperation(resource.getIcon("JDP.userUpdate"), resource.getString("GML.modify"),
            "displayUserModify?Iduser=" + thisUserId);
    if (userObject.isBlockedState()) {
      operationPane
          .addOperation(resource.getIcon("JDP.userUnblock"), resource.getString("JDP.userUnblock"),
              "userUnblock?Iduser=" + thisUserId);
    } else {
      operationPane
          .addOperation(resource.getIcon("JDP.userBlock"), resource.getString("JDP.userBlock"),
              "userBlock?Iduser=" + thisUserId);
    }
    operationPane.addOperation(resource.getIcon("JDP.userDel"), resource.getString("GML.delete"),
        "javascript:ConfirmAndSend('" + resource.getString("JDP.userDelConfirm") +
            "','userDelete?Iduser=" + thisUserId + "')");
  }
  if (isDomainSync && !isGroupManager) {
    operationPane
        .addOperation(resource.getIcon("JDP.userUpdate"), resource.getString("GML.modify"),
            "displayUserMS?Iduser=" + thisUserId);
    operationPane
        .addOperation(resource.getIcon("JDP.userSynchro"), resource.getString("JDP.userSynchro"),
            "userSynchro?Iduser=" + thisUserId);
    operationPane.addOperation(resource.getIcon("JDP.userUnsynchro"),
        resource.getString("JDP.userUnsynchro"), "userUnSynchro?Iduser=" + thisUserId);
  }
  if (isX509Enabled && !isGroupManager) {
    operationPane.addLine();
    operationPane.addOperation(resource.getIcon("JDP.x509"), resource.getString("JDP.getX509"),
        "userGetP12?Iduser=" + thisUserId);
  }

%>
<html>
<head>
<view:looknfeel/>
  <script language="JavaScript">
    function ConfirmAndSend(textToDisplay, targetURL) {
      if (window.confirm(textToDisplay)) {
        window.location.href = targetURL;
      }
    }
  </script>
</head>
<body>
<%
  out.println(window.printBefore());
%>
<view:frame>
<view:board>
  <table cellpadding="5" cellspacing="0" border="0" width="100%">
    <tr>
      <td class="txtlibform"><%=resource.getString("GML.lastName") %> :</td>
      <td><%=EncodeHelper.javaStringToHtmlString(userObject.getLastName())%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("GML.surname") %> :</td>
      <td><%=EncodeHelper.javaStringToHtmlString(userObject.getFirstName())%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("GML.eMail") %> :</td>
      <td><%=EncodeHelper.javaStringToHtmlString(userObject.geteMail())%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("JDP.userRights") %> :</td>
      <td>
        <%
          switch (userObject.getAccessLevel()) {
            case ADMINISTRATOR:
              out.print(resource.getString("GML.administrateur"));
              break;
            case GUEST:
              out.print(resource.getString("GML.guest"));
              break;
            case PDC_MANAGER:
              out.print(resource.getString("GML.kmmanager"));
              break;
            case DOMAIN_ADMINISTRATOR:
              out.print(resource.getString("GML.domainManager"));
              break;
            default:
              out.print(resource.getString("GML.user"));
          }
        %>
      </td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("JDP.userState") %> :</td>
      <td>
        <fmt:message key="GML.user.account.state.${userObject.state.name}"/>
      </td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("GML.login") %> :</td>
      <td><%=EncodeHelper.javaStringToHtmlString(userObject.getLogin())%></td>
    </tr>
    <tr>
      <td class="txtlibform"><%=resource.getString("JDP.silverPassword") %> :</td>
      <td>
        <%
          if (userObject.isPasswordAvailable() && userObject.isPasswordValid()) {
            out.print(resource.getString("GML.yes"));
          } else {
            out.print(resource.getString("GML.no"));
          }
        %>
      </td>
    </tr>

    <%
      //if (isUserRW)
      //{
      String[] properties = userObject.getPropertiesNames();
      for (final String property : properties) {
        if (!property.startsWith("password")) {
    %>
    <tr>
      <td class="txtlibform">
        <%=userObject.getSpecificLabel(resource.getLanguage(), property)%> :
      </td>

      <td>
        <%
          if ("STRING".equals(userObject.getPropertyType(property)) ||
              "USERID".equals(userObject.getPropertyType(property))) {

            out.print(EncodeHelper.javaStringToHtmlString(userObject.getValue(property)));

          } else if ("BOOLEAN".equals(userObject.getPropertyType(property))) {

            if (userObject.getValue(property) != null &&
                "1".equals(userObject.getValue(property))) {
              out.print(resource.getString("GML.yes"));
            } else if (userObject.getValue(property) == null ||
                "".equals(userObject.getValue(property)) ||
                "0".equals(userObject.getValue(property))) {
              out.print(resource.getString("GML.no"));
            }
          }
        %>
      </td>
    </tr>
    <%
        }
      }
      //}
    %>

  </table>
  </view:board>
  </view:frame>
<%
  out.println(window.printAfter());
%>
</body>
</html>