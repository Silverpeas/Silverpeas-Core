<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="context" value="${requestScope.context}"/>
<c:set var="isNodeResource" value="${requestScope.context.resource.type.name eq 'NODE'}"/>
<c:set var="instanceId" value="${context.resource.instanceId}"/>
<c:set var="resourceId" value="${context.resource.id}"/>

<fmt:message var="panelTitleLabel" key="subscription.panel.title"/>
<fmt:message var="forcedSubscriptionListLabel" key="subscription.panel.forced.list"/>
<fmt:message var="modifyForcedSubscriptionLabel" key="subscription.panel.forced.modify"/>
<fmt:message var="selfCreationSubscriptionListLabel" key="subscription.panel.selfCreation.list"/>
<fmt:message var="selfCreationSubscriptionListHelpLabel" key="subscription.panel.selfCreation.list.help"/>
<fmt:message key="subscription.panel.list.group" var="listOfOneGroup">
  <fmt:param value="${1}"/>
</fmt:message>
<fmt:message key="subscription.panel.list.group" var="listOfSeveralGroups">
  <fmt:param value="${2}"/>
</fmt:message>
<fmt:message key="subscription.panel.list.user" var="listOfOneUser">
  <fmt:param value="${1}"/>
</fmt:message>
<fmt:message key="subscription.panel.list.user" var="listOfSeveralUsers">
  <fmt:param value="${2}"/>
</fmt:message>
<fmt:message var="subscribedSinceLabel" key="subscription.panel.list.subscribed.since">
  <fmt:param value=""/>
</fmt:message>
<fmt:message var="unsubscribeLabel" key="subscription.panel.unsubscribe"/>
<fmt:message var="unsubscribeConfirmMessage" key="subscription.panel.unsubscribe.message.confirm">
  <fmt:param value="@name@"/>
</fmt:message>
<fmt:message var="unsubscribeSuccessMessage" key="subscription.panel.unsubscribe.message.success">
  <fmt:param value="@name@"/>
</fmt:message>
<fmt:message var="unsubscribeErrorMessage" key="subscription.panel.unsubscribe.message.error">
  <fmt:param value="@name@"/>
