<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.silverpeas.selection.multilang.selectionBundle" />

<c:set var="currentUserId"     value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="selection"         value="${requestScope.SELECTION}"/>
<c:set var="multipleSelection" value="${selection.multiSelect}"/>
<c:set var="instanceId"        value="${selection.extraParams.componentId}"/>
<c:set var="domainId"          value="${selection.extraParams.domainId}"/>
<c:set var="roles"             value="${selection.extraParams.joinedProfileNames}"/>
<c:set var="resourceId"        value="${selection.extraParams.objectId}"/>
<c:set var="validationURL"     value="${selection.goBackURL}"/>
<c:set var="cancelationURL"    value="${selection.cancelURL}"/>
<c:set var="hotSetting"        value="${selection.hotSetting}"/>

<c:set var="selectionScope"   value=""/>
<c:if test="${selection.elementSelectable}">
  <c:set var="selectionScope" value="user"/>
</c:if>
<c:if test="${selection.setSelectable}">
  <c:set var="selectionScope" value="${selectionScope}group"/>
</c:if>
<c:if test="${selectionScope == null || fn:length(fn:trim(selectionScope)) == 0}">
  <c:set var="selectionScope" value="usergroup"/>
</c:if>
<c:choose>
  <c:when test='${!multipleSelection}'>
    <fmt:message key="selection.SelectedUserOrGroup" var="selectionTitle"/>
  </c:when>
  <c:when test='${selectionScope == "user"}'>
    <fmt:message key="selection.SelectedUsers" var="selectionTitle"/>
  </c:when>
  <c:when test='${selectionScope == "group"}'>
    <fmt:message key="selection.SelectedGroups" var="selectionTitle"/>
  </c:when>
  <c:otherwise>
    <fmt:message key="selection.SelectedUsersAndGroups" var="selectionTitle"/>
  </c:otherwise>
