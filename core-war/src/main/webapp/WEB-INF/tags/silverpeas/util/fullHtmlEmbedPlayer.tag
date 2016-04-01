<%@ tag import="org.silverpeas.core.util.URLUtil" %>
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

<%-- Attributes --%>
<%@ attribute name="type" required="true"
              type="java.lang.String"
              description="Indicates here the type of the media ('video' or 'audio')." %>
<c:set var="type" value="${fn:toLowerCase(type) eq 'video' ? 'video': 'audio'}"/>

<%@ attribute name="mimeType" required="true"
              type="java.lang.String"
              description="Indicates here the mime type of the media." %>

<%@ attribute name="url" required="true"
              type="java.lang.String"
              description="Indicates here the URL of the media." %>

<%@ attribute name="posterUrl" required="true"
              type="java.lang.String"
              description="Indicates here the poster URL of the media." %>

<%@ attribute name="definition" required="true"
              type="org.silverpeas.core.io.media.Definition"
              description="Indicates here the definition of the media." %>
<c:set var="definitionRatio" value="${definition.height / definition.width}"/>

<%@ attribute name="backgroundColor" required="false"
              type="java.lang.String"
              description="Indicates here the color of the background of the player." %>

<%@ attribute name="autoPlay" required="false"
              type="java.lang.Boolean"
              description="Indicates here if the paly of the media must start automatically." %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Silverpeas Embed Player</title>
  <!-- optimize mobile versions -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

  <link href="<%=URLUtil.appendVersion(URLUtil.getApplicationURL() + "/util/styleSheets/globalSP_SilverpeasV5.css")%>" rel="stylesheet">
  <script src="<%=URLUtil.appendVersion(URLUtil.getApplicationURL() + "/util/javaScript/flowplayer/swfobject.js")%>"></script>
  <view:includePlugin name="jquery"/>
  <view:includePlugin name="${type}Player"/>
  <style type="text/css">
    body {
      border: none;
      margin: 0;
      padding: 0;
      background-color: transparent;
    }

    html, body, #audioBeforeContainer {
      margin: 0;
      padding: 0;
      background-color: '${backgroundColor}';
      text-align: center;
      vertical-align: middle;
      display: table-cell;
    }

    body > #audioBeforeContainer {
      height: auto;
      min-height: 100%;
    }

    #audioContainer {
      padding: 0;
      clear: both;
      position: relative;
      height: 100px;
      margin: -100px 0 0 0;
    }

    audio {
      height: 100px;
      width: 100%;
    }
  </style>

  <script type="text/javascript">
    var context = {
      useFlash : false
    };
    $(document).ready(function() {
      var $window = $(window);
      var isHtml5NotSupported = !document.createElement('${type}').canPlayType ||
          !document.createElement('${type}').canPlayType('${mimeType}');
      context.useFlash = isHtml5NotSupported || swfobject.getFlashPlayerVersion().major;
      <c:choose>
      <c:when test="${type eq 'video'}">
      var $videoContainer = jQuery('#videoContainer');
      if (context.useFlash) {
        var $flashVideoContainer = $('<div>', {
          'id' : 'flashVideoContainer'
        });
        $videoContainer.replaceWith($flashVideoContainer);
        $flashVideoContainer.videoPlayer({
          container : {
            width : $window.width() + 'px',
            height : $window.height() + 'px'
          },
          clip : {
            url : '${silfn:escapeJs(url)}',
            posterUrl : '${silfn:escapeJs(posterUrl)}',
            mimeType : '${silfn:escapeJs(mimeType)}'
            <c:if test="${not empty autoPlay and autoPlay}">,
            autoPlay : ${autoPlay}
            </c:if>
          }
          <c:if test="${not empty backgroundColor}">,
          canvas : {
            backgroundColor : '${backgroundColor}'
          }
          </c:if>
        });
      } else {
        // HTML5 video
        $videoContainer.css('width', $window.width());
        $videoContainer.css('height', $window.height());
        $videoContainer.show();
      }
      </c:when>
      <c:when test="${type eq 'audio'}">
      var $audioBeforeContainer = jQuery('#audioBeforeContainer');
      var $audioContainer = jQuery('#audioContainer');
      if (context.useFlash) {
        $audioBeforeContainer.remove();
        var $flashAudioContainer = $('<div>', {
          'id' : 'flashAudioContainer'
        });
        $audioContainer.replaceWith($flashAudioContainer);
        $flashAudioContainer.audioPlayer({
          container : {
            width : $window.width() + 'px',
            height : $window.height() + 'px'
          },
          clip : {
            url : '${silfn:escapeJs(url)}.mp3',
            posterUrl : '${silfn:escapeJs(posterUrl)}',
            mimeType : '${silfn:escapeJs(mimeType)}'
            <c:if test="${not empty autoPlay and autoPlay}">,
            autoPlay : ${autoPlay}
            </c:if>
          }
          <c:if test="${not empty backgroundColor}">,
          canvas : {
            backgroundColor : '${backgroundColor}'
          }
          </c:if>
        });
      } else {
        // HTML5 audio + mp3 support
        $audioBeforeContainer.css('width', $window.width());
        $audioBeforeContainer.css('height', $window.height());
        $audioBeforeContainer.show();
        $audioContainer.show();
      }
      </c:when>
      </c:choose>
    });
  </script>
</head>
<body>
<c:choose>
  <c:when test="${type eq 'video'}">
    <div id="videoContainer" class="minimalist" data-ratio="${definitionRatio}">
      <video preload="auto" poster="${posterUrl}" controls>
        <source type="${mimeType}" src="${url}"/>
      </video>
    </div>
  </c:when>
  <c:when test="${type eq 'audio'}">
    <div id="audioBeforeContainer" style="display: none">
      <span><img src="${posterUrl}"></span>
    </div>
    <div id="audioContainer" style="display: none">
      <audio preload="auto" controls>
      <source type="${mimeType}" src="${url}"/>
      </audio>
    </div>
  </c:when>
</c:choose>
</body>
</html>