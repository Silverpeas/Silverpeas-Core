<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:set var="settings" value="${lookHelper.tickerSettings}"/>

<script type="text/javascript">
function __getTime(news) {
  var date = new Date(news.date);
  var hour = date.getHours();
  var minute = date.getMinutes();
  if (minute < 10) {
    minute = "0" + minute;
  }
  var nbDays = news.publishedForNbDays;
  var label = hour + ":" + minute;
  if (nbDays == 1) {
    label = TickerBundle.get('lookSilverpeasV5.ticker.date.yesterday') + " " + label;
  } else if (nbDays > 1) {
    label = TickerBundle.get('lookSilverpeasV5.ticker.date.daysAgo', nbDays);
  }
  return label;
}

function __notifyUser(news) {
  spUserNotification.notifyOnDesktop(news.title, {
    body: news.description,
    tag: news.publicationId,
    icon: webContext + '/util/icons/component/quickinfoBig.png'
  }, function(desktopNotification) {
    desktopNotification.onclick = function() {
      top.location.href = webContext + '/Publication/' + desktopNotification.tag;
      // do not work with Chrome
      window.focus();
    };
  });
}

function __isNewNews(id) {
  var index;
  var found = false;
  for (index = 0; index < tickerNews.length && !found; ++index) {
     found = tickerNews[index] === id;
  }
  return !found;
}

var tickerNews = [];

(function($) {
  function worker() {
    $.ajax({
      url : webContext + '/services/news/ticker?limit='+${settings.displayLimit},
      type : "GET",
      dataType : "json",
      cache : false,
      success : function(data) {
        var nbNews = data.length;
        $("#sp-ticker").empty();
        if (nbNews === 0) {
          $("#ticker").hide();
        } else {
          $("#ticker").show();
          var ul = $("<ul>").appendTo("#sp-ticker");
          ul.attr("id", "js-news").attr("class", "js-hidden");
          var firstLoad = (tickerNews.length == 0);
          $.each(data, function(i, item) {
            if (firstLoad) {
              tickerNews[tickerNews.length] = item.id;
            } else {
              if (__isNewNews(item.id)) {
                __notifyUser(item);
                tickerNews[tickerNews.length] = item.id;
              }
            }
            var li = $("<li>").attr("class", "news-item").appendTo("#js-news");
            var date = $("<span>").attr("class", "ticker-item-date").text(__getTime(item) + " - ");
            date.appendTo(li);
            var span = $("<span>").attr("title", item.description);
            var newsLabel = $("<span>").attr("class", "ticker-item-title").text(item.title);
            span.append(newsLabel);
            if (${settings.descriptionDisplayed}) {
              var newsDesc = $("<span>").attr("class", "ticker-item-description").text(item.description);
              span.append(newsDesc);
            }
            if (${settings.linkOnItem}) {
              var a = $("<a>").addClass("sp-permalink").attr("href", item.permalink).attr("target", "_top");
              span.appendTo(a);
              a.appendTo(li);
            } else {
              span.appendTo(li);
            }
          });

          //init the ticker plugin itself
          $('#js-news').ticker({
            speed : ${settings.getParam('speed', '0.10')},
            pauseOnItems : ${settings.getParam('pauseOnItems', '5000')},
            htmlFeed : true,
            controls : ${settings.getParam('controls', 'false')},
            displayType : '${settings.getParam('displayType', 'fade')}',
            fadeInSpeed : ${settings.getParam('fadeInSpeed', '600')},
            fadeOutSpeed : ${settings.getParam('fadeOutSpeed', '300')},
            titleText : '${settings.label}'
          });
        }

        // Schedule the next request when the current one's complete
        var refreshDelay = ${settings.refreshDelay}*1000;
        setTimeout(worker, refreshDelay);
      }
    });
  }

  var _tickerPromiseDepencies = [window.TICKER_PROMISE, window.USERNOTIFICATION_PROMISE];
  sp.promise.whenAllResolved(_tickerPromiseDepencies).then(function() {
    $(document).ready(function() {
      var __removeDesktopNotificationUserRequest = function() {
        $("#desktop-notifications-permission").remove();
      };
      if (spUserNotification.desktopPermissionAvailable() &&
          !spUserNotification.desktopPermissionAuthorized() &&
          !spUserNotification.desktopPermissionDenied()) {
        $("#desktop-notifications-permission").text(TickerBundle.get("lookSilverpeasV5.ticker.notifications.permission.request"));
        $("#desktop-notifications-permission").show();
        spUserNotification.addEventListener('desktopNotificationPermissionAccepted', __removeDesktopNotificationUserRequest);
        spUserNotification.addEventListener('desktopNotificationPermissionDenied', __removeDesktopNotificationUserRequest);
      } else {
        __removeDesktopNotificationUserRequest();
      }
    });
    worker();
  });
})(jQuery);
</script>
<div id="ticker" style="display: none">
  <span id="sp-ticker"></span>
  <a href="#" style="display: none;" onclick="spUserNotification.requestDesktopPermission()" id="desktop-notifications-permission"></a>
</div>