</fmt:message>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="pagination"/>
  <view:includePlugin name="datepicker"/>
  <view:includePlugin name="popup"/>
  <title><fmt:message key="subscription.panel.title"/></title>
  <style type="text/css">
    html, body {
      height: 100%;
      overflow: hidden;
      margin: 0;
      padding: 0
    }

    div.pageNav .pages_indication {
      display: none
    }
  </style>
  <script type="text/javascript" src="<c:url value='/util/javaScript/angularjs/services/silverpeas-profile.js'/>"></script>
  <script type="text/javascript">

    /**
     * Go to screen that proposing to select groups and users : User Panel.
     */
    function modifyForcedSubscriptions() {
      $("<form>").attr('method', 'GET').attr('action',
          '<c:url value="/RSubscription/jsp/ToUserPanel"/>').appendTo(document.body).submit();
    }

    /**
     * Subscription management JQuery plugin
     */
    (function($) {

      var context = {
        cache : [],
        SUB_TYPE : {
          FORCED_GROUP : 'forced_group_subscription',
          FORCED_USER : 'forced_user_subscription',
          SELF_CREATION_USER : 'selfCreation_user_subscription'
        },
        subscriptions : [],
        users : [],
        groups : [],
        dateFormat : '',
        unsubscribeStartUrl : ''
      };
      context.subscriptions[context.SUB_TYPE.FORCED_GROUP] = [];
      context.subscriptions[context.SUB_TYPE.FORCED_USER] = [];
      context.subscriptions[context.SUB_TYPE.SELF_CREATION_USER] = [];

      function __putInCache(key, value) {
        context[key] = value;
      }

      function __getFromCache(key) {
        return context[key];
      }

      $.subscription = function() {
        __loadSubscriptions();
      };

      function __loadSubscriptions() {

        // Date format
        var dateFormat = $.datepicker.regional['${requestScope.resources.language}'];
        if (dateFormat) {
          context.dateFormat = dateFormat.dateFormat;
        } else {
          context.dateFormat = $.datepicker.regional[''].dateFormat;
        }

        // Subscriptions
        var url = '<c:url value="/services/subscriptions/${instanceId}"/>';
        <c:if test="${isNodeResource}">
        url += '/${resourceId}';
        </c:if>
        $.ajax({
          url : url,
          type : 'GET',
          dataType : 'json',
          cache : false,
          success : function(subscriptions, status, jqXHR) {
            __renderSubscriptions(subscriptions);
          },
          error : function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });

        // Unscubscribe start URL
        context.unsubscribeStartUrl = '<c:url value="/services/unsubscribe/${instanceId}" />';
        <c:if test="${isNodeResource}">
        context.unsubscribeStartUrl += '/topic/${resourceId}';
        </c:if>
      }

      /**
       * Rendering all subscriptions.
       * @param subscriptions
       * @private
       */
      function __renderSubscriptions(subscriptions) {
        __classifySubscriptions(subscriptions).then(function() {
          var $forcedGroups = __renderSubscriptionBloc({
            subType : context.SUB_TYPE.FORCED_GROUP
          });
          var $forcedUsers = __renderSubscriptionBloc({
            subType : context.SUB_TYPE.FORCED_USER
          });
          var $manualUsers = __renderSubscriptionBloc({
            subType : context.SUB_TYPE.SELF_CREATION_USER
          });

          __paginate($('div.compulsory-subscription-management'), [$forcedGroups, $forcedUsers]);
          __paginate($('div.individual-subscription'), [$manualUsers]);
        });
      }

      /**
       * Classifying subscription for displaying.
       * Loading also of all user and group profiles.
       * @param subscriptions
       * @private
       */
      function __classifySubscriptions(subscriptions) {
        var userIds = [];
        var groupIds = [];
        $(subscriptions).each(function(index, subscription) {
          if (subscription.subscriber.group) {
            context.subscriptions[context.SUB_TYPE.FORCED_GROUP].push(subscription);
            groupIds.push(subscription.subscriber.id);
          } else if (subscription.subscriber.user) {
            if (subscription.forced) {
              context.subscriptions[context.SUB_TYPE.FORCED_USER].push(subscription);
            } else if (subscription.selfCreation) {
              context.subscriptions[context.SUB_TYPE.SELF_CREATION_USER].push(subscription);
            } else {
              return false;
            }
            userIds.push(subscription.subscriber.id);
          }
        });

        // the promise of the processing termination to chain the asynchronous computations together;
        // it is immediatly resolved as the computation of this method is synchrone.
        var termination =  AngularPromise.defer();
        termination.resolve(0);
        termination = termination.promise;

        // User profiles
        if (userIds.length > 0) {
          termination = termination.then(function() {
            return User.get({
              id : userIds
            }).then(function(users) {
              $(users).each(function(index, user) {
                context.users[user.id] = user;
              });
              // Agregate informations
              __agregatesData(context.subscriptions[context.SUB_TYPE.FORCED_USER], context.users);
              __agregatesData(context.subscriptions[context.SUB_TYPE.SELF_CREATION_USER], context.users);
            });
          });
        }

        // Group profiles
        if (groupIds.length > 0) {
          termination = termination.then(function() {
            return UserGroup.get({
              ids : groupIds
            }).then(function(groups) {
              $(groups).each(function(index, group) {
                context.groups[group.id] = group;
              });
              // Agregate informations
              __agregatesData(context.subscriptions[context.SUB_TYPE.FORCED_GROUP], context.groups);
            });
          });
        }

        return termination;
      }

      /**
       * Adding profile data on subscription object.
       * @param subscriptions
       * @param profiles
       * @private
       */
      function __agregatesData(subscriptions, profiles) {
        $(subscriptions).each(function(index, subscription) {
          var subscriber = profiles[subscription.subscriber.id];
          if (subscription.subscriber.group) {
            subscription.ui = {
              classSuffix : 'group',
              name : subscriber.name,
              avatarUrl : '<c:url value="/util/icons/component/groupe_Type_gestionCollaborative.png"/>'
            };
            subscription.ws = {
              subscriberType : 'group'
            };
          } else {
            subscription.ui = {
              classSuffix : 'user',
              name : subscriber.fullName,
              avatarUrl : subscriber.avatar
            };
            subscription.ws = {
              subscriberType : 'user'
            };
          }
        });

        // Sorting subscriptions by their names
        subscriptions.sort(__sortByName);
      }

      /**
       * Sort subscriptions by their names
       * @param a
       * @param b
       * @return {number}
       * @private
       */
      function __sortByName(a, b) {
        var aName = a.ui.name.toLowerCase();
        var bName = b.ui.name.toLowerCase();
        return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
      }

      /**
       * Rendering a bloc of subscriptions
       * @param [params] the parameters to consider when rendering bloc of subscriptions.
       * @param [params.subType] the type of the bloc of subscriptions.
       * @private
       */
      function __renderSubscriptionBloc(params) {
        var $bloc = $('#' + params.subType);
        var subscriptions = context.subscriptions[params.subType];
        if (subscriptions.length > 0) {

          // List
          var $list = __renderSubscriptionBlocList(params, $bloc, subscriptions);

          // Bloc title
          __renderSubscriptionBlocTitle(params);

          // Displaying
          $list.show();
        }
        return $bloc;
      }

      /**
       * Rendering a the title of a bloc of subscriptions
       * @param [params] the parameters to consider when rendering bloc of subscriptions.
       * @param [params.subType] the type of the bloc of subscriptions.
       * @private
       */
      function __renderSubscriptionBlocTitle(params) {
        var $bloc = $('#' + params.subType);
        var nbSubscriptions = $bloc.find('li[id^="' + params.subType + '"]').length;
        var blocTitle = (nbSubscriptions <= 1) ? '${listOfOneUser}' : '${listOfSeveralUsers}';
        if (params.subType == context.SUB_TYPE.FORCED_GROUP) {
          blocTitle = (nbSubscriptions <= 1) ? '${listOfOneGroup}' : '${listOfSeveralGroups}';
        }
        $bloc.find('.nb_results').text(blocTitle.replace(/[0-9]*/, nbSubscriptions));
        return nbSubscriptions;
      }

      /**
       * Rendering a bloc of subscriptions
       * @param [params] the parameters to consider when rendering bloc of subscriptions.
       * @param [params.subType] the type of the bloc of subscriptions.
       * @param $bloc the bloc to manage
       * @param subscriptions subscriptions to manage
       * @private
       */
      function __renderSubscriptionBlocList(params, $bloc, subscriptions) {
        var $list = $bloc.find('#' + params.subType + '_list');
        $(subscriptions).each(function(index, subscription) {
          $list.append(__renderSubscription(params, index, subscription));
        });
        return $list;
      }

      /**
       * Rendering a subscription
       * @param [params] the parameters to consider when rendering bloc of subscriptions.
       * @param [params.subType] the type of the bloc of subscriptions.
       * @param index for odd or even classes
       * @param subscription subscription to manage
       * @private
       */
      function __renderSubscription(params, index, subscription) {

        // Subscriber
        var subscriber;
        if (params.subType == context.SUB_TYPE.FORCED_GROUP) {
          subscriber = context.groups[subscription.subscriber.id];
        } else {
          subscriber = context.users[subscription.subscriber.id];
        }

        // If the user is not found, then the treatment is stopped
        if (!subscriber) {
          return null;
        }

        // Initializing the subscription
        var $subscription = $('<li>').attr('id',
                params.subType + '_' + subscription.subscriber.id).addClass('line').addClass(
                ((index % 2) == 0) ? 'odd' : 'even');

        // Avatar
        var $avatar = $('<div>').addClass('avatar').append($('<img>').attr('src',
            subscription.ui.avatarUrl));
        $subscription.append($avatar);

        // Name
        $subscription.append($('<span>').addClass('name_' +
            subscription.ui.classSuffix).text(subscription.ui.name));

        // Details
        if (subscription.creationDate) {
          $subscription.append($('<span>').addClass('subscription-date').text('${subscribedSinceLabel} ' +
              __formatDate(subscription.creationDate)));
        }

        // Unsubscribe
        if (params.subType == context.SUB_TYPE.FORCED_GROUP ||
            params.subType == context.SUB_TYPE.FORCED_USER ||
            (params.subType == context.SUB_TYPE.SELF_CREATION_USER &&
                subscription.subscriber.id == '${currentUserId}')) {
          var unsuncribeAction = $('<a>');
          $subscription.append(unsuncribeAction.attr('title',
                  '${unsubscribeLabel}').addClass('action unsubscribe').text('${unsubscribeLabel}').click(function() {
                __confirmSubscriptionDeletion(subscription, function() {
                  $.ajax({
                    url : context.unsubscribeStartUrl + '/' + subscription.ws.subscriberType + '/' +
                        subscription.subscriber.id,
                    type : 'POST',
                    dataType : 'json',
                    cache : false,
                    contentType : "application/json",
                    success : function(subscriptions, status, jqXHR) {
                      unsuncribeAction.remove();
                      notySuccess("${unsubscribeSuccessMessage}".replace('@name@',
                          subscription.ui.name));
                      $subscription.attr('id', '');
                      $subscription.addClass('unsubscribed');
                      __renderSubscriptionBlocTitle(params);
                    },
                    error : function(jqXHR, textStatus, errorThrown) {
                      window.console &&
                      window.console.log(('Silverpeas Subscription Management - ERROR - ' +
                          errorThrown ));
                      notyError("${unsubscribeErrorMessage}".replace('@name@',
                          subscription.ui.name));
                    }
                  });
                  return true;
                });
              }));
        }

        // Return the built subscription.
        return $subscription;
      }

      /**
       * Format a date represented by a long into a readable date that takes account of the user language.
       * @param longDate
       * @private
       */
      function __formatDate(longDate) {
        return $.datepicker.formatDate(context.dateFormat, new Date(longDate));
      }

      /**
       * Paginate a container that takes all the height of the screen.
       * @param $mainContainer
       * @param blocs
       * @private
       */
      function __paginate($mainContainer, blocs) {

        var notEmptyBlocs = [];

        // Compute the size of blocs
        var sizeOfBlocs = 0;
        $(blocs).each(function(index, $bloc) {
          var currentHeight = $bloc.height();
          if (currentHeight > 20) {
            notEmptyBlocs.push($bloc);
          }
          sizeOfBlocs += currentHeight;
        });

        // Sorting blocs by their height
        notEmptyBlocs.sort(function($blocA, $blocB) {
          var aHeight = $blocA.height();
          var bHeight = $blocB.height();
          return ((aHeight < bHeight) ? -1 : ((aHeight > bHeight) ? 1 : 0));
        });

        // Compute the available heigth for one bloc
        var availableBlocHeight = parseInt(($(window).height() -
            ($mainContainer.height() - sizeOfBlocs) - 75) / notEmptyBlocs.length);

        // Paginate each bloc
        $(notEmptyBlocs).each(function(index, $bloc) {
          var blocHeight = $bloc.height();
          var remainingBlocHeight = availableBlocHeight - blocHeight;
          if (remainingBlocHeight <= 0) {
            var blocTitleHeight = $('[id$="_result_count"]', $bloc).height();
            var $pagination = $('[id$="_list_pagination"]', $bloc).show();
            var paginationHeight = $('[id$="_list_pagination"]', $bloc).height();
            var $rows = $('li', $bloc);
            var rowHeight = $rows.outerHeight(true);
            $pagination.smartpaginator({
              totalrecords : $rows.length,
              recordsperpage : parseInt((availableBlocHeight - blocTitleHeight - paginationHeight) /
                  rowHeight),
              length : 5,
              datacontainer : $('[id$="_list"]', $bloc).attr('id'),
              dataelement : 'li',
              next : $('<img>',
                  {src : '<c:url value="/util/viewGenerator/icons/arrows/arrowRight.gif"/>'}),
              prev : $('<img>',
                  {src : '<c:url value="/util/viewGenerator/icons/arrows/arrowLeft.gif"/>'}),
              first : $('<img>',
                  {src : '<c:url value="/util/viewGenerator/icons/arrows/arrowDoubleLeft.gif"/>'}),
              last : $('<img>',
                  {src : '<c:url value="/util/viewGenerator/icons/arrows/arrowDoubleRight.gif"/>'}),
              theme : 'pageNav'
            });
          } else {
            availableBlocHeight +=
                parseInt(remainingBlocHeight / (notEmptyBlocs.length - (index + 1)));
          }
        });
      }

      /**
       * Handle confirmation message.
       * @param subscription
       * @param callback
       * @private
       */
      function __confirmSubscriptionDeletion(subscription, callback) {
        var $confirm = $('<div>').html("${unsubscribeConfirmMessage}".replace('@name@',
            subscription.ui.name)).hide().appendTo(document.body);
        $confirm.popup('confirmation', {
          callback : callback,
          callbackOnClose : function() {
            $confirm.dialog('destroy');
            $confirm.remove();
          }});
      }
    })(jQuery);

    /**
     * Initialization.
     */
    jQuery(document).ready(function() {
      jQuery.subscription();
    });
  </script>
