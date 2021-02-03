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
<%@ page import="org.silverpeas.core.admin.user.model.User" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.wopi.WopiSettings" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="origin" value="<%=URLUtil.getServerURL(request)%>"/>
<c:set var="wopiClientUrl" value="${requestScope.WopiClientUrl}"/>
<c:set var="currentUser" value="<%=User.getCurrentRequester()%>"/>
<c:set var="wopiUser" value="${requestScope.WopiUser}"/>
<jsp:useBean id="wopiUser" type="org.silverpeas.core.wopi.WopiUser"/>
<c:set var="wopiFile" value="${requestScope.WopiFile}"/>
<jsp:useBean id="wopiFile" type="org.silverpeas.core.wopi.WopiFile"/>
<c:set var="hideTrackChanges" value="${not(currentUser.accessAdmin or wopiFile.owner().id eq currentUser.id)}"/>
<c:set var="uiDefaults" value="<%=WopiSettings.getUIDefaults()%>"/>

<view:sp-page>
  <view:sp-head-part minimalSilverpeasScriptEnv="true">
    <meta charset="utf-8">
    <%-- Enable IE Standards mode --%>
    <meta http-equiv="x-ua-compatible" content="ie=edge">
    <meta name="viewport"
          content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no">

    <style type="text/css">
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
        overflow: hidden;
        -ms-content-zooming: none;
      }

      iframe {
        display: block;
        border: none;
        width: 100%;
        height: 100%;
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        margin: 0;
      }
    </style>
  </view:sp-head-part>
  <view:sp-body-part>
    <div id="wopicontainer"></div>
    <form name="wopiform" method="post" action="${wopiClientUrl}" target="wopiframe">
    <c:if test="${uiDefaults.present}">
      <input name="${uiDefaults.get().first}" value="${uiDefaults.get().second}" type="hidden">
    </c:if>
    </form>
    <script type="text/javascript">
      (function() {
        let $container = document.querySelector('#wopicontainer');
        let $iframe = document.createElement('iframe');
        $iframe.name = 'wopiframe';
        $iframe.id = 'wopiframe';
        $iframe.title = 'Online Editing';
        // $iframe.setAttribute('allowfullscreen', 'true');
        // $iframe.setAttribute('sandbox', 'allow-scripts allow-same-origin allow-forms allow-modals allow-popups allow-top-navigation allow-popups-to-escape-sandbox');
        $container.appendChild($iframe);

        window.addEventListener("message", function (event) {
          if ('${origin}' === event.origin && event.data) {
            let data = JSON.parse(event.data);
            sp.log.debug('receive', data);
            let msgId = data['MessageId'];
            if ('App_LoadingStatus' === msgId) {
              ClientMessageManager.post('Host_PostmessageReady');
              ClientMessageManager.post('Hide_Menu_Item', {id : 'signdocument'});
              <c:if test="${hideTrackChanges}">
              ClientMessageManager.post('Hide_Menu_Item', {id : 'changesmenu'});
              </c:if>
            } else if ('Views_List' === msgId) {
              if (data.Values) {
                const userIds = data.Values
                    .map(function(view) {
                      return view['UserId'];
                    })
                    .filter(function(id) {
                      return id && id !== ''
                    })
                    .join(',');
                HostMessageManager.post('SP_CURRENT_USERS', {
                  'X-WOPI-ViewUserIds' : userIds
                });
              }
            } else if ('UI_Close' === msgId) {
              window.top.close();
            }
            return;
          }
          sp.log.warning("received an event from an unknown origin");
        }, false);

        let ClientMessageManager = new function() {
          let $window = $iframe.contentWindow;
          this.post = function(msgId, values) {
            let data = JSON.stringify({
              'MessageId' : msgId,
              'SendTime' : Date.now(),
              'Values' : values ? values : {}
            });
            sp.log.debug('send', data);
            $window.postMessage(data, '${origin}');
          }
        };

        let HostMessageManager = new function() {
          const baseUrl = webContext + '/services/wopi/files/${wopiFile.id()}';
          this.post = function(msgType, values) {
            let ajaxRequest = sp.ajaxRequest(baseUrl)
                .withHeader('X-WOPI-Override', msgType)
                .withHeader('access_token', '${wopiUser.accessToken}');
            if (typeof values ===  'object') {
              for (let key in values) {
                ajaxRequest.withHeader(key, values[key]);
              }
            }
            return ajaxRequest.byPostMethod().send();
          }
        };

        setTimeout(function() {
          document.wopiform.submit();
        }, 0);
      })();
    </script>
  </view:sp-body-part>
</view:sp-page>