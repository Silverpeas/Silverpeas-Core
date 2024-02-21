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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.kernel.util.Pair" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBarElement" %>
<%@ page import="java.util.List" %>
<%@ page import="org.silverpeas.core.util.CollectionUtil" %>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
      response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
      response.setHeader("Pragma", "no-cache"); //HTTP 1.0
      response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
<%@ include file="check.jsp" %>
<html>
  <head>
    <title><%=resource.getString("GML.popupTitle")%></title>
    <view:looknfeel/>
  </head>

  <%
        Pair<String, String> hostComponentNameObject = (Pair<String, String>) request.getAttribute("HostComponentName");
        String hostSpaceName = (String) request.getAttribute("HostSpaceName");
        String hostComponentName = hostComponentNameObject.getFirst();
        List<String> hostPath = (List<String>) request.getAttribute("HostPath");
  %>

  <body>

    <%
          browseBar.setDomainName(hostSpaceName);
          browseBar.setComponentName(hostComponentName);
          if (CollectionUtil.isNotEmpty(hostPath)) {
            for (String pathItem : hostPath) {
              browseBar.addElement(new BrowseBarElement(pathItem, null));
            }
          }

          out.println(window.printBefore());
          out.println(frame.printBefore());

          Button closeButton = gef.getFormButton(resource.getString("GML.close"), "javascript:onClick=window.close();", false);
    %>

    <table ALIGN="CENTER" CELLPADDING=2 CELLSPACING=0 BORDER=0 WIDTH="98%" CLASS="intfdcolor">
      <tr>
        <td>
          <table ALIGN=CENTER CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%" CLASS=intfdcolor4>
            <tr>
              <td>
                <table border=0 cellPadding=1 cellSpacing=1 width="389" align="center">
                  <tr>
                    <td width="369" align="center"><%=WebEncodeHelper.javaStringToHtmlString(resource.getString("AlertsConfirmation"))%></td>
                  </tr>
                </table>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <%
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton(closeButton);
          buttonPane.setHorizontalPosition();
          out.println(frame.printMiddle());
          out.println("<br/><center>" + buttonPane.print() + "<br></center>");
          out.println(frame.printAfter());
          out.println(window.printAfter());
    %>

  </body>
</html>
