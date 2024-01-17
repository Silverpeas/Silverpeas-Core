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
  ~ FLOSS exception. You should have received a copy of the text describing
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

<%@ tag import="org.silverpeas.core.chat.ChatLocalizationProvider" %>
<%@ tag import="org.silverpeas.core.admin.user.model.User" %>
<%@ tag import="org.silverpeas.core.chat.servers.ChatServer" %>
<%@ tag import="org.silverpeas.core.chat.ChatUser" %>
<%@ tag import="org.silverpeas.core.util.file.FileServerUtils" %>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="chatUser" value="<%=ChatUser.getCurrentRequester()%>"/>
<jsp:useBean id="chatUser" type="org.silverpeas.core.chat.ChatUser"/>
<c:set var="chatSettings" value="<%=ChatServer.getChatSettings()%>"/>
<jsp:useBean id="chatSettings" type="org.silverpeas.core.chat.ChatSettings"/>
<c:set var="chatBundle" value="<%=ChatLocalizationProvider.getLocalizationBundle(
          User.getCurrentRequester().getUserPreferences().getLanguage())%>"/>
<jsp:useBean id="chatBundle" type="org.silverpeas.core.util.LocalizationBundle"/>

<c:set var="chatUrl" value="${chatSettings.BOSHServiceUrl}"/>
<c:set var="chatWsUrl" value="${chatSettings.websocketServiceUrl}"/>
<c:set var="chatIceServer" value="${chatSettings.ICEServer}"/>
<c:set var="chatACL" value="${chatSettings.ACL}"/>
<c:set var="aclGroupsAllowedToCreate" value="${chatACL.aclOnGroupChat.groupsAllowedToCreate}"/>

<script type="text/javascript">
  (function() {
    whenSilverpeasReady(function() {
      spLayout.getHeader().addEventListener('load', function() {
        <c:choose>
        <c:when test="${sessionScope.get('Silverpeas.Chat') and chatUser.registered}">
        window.USERSESSION_PROMISE.then(function() {
          const chatOptions = {
            viewMode : 'overlayed',
            url : '${chatUrl}',
            wsUrl : '${chatWsUrl}',
            jid : '${chatUser.chatLogin}@${chatUser.chatDomain}',
            vcard : {
              'fn' : '${silfn:escapeJs(chatUser.displayedName)}'
            },
            id : '${chatUser.chatLogin}',
            password : '${chatUser.chatPassword}',
            domain : '${chatUser.chatDomain}',
            <c:if test="${not empty chatIceServer}">
            ice : {
              server : '${chatIceServer}',
              auth : true
            },
            </c:if>
            acl : {
              groupchat : {
                creation : ${empty aclGroupsAllowedToCreate or chatUser.isAtLeastInOneGroup(aclGroupsAllowedToCreate)}
              }
            },
            language : currentUser.language,
            avatar : webContext + '/display/avatar/60x/',
            userAvatarUrl : webContext + '/<%=FileServerUtils.getImageURL(chatUser.getAvatar(), "60x60")%>',
            notificationLogo : (window.SilverChatSettings ? window.SilverChatSettings.get('un.d.i.u') : ''),
            nbMsgMaxCachedPerRoom : ${chatSettings.maxCachedMsgThresholdPerRoom},
            clearMessagesOnReconnection : ${chatSettings.clearMessagesOnReconnection()},
            replyToEnabled : ${chatSettings.replyToEnabled},
            reactionToEnabled : ${chatSettings.reactionToEnabled},
            visioEnabled : ${chatSettings.visioEnabled},
            screencastEnabled : ${chatSettings.screencastEnabled},
            debug : false,
            selectUser : function(openChatWith) {
              $('#userId').off('change').on('change', function() {
                var id = $(this).val();
                if (id && id !== '${chatUser.id}') {
                  User.get(id).then(function(user) {
                    if (user) {
                      openChatWith(user.chatId, user.fullName);
                    }
                  });
                }
              });
              SP_openUserPanel(webContext + '/chat/users/select', '', 'menubar=no,scrollbars=no,statusbar=no');
            }
          };
          if (typeof SilverChat === 'undefined') {
            sp.log.warning('${silfn:escapeJs(chatBundle.getString("chat.client.notAvailable"))}');
            return;
          }
          SilverChat.init(chatOptions).start();
          spUserSession.addLogoutPromise(new Promise(function(resolve, reject) {
            spUserSession.addEventListener('current-user-logout', function() {
              spProgressMessage.show();
              SilverChat.stop().then(function() {
                resolve();
              }, function() {
                reject();
              });
            }, 'silverchat-user-logout');
          }));
        });
        </c:when>
        <c:otherwise>
        sp.log.warning('${silfn:escapeJs(chatBundle.getString("chat.server.notAvailable"))}');
        </c:otherwise>
        </c:choose>
      }, 'silverchat-initialization');
    });
  })();
</script>
<form id="chat_selected_user">
  <input type="hidden" name="userId" id="userId"/>
  <input type="hidden" name="userName" id="userName"/>
</form>
