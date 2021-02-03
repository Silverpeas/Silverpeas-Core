<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@ page import="org.silverpeas.core.admin.domain.synchro.SynchroDomainReport" %><%--

    Copyright (C) 2000 - 2021 Silverpeas

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

<%@ include file="check.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title> Synchro Dynamic Report - Mode <%=SynchroDomainReport.getReportLevel()%>
  </title>
</head>
<body class="page_content_admin">
<NOBR>
  <%
    Thread.sleep(100);
    String toDisp = SynchroDomainReport.getMessages();
    boolean isActive = true;

    SynchroDomainReport.waitForStart();
    while (isActive || (toDisp != null)) {
      if (toDisp != null) {
        if (toDisp.length() > 0) {
          if (toDisp.startsWith("[F")) {
            out.print("<FONT SIZE=3 COLOR=#FF0000>");
          } else if (toDisp.startsWith("[E")) {
            out.print("<FONT SIZE=3 COLOR=#FF6666>");
          } else if (toDisp.startsWith("[W")) {
            out.print("<FONT COLOR=#000000>");
          } else if (toDisp.startsWith("[I")) {
            out.print("<FONT COLOR=#0000FF>");
          } else if (toDisp.startsWith("[D")) {
            out.print("<FONT COLOR=#B6B6B6>");
          } else {
            out.print("<FONT COLOR=#00FF00>");
          }
        }
        out.print(WebEncodeHelper.javaStringToHtmlString(toDisp) + "<BR>");
        out.print("</FONT>");
        out.flush();
      }
      // Verifying the state before getting the next message.
      isActive = (SynchroDomainReport.isSynchroActive()) ||
          (SynchroDomainReport.getState() == SynchroDomainReport.STATE_WAITSTART);
      toDisp = SynchroDomainReport.getMessages();
    }
    SynchroDomainReport.reset();
  %>
</NOBR>
</body>
</html>