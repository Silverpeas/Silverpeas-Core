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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ include file="check.jsp" %>
<%
  Domain domain = (Domain) request.getAttribute("domainObject");
%>
<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<c:set var="path"><fmt:message key="JDP.domainSynchro"/><%="..."%></c:set>
<c:set var="componentName"><%=getDomainLabel(domain, resource)%>
</c:set>
<c:set var="componentLink"><%="domainContent?Iddomain=" + domain.getId()%>
</c:set>

<view:sp-page>
  <view:sp-head-part withCheckFormScript="true">
    <script type="text/javascript">
      function ValidForm() {
        SP_openWindow(webContext + '/RjobDomainPeas/jsp/displayDynamicSynchroReport?IdTraceLevel='
            + document.domainForm.IdTraceLevel.value, 'SynchroDomainReport', '750', '550',
            'menubar=yes,scrollbars=yes,statusbar=yes,resizable=yes');
        document.domainForm.submit();
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="page_content_admin">
    <view:browseBar path="${path}">
      <view:browseBarElt label="${componentName}" link="${componentLink}"/>
    </view:browseBar>
    <view:window>
      <view:frame>
        <view:board>
          <form name="domainForm" action="domainSynchro" method="post">
          <input type="hidden" name="X-ATKN" value="${requestScope['X-ATKN']}"/>
          <table>
            <tr>
              <th class="txtlibform">
                <%=resource.getString("GML.name")%> :
              </th>
              <td>
                <%=WebEncodeHelper.javaStringToHtmlString(domain.getName())%>
              </td>
            </tr>
            <% if (StringUtil.isDefined(domain.getDescription())) { %>
            <tr>
              <th class="txtlibform">
                <%=resource.getString("GML.description")%> :
              </th>
              <td>
                <%=WebEncodeHelper.javaStringToHtmlString(domain.getDescription())%>
              </td>
            </tr>
            <% } %>
            <tr>
              <th class="txtlibform">
                <label for="traceLevel"><%=resource.getString("JDP.traceLevel")%>:</label>
              </th>
              <td>
                <select id="traceLevel" name="IdTraceLevel" size="1">
                  <option value="<%=Level.DEBUG%>">Debug</option>
                  <option value="<%=Level.INFO%>" selected="selected">Info</option>
                  <option value="<%=Level.WARNING%>">Warning</option>
                  <option value="<%=Level.ERROR%>">Error</option>
                </select>
              </td>
            </tr>
          </table>
        </view:board>
        <view:buttonPane>
          <view:button label="${validateLabel}" action="javascript:ValidForm()" disabled="false"/>
          <view:button label="${cancelLabel}" action="domainContent" disabled="false"/>
        </view:buttonPane>
        </form>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>