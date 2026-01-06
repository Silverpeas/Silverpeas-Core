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
<%@ page import="org.silverpeas.kernel.logging.Level" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
    Domain domObject = (Domain)request.getAttribute("domainObject");

    browseBar.setComponentName(getDomainLabel(domObject, resource), "domainContent?Iddomain="+domObject.getId());
    browseBar.setPath(resource.getString("JDP.domainSynchro") + "...");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel withCheckFormScript="true"/>
  <script type="text/javascript">
    function ValidForm(){
      SP_openWindow('<%=m_context %>/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel=' + document.domainForm.IdTraceLevel.value, 'SynchroDomainReport', '750', '550', 'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
      document.domainForm.submit();
    }
  </script>
</head>
<body class="page_content_admin">
<%
out.println(window.printBefore());
out.println(frame.printBefore());
%>
<view:frame>
<view:board>
<form name="domainForm" action="domainSynchro" method="post">
  <input type="hidden" name="X-ATKN" value="${requestScope['X-ATKN']}"/>
  <table>
    <tr>
        <td class="txtlibform">
            <%=resource.getString("GML.name")%> :
        </td>
        <td>
            <%=WebEncodeHelper.javaStringToHtmlString(domObject.getName())%>
        </td>
    </tr>
    <% if (StringUtil.isDefined(domObject.getDescription())) { %>
    <tr>
        <td class="txtlibform">
            <%=resource.getString("GML.description")%> :
        </td>
        <td>
            <%=WebEncodeHelper.javaStringToHtmlString(domObject.getDescription())%>
        </td>
    </tr>
    <% } %>
    <tr>
      <td class="txtlibform">
        <%=resource.getString("JDP.traceLevel")%> :
      </td>
      <td>
        <select name="IdTraceLevel" size="1">
          <option value="<%=Level.DEBUG%>">Debug</option>
          <option value="<%=Level.INFO%>" selected="selected">Info</option>
          <option value="<%=Level.WARNING%>">Warning</option>
          <option value="<%=Level.ERROR%>">Error</option>
        </select>
      </td>
    </tr>
  </table>
</view:board>
</form>
		<%
		  ButtonPane bouton = gef.getButtonPane();
		  bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:ValidForm()", false));
      bouton.addButton(gef.getFormButton(resource.getString("GML.cancel"), "domainContent", false));
		  out.println(bouton.print());
		%>
</view:frame>
<%
out.println(window.printAfter());
%>
</body>
</html>