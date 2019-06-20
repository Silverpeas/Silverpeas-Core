<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
Collection<ConnectionDetail> 	connections = (Collection<ConnectionDetail>) request.getAttribute("Connections");
%>

<%@page import="org.silverpeas.core.admin.service.OrganizationControllerProvider"%>
<%@ page import="org.silverpeas.core.util.CollectionUtil" %>
<html>
<head>
  <view:looknfeel />
    <script language="javascript">
      var connectionWindow = window;

      function editConnection(id) {
          urlWindows = "EditConnection?ConnectionId="+id;
          windowName = "connectionWindow";
          larg = "650";
          haut = "280";
          windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
          if (!connectionWindow.closed && connectionWindow.name== "connectionWindow")
              connectionWindow.close();
          connectionWindow = SP_openWindow(urlWindows, windowName, larg, haut, windowParams);
      }

      function deleteConnection(id) {
        jQuery.popup.confirm("<%=resource.getString("webConnections.confirmDeleteConnection")%>", function() {
          document.deleteForm.ConnectionId.value = id;
          document.deleteForm.submit();
        });
      }
    </script>
  </head>
  <body>
      <view:browseBar extraInformations='<%=resource.getString("webConnections.label")%>'/>
      <view:window>
        <view:frame>
<%
      ArrayPane arrayPane = gef.getArrayPane("connectionList", "ViewConnections", request, session);
      arrayPane.addArrayColumn(resource.getString("GML.name"));
      arrayPane.addArrayColumn(resource.getString("webConnections.login"));
      ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("GML.operation"));
      columnOp.setSortable(false);
      // remplissage de l'ArrayPane avec les connexions
      if (CollectionUtil.isNotEmpty(connections)) {
        for(ConnectionDetail connection : connections) {
          ArrayLine line = arrayPane.addArrayLine();
          line.addArrayCellText(connection.getComponentName());
          ComponentInst inst = OrganizationControllerProvider
              .getOrganisationController().getComponentInst(connection.getComponentId());
          String nameLogin = inst.getParameterValue("login");
          String name =  connection.getParam().get(nameLogin);
          line.addArrayCellText(name);
          IconPane iconPane = gef.getIconPane();
          Icon updateIcon = iconPane.addIcon();
          Icon deleteIcon = iconPane.addIcon();
          updateIcon.setProperties(resource.getIcon("webConnections.update"), resource.getString(
              "webConnections.updateConnection"), "javaScript:onClick=editConnection('" + connection.
              getConnectionId() + "')");
          deleteIcon.setProperties(resource.getIcon("webConnections.delete"), resource.getString(
              "webConnections.deleteConnection"), "javaScript:onClick=deleteConnection('" + connection.
              getConnectionId() + "')");
          line.addArrayCellText(updateIcon.print() + "&nbsp;&nbsp;&nbsp;&nbsp;" + deleteIcon.print());
        }
      }

      out.println(arrayPane.print());
  %>
        </view:frame>
      </view:window>

    <form name="connectionForm" action="" method="post">
      <input type="hidden" name="Login"/>
      <input type="hidden" name="Password"/>
      <input type="hidden" name="ConnectionId"/>
      <input type="hidden" name="ComponentId"/>
    </form>
    <form name="deleteForm" action="DeleteConnection" method="post">
      <input type="hidden" name="ConnectionId"/>
    </form>

    <div id="updateForm" style="display: none;">
      <form name="categoryForm" action="CreateCategory" method="post">
        <table cellpadding="5" width="100%">
          <tr>
            <td class="txtlibform"><fmt:message key="GML.title"/> :</td>
            <td><input type="text" name="Name" id="categoryName" size="60" maxlength="150"/>
              &nbsp;<img border="0" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/></td>
          </tr>
          <tr>
            <td class="txtlibform"><fmt:message key="GML.description" /> :</td>
            <td><input type="text" name="Description" id="categoryDescription" size="60" maxlength="150"/></td>
          </tr>
          <tr>
            <td colspan="2"><img border="0" alt="mandatory" src="<c:out value="${mandatoryFieldUrl}" />" width="5" height="5"/> : <fmt:message key="GML.requiredField"/></td>
          </tr>
        </table>
        <input type="hidden" name="CategoryId"/>
      </form>
    </div>

  </body>
</html>