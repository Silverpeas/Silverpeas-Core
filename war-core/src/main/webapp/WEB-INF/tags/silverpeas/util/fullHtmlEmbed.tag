<%--
  Copyright (C) 2000 - 2014 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Fragments--%>
<%@ attribute name="playerJsParameters" required="true"
              fragment="true"
              description="See silverpeas-player.js to analyse the parameters structure ... It just asking to build the object : '{...}'" %>

<%-- Attributes --%>
<%@ attribute name="type" required="true"
              type="java.lang.String"
              description="Indicates here the type of the media ('video' or 'audio')." %>
<c:set var="type" value="${fn:toLowerCase(type)}"/>

<%@ attribute name="mimeType" required="true"
              type="java.lang.String"
              description="Indicates here the mime type of the media." %>

<%@ attribute name="url" required="true"
              type="java.lang.String"
              description="Indicates here the URL of the media." %>

<html>
<head>
  <title></title>
  <view:includePlugin name="jquery"/>
  <view:includePlugin name="player"/>
  <script type="text/javascript">
    document.createElement('video');
    document.createElement('audio');
    document.createElement('track');
    var playerParameters = <jsp:invoke fragment="playerJsParameters"/>;
    $(document).ready(function() {
      $("#jquery_jplayer_1").player(playerParameters);
    });
  </script>
</head>
<body>
<c:choose>
  <c:when test="${type eq 'video'}">
    <div id="jp_container_1" class="jp-video ">
      <div class="jp-type-single">
        <div id="jquery_jplayer_1" class="jp-jplayer"></div>
        <div class="jp-gui">
          <div class="jp-video-play">
            <a href="javascript:;" class="jp-video-play-icon" tabindex="1">play</a>
          </div>
          <div class="jp-interface">
            <div class="jp-progress">
              <div class="jp-seek-bar">
                <div class="jp-play-bar"></div>
              </div>
            </div>
            <div class="jp-current-time"></div>
            <div class="jp-duration"></div>
            <div class="jp-controls-holder">
              <ul class="jp-controls">
                <li><a href="javascript:;" class="jp-play" tabindex="1">play</a></li>
                <li><a href="javascript:;" class="jp-pause" tabindex="1">pause</a></li>
                <li><a href="javascript:;" class="jp-stop" tabindex="1">stop</a></li>
                <li><a href="javascript:;" class="jp-mute" tabindex="1" title="mute">mute</a></li>
                <li><a href="javascript:;" class="jp-unmute" tabindex="1" title="unmute">unmute</a>
                </li>
                <li><a href="javascript:;" class="jp-volume-max" tabindex="1" title="max volume">max
                  volume</a></li>
              </ul>
              <div class="jp-volume-bar">
                <div class="jp-volume-bar-value"></div>
              </div>
              <ul class="jp-toggles">
                <li><a href="javascript:;" class="jp-full-screen" tabindex="1" title="full screen">full
                  screen</a></li>
                <li>
                  <a href="javascript:;" class="jp-restore-screen" tabindex="1" title="restore screen">restore
                    screen</a></li>
                <li><a href="javascript:;" class="jp-repeat" tabindex="1" title="repeat">repeat</a>
                </li>
                <li><a href="javascript:;" class="jp-repeat-off" tabindex="1" title="repeat off">repeat
                  off</a></li>
              </ul>
            </div>
            <div class="jp-details">
              <ul>
                <li><span class="jp-title"></span></li>
              </ul>
            </div>
          </div>
        </div>
        <div class="jp-no-solution">
          <span>Update Required</span>
          To play the media you will need to either update your browser to a recent version or
          update your <a href="http://get.adobe.com/flashplayer/" target="_blank">Flash plugin</a>.
        </div>
      </div>
    </div>
  </c:when>
  <c:when test="${type eq 'audio'}">
    <div id="jquery_jplayer_1" class="jp-jplayer"></div>
    <div id="jp_container_1" class="jp-audio">
      <div class="jp-type-single">
        <div class="jp-gui jp-interface">
          <ul class="jp-controls">
            <li><a href="javascript:;" class="jp-play" tabindex="1">play</a></li>
            <li><a href="javascript:;" class="jp-pause" tabindex="1">pause</a></li>
            <li><a href="javascript:;" class="jp-stop" tabindex="1">stop</a></li>
            <li><a href="javascript:;" class="jp-mute" tabindex="1" title="mute">mute</a></li>
            <li><a href="javascript:;" class="jp-unmute" tabindex="1" title="unmute">unmute</a></li>
            <li><a href="javascript:;" class="jp-volume-max" tabindex="1" title="max volume">max
              volume</a></li>
          </ul>
          <div class="jp-progress">
            <div class="jp-seek-bar">
              <div class="jp-play-bar"></div>
            </div>
          </div>
          <div class="jp-volume-bar">
            <div class="jp-volume-bar-value"></div>
          </div>
          <div class="jp-time-holder">
            <div class="jp-current-time"></div>
            <div class="jp-duration"></div>
            <ul class="jp-toggles">
              <li>
                <a href="javascript:;" class="jp-repeat" tabindex="1" title="repeat">repeat</a>
              </li>
              <li>
                <a href="javascript:;" class="jp-repeat-off" tabindex="1" title="repeat off">repeat
                  off</a>
              </li>
            </ul>
          </div>
        </div>
        <div class="jp-details">
          <ul>
            <li><span class="jp-title"></span></li>
          </ul>
        </div>
        <div class="jp-no-solution">
          <span>Update Required</span>
          To play the media you will need to either update your browser to a recent version or
          update your <a href="http://get.adobe.com/flashplayer/" target="_blank">Flash plugin</a>.
        </div>
      </div>
    </div>
  </c:when>
</c:choose>
</body>
</html>