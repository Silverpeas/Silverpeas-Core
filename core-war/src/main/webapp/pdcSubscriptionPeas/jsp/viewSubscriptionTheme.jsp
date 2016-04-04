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
<%@ page import="org.silverpeas.core.subscription.constant.SubscriberType" %>
<%@ page import="org.silverpeas.core.web.subscription.bean.NodeSubscriptionBean" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
  Collection<NodeSubscriptionBean> subscriptions = (Collection) request.getAttribute("subscriptions");
  String currentUserId = (String) request.getAttribute("currentUserId");
  String userId = (String) request.getAttribute("userId");
  String action = (String) request.getAttribute("action");

  OrganizationController organizationCtrl = sessionController.getOrganisationController();
  final String rootPath = resource.getString("Path");

  boolean isReadOnly = false;
  if (action != null && action.equals("showUserSubscriptions")) {
    isReadOnly = true;
  }

  String language = resource.getLanguage();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel/>
<script type="text/javascript">
  function confirmDelete() {
    var boxItems = document.readForm.themeCheck;
    if (boxItems != null) {
      var nbBox = boxItems.length;
      var sendIt = false;
      if ((nbBox == null) && (boxItems.checked == true)) {
        sendIt = true;
      } else {
        for (var i = 0; i < boxItems.length; i++) {
          if (boxItems[i].checked == true) {
            sendIt = true;
          }
        }
      }

      if (sendIt && areYouSure()) {
        document.readForm.mode.value = 'delete';
        document.readForm.submit();
      }
    }
  }

function areYouSure() {
    return confirm("<%=resource.getString("confirmDeleteSubscription")%>");
}
</script>
</head>
<body>
<form name="readForm" action="DeleteTheme" method="post">
<input type="hidden" name="mode"/>

  <%
    browseBar.setComponentName(rootPath);

    TabbedPane tabbedPane = gef.getTabbedPane();
    tabbedPane.addTab(resource.getString("pdc"), "subscriptionList.jsp?userId=" + userId, false);
    tabbedPane.addTab(resource.getString("thematique"), "#", true);
    tabbedPane.addTab(resource.getString("application"),
        "ViewSubscriptionComponent?userId=" + userId + "&action=" + action, false);

    if (!isReadOnly) {
      operationPane.addOperation(resource.getIcon("icoDelete"), resource.getString("DeleteSC"),
          "javascript:confirmDelete()");
    }

    out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());

    ArrayPane arrayPane =
        gef.getArrayPane("ViewSubscriptionTheme", "ViewSubscriptionTheme", request, session);
    arrayPane.addArrayColumn(resource.getString("SubscriptionType"));
    arrayPane.addArrayColumn(resource.getString("emplacement"));
    if (!isReadOnly) {
      arrayPane.addArrayColumn(resource.getString("Operations")).setSortable(false);
    }

    // remplissage de l'ArrayPane avec les abonnements
    if (!subscriptions.isEmpty()) {
      for (NodeSubscriptionBean subscription : subscriptions) {
        ArrayLine line = arrayPane.addArrayLine();
        StringBuilder subTypeLabel = new StringBuilder();
        subTypeLabel.append(
            resource.getString("SubscriptionMethod." + subscription.getSubscriptionMethod()));
        if (SubscriberType.GROUP.equals(subscription.getSubscriber().getType())) {
          subTypeLabel.append(" ");
          subTypeLabel.append(resource.getString("SubscriptionType." + subscription.getSubscriber().getType()));
          subTypeLabel.append(" <b>");
          subTypeLabel.append(subscription.getSubscriberName());
          subTypeLabel.append("</b>");
        }
        line.addArrayCellText(subTypeLabel.toString());
        if (!isReadOnly) {
          line.addArrayCellLink(subscription.getPath(), subscription.getLink());
        } else {
          line.addArrayCellText(subscription.getPath());
        }
        if (!isReadOnly && !subscription.isReadOnly()) {
          String delete = subscription.getResource().getId() + "-" +
              subscription.getResource().getInstanceId() + "-" +
              subscription.getCreatorId();
          line.addArrayCellText(
              "&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"checkbox\" name=\"themeCheck\" value=\"" +
                  delete + "\">");
        }
      }
    }

    out.println(arrayPane.print());

    out.println(frame.printAfter());
    out.println(window.printAfter());
  %>

</form>
<form name="subscribeThemeForm" action="" method="post">
	<input type="hidden" name="Id"/>
</form>
</body>
</html>