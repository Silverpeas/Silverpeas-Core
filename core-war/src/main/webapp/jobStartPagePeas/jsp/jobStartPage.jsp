<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ include file="check.jsp" %>

<style type="text/css">
  #sp-admin-layout-body-part-layout {
    width: 100%;
    flex: 1;
    display: flex;
    flex-direction: row;
  }

  #sp-admin-layout-body-part-layout-navigation-part {
    position: relative;
    overflow: auto;
  }

  #sp-admin-layout-body-part-layout-content-part {
    flex: 1;
    height: 100%;
  }
</style>
<div id="sp-admin-layout-body-part-layout">
  <div id="sp-admin-layout-body-part-layout-navigation-part"></div>
  <div id="sp-admin-layout-body-part-layout-content-part"></div>
</div>
<script type="text/javascript">
  (function() {
    spAdminLayout.getBody().ready(function() {
      spAdminWindow.loadSpaceAndComponentHomepage();
    });
  })();
</script>