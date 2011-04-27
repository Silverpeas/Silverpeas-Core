<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="check.jsp" %>
<%
  String spaceId = (String) request.getAttribute("CurrentSpaceId");

  browseBar.setSpaceId(spaceId);
  browseBar.setClickable(false);
  browseBar.setExtraInformation(resource.getString("JSPP.creationInstance"));
%>
<html>
  <head>
    <title><%=resource.getString("GML.popupTitle")%></title>
    <view:looknfeel/>
    <%
      out.println(gef.getLookStyleSheet());
    %>
    <script type="text/javascript">
      $(document).ready(function() 
      {
        // By suppling no content attribute, the library uses each elements title attribute by default
        $('a[title]').qtip({
          content: {
            text: false // Use each elements title attribute
          },
          style: 'silverpeas',
          position: {
            corner: {
              target: 'topRight',
              tooltip: 'bottomLeft'
            },
            adjust: {
              screen: true
            }
          }
        });
      });
    </script>
    <style type="text/css">
      .component-icon {
        margin: 2px;
      }
    </style>
  </head>
  <body onLoad="javascript:window.resizeTo(750,700)">
    <view:window>
      <view:frame> 
      <center>
        <view:board>
          <br />
          <table width="70%" align="center" border="0" cellPadding="0" cellSpacing="0">
            <c:set var="currentSuite" value="" scope="page"/>
            <c:forEach items="${requestScope.ListComponents}" var="component" varStatus="loop">
              <c:if test="${component.visible}">
                <c:if test="${component.suite != null && component.suite != currentSuite}">
                  <c:set var="currentSuite" value="${component.suite}" scope="page"/>         
                  <tr>
                    <td colspan="2" align="center" class="txttitrecol">&nbsp;</td>
                  </tr>
                  <tr>
                    <td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"></td>
                  </tr>
                  <tr>
                    <td align="center" class="txttitrecol">&nbsp;</td>
                    <td align="center" class="txttitrecol"><c:out value="${currentSuite}"/></td>
                  </tr>
                  <tr>
                    <td colspan="2" align="center" class="intfdcolor" height="1"><img src="<%=resource.getIcon("JSPP.px")%>"></td>
                  </tr>
                  <tr>
                    <td colspan="2" align="center" height="2"><img src="<%=resource.getIcon("JSPP.px")%>"></td>
                  </tr>
                </c:if>
                <tr>
                  <td align="center" width="30">
                    <a href="CreateInstance?ComponentNum=<c:out value="${loop.index}" />" title="<c:out value="${component.description}" />"><img src="<%=iconsPath%>/util/icons/component/<c:out value="${component.name}" />Small.gif" class="component-icon" alt=""/></a>
                  </td>
                  <td align="left">
                    <a href="CreateInstance?ComponentNum=<c:out value="${loop.index}" />" title="<c:out value="${component.description}" />"><c:out value="${component.label}" /></a>
                  </td>
                </tr>
              </c:if>
            </c:forEach>
          </table>
        </view:board>
        <br /><br />
        <%
          ButtonPane buttonPane = gef.getButtonPane();
          buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"),
              "javascript:window.close();", false));
          out.println(buttonPane.print());
        %>
      </center>
    </view:frame>
  </view:window>
</body>
</html>