</c:choose>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="userSelector">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <view:includePlugin name="pagination"/>
    <view:includePlugin name="breadcrumb"/>
    <script type="text/javascript" src="/silverpeas/util/javaScript/angular.min.js"></script>
    <title><fmt:message key="selection.UserSelectionPanel"/></title>
    <style  type="text/css" >
      html, body {height:100%; overflow:hidden; margin:0px; padding:0px}
      div.pageNav .pages_indication {display: none}
    </style>
  </head>
    <body class="userPanel">
    <div class="container_userPanel" ng-controller="mainController">

      <div id="filter_userPanel">
        <h4 class="title"><fmt:message key="selection.Filter"/></h4>
        <ul class="listing_filter">
          <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
            <li><a href="#" ng-click="goToMyContacts()" id="filter_contact" class="filter"><fmt:message key="selection.MyContacts"/></a></li>
            <li><a href="#" ng-click="goToAllUsers()" id="filter_users" class="filter select"><fmt:message key="selection.AllUsers"/></a></li>
          </c:if>
          <li id="filter_groups">
            <div class="filter" id="breadcrumb"></div>
            <ul class="listing_groups_filter">
              <li ng-repeat="group in groups">
                <a href="#" ng-click="goToGroup(group)" class="filter">{{ group.name }}<span class="nb_results_by_filter"> ({{ group.userCount }})</span></a>
              </li>
            </ul>
          </li>
        </ul>
      </div>

      <div id="results_userPanel">
        <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          <div class="groups_results_userPanel">
            <div class="container_input_search">
              <input type="text" class="search autocompletion" id="group_search" ng-model="searchedGroups"/>
            </div>
            <div class="listing_groups">
              <p class="nb_results" id="group_result_count">{{ groups.length }} <fmt:message key='selection.groupsFound'/></p>
              <a href="#" ng-click="selectAllGroups()" title="<fmt:message key='selection.AddAllGroupsToSelection'/>" class="add_all"><fmt:message key="selection.AddAllGroups"/></a>
              <ul id="group_list">
                <li ng-repeat="group in groups" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img alt="" src="/silverpeas/util/icons/component/groupe_Type_gestionCollaborative.png"></img></div>
                  <span class="name_group">{{ group.name }}</span><span class="nb_user_group">{{ group.userCount + ' ' + '<fmt:message key="GML.user_s"/>' }}</span>
                  <span class="sep_nb_user_group"> - </span><span class="domain_group">{{ group.domainName }}</span>
                  <a href="#" ng-click="selectGroup(group)" id="{{ 'add_group_' + group.id }}" title="<fmt:message key='selection.AddToSelection'/>" class="add group"><fmt:message key="selection.AddToSelection"/></a>
                </li>
              </ul>
              <div id="group_list_pagination" class="pageNav_results_userPanel"></div>
            </div>
          </div>
        </c:if>
        <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_results_userPanel">
            <div class="container_input_search">
              <input type="text" class="search autocompletion" id="user_search" ng-model="searchedUsers"/>
            </div>
            <div class="listing_users">
              <p class="nb_results" id="user_result_count">{{ users.length }} <fmt:message key='selection.usersFound'/></p>
              <a href="#" ng-click="selectAllUsers()" title="<fmt:message key='selection.AddAllUsersToSelection'/>" class="add_all"><fmt:message key="selection.AddAllUsers"/></a>
              <ul id="user_list">
                <li ng-repeat="user in users" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img ng-src="{{ user.avatar }}" alt="avatar"/></div>
                  <span class="name_user">{{ user.lastName + ' ' + user.firstName }} </span>
                  <span class="mail_user">{{ user.eMail }}</span><span class="sep_mail_user"> - </span><span class="domain_user">{{ user.domainName }}</span>
                  <a href="#" ng-click="selectUser(user)" id="{{ 'add_user_' + user.id }}" title="<fmt:message key='selection.AddToSelection'/>" class="add user"><fmt:message key="selection.AddToSelection"/></a>
                </li>
              </ul>
                <div id="user_list_pagination" class="pageNav_results_userPanel"></div>
            </div>
          </div>
        </c:if>
      </div>

    </div>
    <script type="text/javascript">
        /* configure the application userSelector */
        angular.module('userSelector', []).
                constant('context', {
          currentUserId: '${currentUserId}',
          selectionScope: '${selectionScope}',
          component: '${instanceId}',
          resource: '${resourceId}',
          roles: '${roles}',
          domain: '${domainId}'});
        /* some services */
        angular.module('userSelector').factory('service', function(context, $http, $log) {
          var userRootURL = webContext + '/services/profile/users';
          var groupRootURL = webContext + '/services/profile/groups';
          var defaultQuery = '';
          if (context.resource)
            defaultQuery += '?resource=' + context.resource;
          if (context.domain)
            defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?':'&') + 'domain=' + context.domain;
          if (context.roles)
            defaultQuery += (defaultQuery.indexOf('?') < 0 ? '?':'&') + 'roles=' + context.roles;
          return {
            getAllUsers: function() {
              var filter =  defaultQuery += (context.component? ((defaultQuery.indexOf('?') < 0 ? '?':'&') +
                      'component=' + context.component):'');
              filter += (defaultQuery.indexOf('?') < 0 ? '?':'&') + 'page=1;10';
              return $http.get(userRootURL + filter).error(function(data, status) {
                alert(status);
              });
            },

            getAllGroups: function() {
              var filter = (context.componentId? '/application/' + context.component:'') + defaultQuery;
              return $http.get(groupRootURL + filter).error(function(data, status) {
                alert(status);
              });
            },

            getSubGroupsOf: function(group) {
              return $http.get(groupRootURL + '/' + group.id +'/groups').error(function(data, status) {
                alert(status);
              });
            },

            getUser: function(userId) {
              return $http.get(userRootURL + '/' + userId + defaultQuery).error(function(data, status) {
                alert(status);
              });
            },

            getUsersInGroup: function(group) {
              var filter = (defaultQuery.indexOf('?') < 0 ? '?':'&') + 'group=' + group.id + '&page=1;10'
              return $http.get(userRootURL + defaultQuery + filter).error(function(data, status) {
                alert(status);
              });
            },

            getRelationshipsOf: function(user) {
              return $http.get(userRootURL + '/' + user.id + '/contacts' + defaultQuery).error(function(data, status) {
                alert(status);
              });
            }
          };
        });
        /* the main controller */
        angular.module('userSelector').controller('mainController', function(service, $scope) {
            service.getAllGroups().success(function(data) {
              $scope.groups = data;
            });

            $scope.currentGroup = null;
            service.getAllUsers().success(function(data) {
              $scope.users = data;
            });
            $scope.searchedGroups = '<fmt:message key="selection.searchUserGroups"/>';
            $scope.searchedUsers = '<fmt:message key="selection.searchUsers"/>';
            $scope.selectedUsers = {};
            $scope.selectedGroups = {};
            $scope.selectAllGroups = function() {
              for (var i = 0; i < $scope.groups.length; i++)
                $scope.selectedGroups[$scope.groups[i].id] = $scope.groups[i];
            };
            $scope.selectAllUsers = function() {
              for (var i = 0; i < $scope.users.length; i++)
                $scope.selectedUsers[$scope.users[i].id] = $scope.users[i];
            };
            $scope.selectUser = function(user) {
              $scope.selectedUsers[user.id] = user;
            };
            $scope.selectGroup = function(group) {
              $scope.selectedGroups[group.id] = group;
            };
            $scope.goToMyContacts = function() {
              service.getRelationshipsOf({id: "${currentUserId}"}).success(function(data){
                $scope.users = data;
              });
            };
            $scope.goToAllUsers = function() {
              service.getAllUsers().success(function(data) {
                $scope.users = data;
              });
            };
            $scope.goToGroup = function(group) {
              service.getSubGroupsOf(group).success(function(data){
                $scope.groups = data;
              });
              service.getUsersInGroup(group).success(function(data){
                $scope.users = data;
              });
              $('#breadcrumb').breadcrumb('set', group);
            };

            $('#breadcrumb').breadcrumb({
              root: { name: '<fmt:message key="selection.RootUserGroups"/>' },
              oninit: function() {

              },
              onchange: function(group) {
                $scope.goToGroup(group);
                highlightFilter($('#breadcrumb'));
            }
          });

        });

        function highlightFilter($this) {
          $('.filter').removeClass('select');
          $this.addClass('select');
        }

        function autoresizeUserGroupFilters() {
          var height_container = $('.container_userPanel').outerHeight();
          var height_listing_groups = $('.listing_groups_filter').outerHeight();
          var height_listing = $('.listing_filter').outerHeight();
          var height_title = $('#filter_userPanel .title').outerHeight();
          var new_height_listing_groups = height_container - (height_listing - height_listing_groups) - height_title;

          $('.listing_groups_filter').css('height', new_height_listing_groups + 'px');
        }

        $(document).ready(function() {
          try {
            var browser = jQuery.uaMatch(navigator.userAgent).browser;
            var documentWidth = $(document).width();
            if (browser === "webkit") {
              documentWidth = "980";
            }

            if ($(window).width() < documentWidth) {
              window.resizeTo(documentWidth, 758);
            }
          } catch (e) {
            // to prevent errors according to cross browser compatibility
          }

          $(window).resize(function() {
            autoresizeUserGroupFilters();
          });

          $('.add').focusin(function() {
            $(this).parent('li').addClass('focus');
          });
          $('.add').focusout(function() {
            $(this).parent('li').removeClass('focus');
          });
          $('.remove').focusin(function() {
            $(this).parent('li').addClass('focus');
          });
          $('.remove').focusout(function() {
            $(this).parent('li').removeClass('focus');
          });
          $('.line').hover(function() {
            $(this).addClass('focus');
          }, function() {
            $(this).removeClass('focus');
          });
        });
    </script>
  </body>
</html>
