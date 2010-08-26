<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<%@ include file="graphicBox.jsp" %>
<%@ include file="checkSilvermail.jsp" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController" value="${requestScope.SILVERMAIL}" />
<c:set var="from" value="${param.from}" />
<c:set var="msg" value="${sessionController.currentMessage}"/>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%
      response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
      response.setHeader("Pragma", "no-cache"); //HTTP 1.0
      response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.silverpeas.notificationserver.channel.silvermail.SILVERMAILMessage"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="java.util.Date"%>
<html>
  <head>
    <title>___/ Silverpeas - Corporate Portal Organizer
      \________________________________________________________________________</title>
      <view:looknfeel />
    <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>

    <script>
      function deleteMessage( pID )
      {
        window.opener.location = "DeleteMessage.jsp?ID=" + pID + "&SpaceId=<c:out value="${param.SpaceId}"/>&from=<c:out value="${from}"/>";
        window.close();
      }

      function goTo()
      {
        window.opener.top.location = "<c:out value="${msg.url}" />";
        window.close();
      }

      function closeWindow()
      {
      <c:choose>
        <c:when test="${'homePage' eq from}">window.opener.location.reload();</c:when>
        <c:otherwise>window.opener.location = "Main.jsp";</c:otherwise>
      </c:choose>
          window.close();
        }
    </script>
  </head>
  <body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5">
    <fmt:message key="silverMail" var="browseLabel" />
    <view:browseBar>
      <view:browseBarElt link="" label="${browseLabel}" />
      <view:browseBarElt link="" label="${msg.subject}" />
    </view:browseBar>
    <view:window>
      <view:frame>
        <center>
          <table cellpadding="2" cellspacing="0" border="0" width="98%" class="intfdcolor">
            <tr>
              <td class="intfdcolor4" NOWRAP>
                <form name="silvermailForm" Action="" Method="POST">
                  <table cellpadding="5" cellspacing="0" border="0" width="100%">
                    <tr>
                      <td valign="baseline" align=left class="txtlibform"><fmt:message key="date" />:&nbsp;</td>
                      <td align=left valign="baseline"><fmt:formatDate value="${msg.date}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                    </tr>
                    <tr>
                      <td valign="baseline" align=left class="txtlibform"><fmt:message key="source" /> :&nbsp;</td>
                      <td align=left valign="baseline"><c:out value="${msg.source}" /></td>
                    </tr>
                    <tr>
                      <td valign="baseline" align=left  class="txtlibform"><fmt:message key="from" /> :&nbsp;</td>
                      <td align=left valign="baseline"><c:out value="${msg.senderName}" /></td>
                    </tr>
                    <tr>
                      <td valign="baseline" align=left  class="txtlibform"><fmt:message key="url" /> :&nbsp;</td>
                      <td align=left valign="baseline">
                        <c:if test="${!empty msg.url}">
                          <fmt:message key="silvermail.link" bundle="${icons}" var="icon_url" />
                          <a href="javaScript:goTo();"><img src="<c:url value="${icon_url}"/>" border="0"/></a>
                          </c:if>
                      </td>
                    </tr>
                    <tr>
                      <td valign="baseline" align=left  class="txtlibform"><fmt:message key="subject" /> :&nbsp;</td>
                      <td align=left valign="baseline"><c:out value="${msg.subject}" /></td>
                    </tr>
                    <tr>
                      <td valign="baseline" align=left  class="txtlibform"></td>
                      <td align=left valign="baseline"><c:out value="${msg.body}" escapeXml="false" /></td>
                    </tr>
                  </table>
                </form>
              </td>
            </tr>
          </table><table cellpadding="2" cellspacing="0" border="0"><tr><td><img src="<c:url value="/util/icons/colorPix/1px.gif" />"/></td></tr></table>
                <fmt:message var="closeLabel" key="close" />
                <c:set var="deleteAction">javascript:onClick=deleteMessage(<c:out value="${sessionController.currentMessageId}"/>);</c:set>
          <fmt:message var="deleteLabel" key="delete" />
          <table cellpadding="2" cellspacing="0" border="0"><tr><td>
                <view:button label="${deleteLabel}" action="${deleteAction}"/></td>
              <td><view:button label="${closeLabel}" action="javascript:onClick=closeWindow();"/></td></tr></table>
        </center>
      </view:frame>
    </view:window>
  </body>
</html>