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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkCommunicationUser.jsp" %>
<%@ page import="org.silverpeas.core.personalization.UserPreferences" %>
<%@ page import="org.silverpeas.core.personalization.service.PersonalizationServiceProvider" %>

<%
  ArrayLine arrayLine = null;
  Collection cResultData = (Collection) request.getAttribute("ConnectedUsersList");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=resources.getString("GML.popupTitle")%></title>
  <view:looknfeel/>
  <!--[ JAVASCRIPT ]-->
  <script type="text/javascript">
    <!--
    //--------------------------------------------------------------------------------------DoIdle
    ID = window.setTimeout("DoIdle();", <%=settings.getString("refreshList")%> * 1000);
    function DoIdle() {
      self.location.href = "Main";
    }

    function manageWindow(page, nom, largeur, hauteur, options) {
      var top = (screen.height - hauteur) / 2;
      var left = (screen.width - largeur) / 2;
      window.open(page, nom,
          "top=" + top + ",left=" + left + ",width=" + largeur + ",height=" + hauteur + "," + options);
    }

    function enterPopup(userId) {
      manageWindow('OpenDiscussion?userId=' + userId, 'popupDiscussion' + userId, '650', '460',
          'menubar=no,scrollbars=no,statusbar=no');
    }

    //-->
  </script>
</head>
<body>
<view:window popup="true" browseBarVisible="false">
  <%
    String icoMonitor = m_context + "/util/icons/monitor.gif";
    String icoNotify = m_context + "/util/icons/talk2user.gif";

    ArrayPane arrayPane = gef.getArrayPane("List", "", request, session);
    ArrayColumn arrayColumn1 = arrayPane.addArrayColumn("");
    arrayColumn1.setSortable(false);
    arrayPane.addArrayColumn(resources.getString("user"));
    if (settings.getBoolean("displayColumnLanguage", false)) {
      arrayColumn1 = arrayPane.addArrayColumn(resources.getString("language"));
      arrayColumn1.setSortable(true);
    }
    if (settings.getBoolean("displayColumnChat", true)) {
      arrayColumn1 = arrayPane.addArrayColumn("");
    }
    if (cResultData != null) {
      Iterator iter = cResultData.iterator();
      while (iter.hasNext()) {
        SessionInfo item = (SessionInfo) iter.next();
        if (!item.getUserDetail().getId().equals(communicationScc.getUserId())) {
          List userList = new ArrayList();
          userList.add(item.getUserDetail().getId());
          UserPreferences preferences =
              PersonalizationServiceProvider.getPersonalizationService().getUserSettings(
              item.getUserDetail().getId());

          arrayLine = arrayPane.addArrayLine();
          arrayLine.addArrayCellText(
              "<div align=\"right\"><img src=\"" + icoMonitor + "\" border=\"0\"/></div>");
          arrayLine.addArrayCellText(item.getUserDetail().getDisplayedName());
          if (settings.getBoolean("displayColumnLanguage", false)) {
            arrayLine.addArrayCellText(preferences.getLanguage());
          }
          if (settings.getBoolean("displayColumnChat", true)) {
          arrayLine.addArrayCellText("<div align=\"left\"><a href=\"#\"><img alt=\"" + resources.getString(
              "notifyUser") + "\" src=\"" + icoNotify + "\" border=\"0\" onclick=\"javascript:enterPopup('" + item.getUserDetail().getId() + "')\"></a></div>");
          }
        }
      }
      out.println(arrayPane.print());
    }
    out.println(resources.getString("refreshedTime") + "&nbsp;" + settings.getString(
        "refreshList") + "&nbsp;" + resources.getString("seconds") + "<br/>");
  %>
</view:window>
</body>
</html>