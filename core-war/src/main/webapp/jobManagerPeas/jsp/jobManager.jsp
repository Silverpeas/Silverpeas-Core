<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title><%=resource.getString("GML.popupTitle")%></title>
  <script type="text/javascript">window.__admin_top = true;</script>
  <view:looknfeel/>
  <style type="text/css">
    body {
      margin: 0;
      padding: 0;
      border: none;
      overflow: hidden;
    }

    #sp-admin-layout-main {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      flex-direction: column;
    }

    #sp-admin-layout-header-part, #sp-admin-layout-body-part {
      padding: 0;
      margin: 0;
      border: none;
    }

    #sp-admin-layout-header-part {
      width: 100%;
    }

    #sp-admin-layout-body-part {
      width: 100%;
      display: table;
    }
  </style>
  <meta name="viewport" content="initial-scale=1.0"/>
</head>
<body>
<div id="sp-admin-layout-main">
  <div id="sp-admin-layout-header-part"></div>
  <div id="sp-admin-layout-body-part"></div>
</div>
<view:progressMessage/>
<script type="text/javascript">
  (function() {
    whenSilverpeasReady(function() {
      initializeSilverpeasAdminLayout('${requestScope.adminBodyUrl}');
    });
  })();
</script>
</body>
</html>