</head>
<body class="subscription-management userPanel">
<!-- Breadcrumb -->
<view:browseBar componentId="${instanceId}">
  <c:forEach items="${context.path}" var="path">
    <view:browseBarElt link="${path.link}" label="${path.name}"/>
  </c:forEach>
  <view:browseBarElt link="#" label="${panelTitleLabel}"/>
</view:browseBar>
<!-- Screen -->
<view:window>
  <div class="compulsory-subscription-management">
    <h4 class="title">${forcedSubscriptionListLabel}</h4>
    <view:buttonPane>
      <c:set var="modifyAction"><c:url value="/RSubscription/jsp/subscriptionpanel.jsp?action=ToUserPanel"/></c:set>
      <view:button label="${modifyForcedSubscriptionLabel}" action="javascript:modifyForcedSubscriptions()"/>
    </view:buttonPane>
    <div id="forced_group_subscription" class="listing_groups">
      <p class="nb_results" id="forced_group_subscription_result_count"></p>
      <ul class="group_list" id="forced_group_subscription_list" style="display: none">
      </ul>
      <div class="pageNav_results_userPanel pager" id="forced_group_subscription_list_pagination" style="display: none">
      </div>
    </div>
    <div id="forced_user_subscription" class="listing_users">
      <p class="nb_results" id="forced_user_subscription_result_count"></p>
      <ul class="user_list" id="forced_user_subscription_list" style="display: none">
      </ul>
      <div class="pageNav_results_userPanel pager" id="forced_user_subscription_list_pagination" style="display: none">
      </div>
    </div>
  </div>
  <div class="individual-subscription">
    <h4 class="title">${selfCreationSubscriptionListLabel}
      <img class="infoBulle" title="${selfCreationSubscriptionListHelpLabel}" src="<c:url value="/util/icons/help.png"/>" alt="info"/>
    </h4>

    <div id="selfCreation_user_subscription" class="listing_users">
      <p id="selfCreation_user_subscription_result_count" class="nb_results"></p>
      <ul class="user_list" id="selfCreation_user_subscription_list" style="display: none">
      </ul>
      <div class="pageNav_results_userPanel pager" id="selfCreation_user_subscription_list_pagination" style="display: none">
      </div>
    </div>
  </div>
</view:window>
</body>
</html>
