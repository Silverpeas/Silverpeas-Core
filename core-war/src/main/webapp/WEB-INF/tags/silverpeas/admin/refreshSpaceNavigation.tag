<%--
  Copyright (C) 2000 - 2026 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="org.silverpeas.core.admin.space.SpaceInst" %>
<%@ tag import="org.silverpeas.web.jobstartpage.control.JobStartPagePeasSessionController" %>
<%@ tag description="Refreshes the navigation of the administration of the spaces when asked by
the request. It has to be used within the head of the page: written before the DOCTYPE, the script
would put the browser into the quirks mode." %>
<%
  final Boolean haveToRefreshNavBar = (Boolean) request.getAttribute("haveToRefreshNavBar");
  if (Boolean.TRUE.equals(haveToRefreshNavBar)) {
    final JobStartPagePeasSessionController jobStartPageSC =
        (JobStartPagePeasSessionController) request.getAttribute("jobStartPagePeas");
    boolean isRoot = true;
    String currentSpaceId = "undefined";
    final SpaceInst currentSpace = jobStartPageSC.getSpaceInstById();
    if (currentSpace != null) {
      isRoot = currentSpace.isRoot();
      currentSpaceId = "'" + currentSpace.getId() + "'";
    }
%>
<script type="text/javascript">
  if (top.window.spAdminWindow) {
    const isRoot = <%=isRoot%>;
    const currentSpaceId = <%=currentSpaceId%>;
    if (isRoot) {
      top.window.spAdminWindow.loadSpace(currentSpaceId);
    } else {
      top.window.spAdminWindow.loadSubSpace(currentSpaceId);
    }
  }
</script>
<% } %>
