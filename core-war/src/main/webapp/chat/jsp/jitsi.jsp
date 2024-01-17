<%@ page import="org.silverpeas.core.chat.ChatSettings" %>
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.util.file.FileServerUtils" %><%--
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="origin" value='<%=request.getAttribute("ownVisioUrl").toString().replaceFirst("(https?://[^/]+)(.*)", "$1")%>'/>
<c:set var="domain" value="${requestScope.domain}"/>
<c:set var="jwt" value="${requestScope.jwt}"/>
<c:set var="roomId" value="${requestScope.roomId}"/>
<c:set var="userName" value="${requestScope.userName}"/>
<c:set var="userAvatarUrl" value="${requestScope.userAvatarUrl}"/>
<c:set var="isMobile" value="${fn:contains(header['User-Agent'], 'Android') or fn:contains(header['User-Agent'], 'iPhone')}"/>
<view:sp-page>
  <view:sp-head-part noLookAndFeel="true">
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <script type="text/javascript" src="https://${domain}/external_api.js"></script>
    <style>
      html,
      body {
        width: 100%;
        height: 100%;
      }

      body {
        margin: 0;
        padding: 0;
        border: none;
        overflow: hidden;
      }
    </style>
  </view:sp-head-part>
  <view:sp-body-part>
    <script type="text/javascript">
      const domain = '${domain}';
      const options = {
        configOverwrite : {
          requireDisplayName : true,
          prejoinPageEnabled : true,
          disableDeepLinking : true
        },
        roomName: '${roomId}',
        jwt: '${jwt}',
        userInfo: {
          displayName: '${userName}'
        }
      };
      const api = new JitsiMeetExternalAPI(domain, options);
      api.addListener('browserSupport', function() {
        api.executeCommand('avatarUrl', '${userAvatarUrl}');
      });
      api.addListener('readyToClose', function() {
        window.postMessage(JSON.stringify({'jitsimeet_event' : 'close'}), "${origin}");
        <c:if test="${isMobile}">
        window.close();
        </c:if>
      });
    </script>
  </view:sp-body-part>
</view:sp-page>
