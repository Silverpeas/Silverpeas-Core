<%--
  Copyright (C) 2000 - 2013 Silverpeas

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

<c:set var="lookHelper" value="${sessionScope['Silverpeas_LookHelper']}"/>
<c:set var="settings" value="${lookHelper.tickerSettings}"/>
<c:set var="language" value="${lookHelper.language}"/>

<script type="text/javascript">
function __getTime(news) {
  __loadI18NProp();
  var date = new Date(news.date);
  var hour = date.getHours();
  var minute = date.getMinutes();
  if (minute < 10) {
    minute = "0" + minute;
  }
  var nbDays = news.publishedForNbDays;
  var label = hour + ":" + minute;
  if (nbDays == 1) {
    label = __getI18NProp("lookSilverpeasV5.ticker.date.yesterday") + " " + label;
  } else if (nbDays > 1) {
    label = __getI18NProp("lookSilverpeasV5.ticker.date.daysAgo", [nbDays]);
  }
  return label;
}

var i18nProp = false;

function __loadI18NProp() {
  if (!i18nProp) {
    $.i18n.properties({
	  name: 'lookBundle',
	  path: webContext + '/services/bundles/org/silverpeas/lookSilverpeasV5/multilang/',
      language: '${language}',
      mode: 'map'
    });
    i18nProp = true;
  }
}

function __getI18NProp(prop, params) {
  __loadI18NProp();
  return $.i18n.prop(prop, params);
}

$(document).ready(function() {
  if (!("Notification" in window) || (Notification && Notification.permission === "granted")) {
    $("#desktop-notifications-permission").remove();
  } else {

    $("#desktop-notifications-permission").text(__getI18NProp("lookSilverpeasV5.ticker.notifications.permission.request"));
  }
});

function requestNotificationPermission() {
  if (Notification && Notification.permission !== "denied") {
    Notification.requestPermission(function (status) {
      if (Notification.permission !== status) {
        Notification.permission = status;
      }
      if (status === "granted") {
        $("#desktop-notifications-permission").remove();
      }
    })
  }
}

function __newsToNotification(news) {
  var notification = new Notification(news.title, {
    body: news.description,
    tag: news.publicationId,
    icon: webContext + '/util/icons/component/quickinfoBig.png'
  });
  notification.onclick = function() {
    top.location.href = webContext + '/Publication/' + notification.tag;
    window.focus();  // do not work with Chrome
  };
}

function __notifyUser(news) {
  // Check if browser supports notifications
  if (!("Notification" in window)) {
    //alert("Ce navigateur ne supporte pas les notifications desktop");
  } else if (Notification.permission === "granted") {
    // browser supports desktop notifications and user accepts it
    __newsToNotification(news);
  } else if (Notification.permission !== 'denied') {
    // notifications are not yet accepted or denied, ask permission to user
    Notification.requestPermission(function (permission) {
      // Whatever user decision, store it
      if(!('permission' in Notification)) {
        Notification.permission = permission;
      }
      // if user decides to accept notifications, send it...
      if (permission === "granted") {
        __newsToNotification(news);
      }
    });
  }
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

(function worker() {
  $.ajax({
    url: webContext+'/services/news/ticker',
    type: "GET",
	async : true,
	dataType : "json",
	cache : false,
    success: function(data) {
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
          var date = $("<span>").attr("class", "ticker-item-date").text(__getTime(item)+ " - ");
          date.appendTo(li);
          var span = $("<span>").attr("title", item.description).text(item.title);
          if (${settings.linkOnItem}) {
            var a = $("<a>").attr("href", item.permalink).attr("target", "_top");
            span.appendTo(a);
            a.appendTo(li);
          } else {
            span.appendTo(li);
          }
        });

        //init the ticker plugin itself
        $('#js-news').ticker({
	    speed: ${settings.getParam('speed', '0.10')},
	    pauseOnItems: ${settings.getParam('pauseOnItems', '5000')},
	    htmlFeed: true,
	    controls: ${settings.getParam('controls', 'false')},
	    displayType: '${settings.getParam('displayType', 'fade')}',
	    fadeInSpeed: ${settings.getParam('fadeInSpeed', '600')},
	    fadeOutSpeed: ${settings.getParam('fadeOutSpeed', '300')},
	    titleText: '${settings.label}'
		});
      }

	  // Schedule the next request when the current one's complete
	  var refreshDelay = ${settings.refreshDelay}*1000;
      setTimeout(worker, refreshDelay);
    }
  });
})();
</script>
<div id="ticker" style="display: none">
  <span id="sp-ticker">
  Chargement...
  </span>
  <a href="#" onclick="requestNotificationPermission()" id="desktop-notifications-permission"></a>
</div>