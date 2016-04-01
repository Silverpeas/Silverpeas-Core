<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="org.silverpeas.core.web.selection.Selection"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.selection.multilang.selectionBundle" />
<view:setBundle basename="org.silverpeas.notificationManager.multilang.notificationManagerBundle" var="notificationBundle" />

<view:setConstant var="DEACTIVATED_USER_STATE" constant="org.silverpeas.core.admin.user.constant.UserState.DEACTIVATED"/>
<fmt:message var="DEACTIVATED_SHORT_LABEL" key="GML.user.account.state.DEACTIVATED.short"/>
<c:set var="DEACTIVATED_SHORT_LABEL" value="${fn:toLowerCase(DEACTIVATED_SHORT_LABEL)}"/>

<c:set var="currentUserId"           value="${sessionScope['SilverSessionController'].userId}"/>
<c:set var="selection"               value="${requestScope.SELECTION}"/>
<jsp:useBean id="selection" type="org.silverpeas.core.web.selection.Selection"/>
<c:set var="registeredServerDomains" value="${selection.registeredServerDomains}"/>
<c:set var="multipleSelection"       value="${selection.multiSelect}"/>
<c:set var="instanceId"              value="${selection.extraParams.componentId}"/>
<c:set var="domainId"                value="${selection.extraParams.domainId}"/>
<c:set var="roles"                   value="${selection.extraParams.joinedProfileNames}"/>
<c:set var="resourceId"              value="${selection.extraParams.objectId}"/>
<c:set var="validationURL"           value="${selection.goBackURL}"/>
<c:set var="cancelationURL"          value="${selection.cancelURL}"/>
<c:set var="hotSetting"              value="${selection.hotSetting}"/>
<c:set var="selectedUserLimit"       value="${selection.selectedUserLimit}"/>
<c:set var="hidingDeactivatedState" value="${selection.filterOnDeactivatedState}"/>

<c:url var="userProfileUrl"          value="/util/javaScript/angularjs/services/silverpeas-profile.js"/>
<c:url var="silverpeasSearchUrl"     value="/util/javaScript/angularjs/directives/silverpeas-searchbox.js"/>
<c:url var="selectionUrl"            value="/selection/jsp/javaScript/selection.js"/>

<c:set var="selectionScope"          value=""/>
<c:if test="${selection.elementSelectable}">
  <c:set var="selectionScope"        value="user"/>
</c:if>
<c:if test="${selection.setSelectable}">
  <c:set var="selectionScope"        value="${selectionScope}group"/>
</c:if>
<c:if test="${selectionScope == null || fn:length(fn:trim(selectionScope)) == 0}">
  <c:set var="selectionScope"        value="usergroup"/>
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
<fmt:message key='selection.userSelected' var="selectedUserText"/>
<fmt:message key='selection.groupSelected' var="selectedGroupText"/>
<c:if test="${multipleSelection}">
  <fmt:message key='selection.selectedUsers' var="selectedUserText"/>
  <fmt:message key='selection.selectedGroups' var="selectedGroupText"/>
</c:if>

<fmt:message key="selection.RemoveAllGroupsFromSelection" var="removeAllGroupsFromSelection"/>
<fmt:message key="selection.RemoveAllUsersFromSelection" var="removeAllUsersFromSelectionText"/>
<fmt:message key="selection.RemoveFromSelection" var="deselectText"/>

<fmt:message key="selection.searchUserGroups" var="defaultGroupSearchText"/>
<fmt:message key="selection.searchUsers" var="defaultUserSearchText"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.userSelector">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <view:includePlugin name="breadcrumb"/>
    <view:includePlugin name="pagination"/>
    <script type="text/javascript" src="${userProfileUrl}"></script>
    <script type="text/javascript" src="${silverpeasSearchUrl}"></script>
    <script type="text/javascript" src="${selectionUrl}"></script>
    <title><fmt:message key="selection.UserSelectionPanel"/></title>
    <style  type="text/css" >
      html, body {height:100%; overflow:hidden; margin:0px; padding:0px}
      div.pageNav .pages_indication {display: none}
    </style>
  </head>
    <body class="userPanel">
    <div class="container_userPanel" ng-controller="mainController"  ng-cloak>

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
              <li ng-repeat="group in groupsFilter">
                <a href="#" ng-click="goToGroup(group)" class="filter">{{ group.name }}<span class="nb_results_by_filter"> ({{ group.userCount }})</span></a>
              </li>
              <li><a href="#" ng-show="groupsFilter.length < groupsFilter.maxlength" ng-click="displayNextGroups()" class="filter"><fmt:message key='selection.NextGroups'/></a></li>
            </ul>
          </li>
        </ul>
      </div>

      <div id="results_userPanel">
        <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          <div class="groups_results_userPanel">
            <silverpeas-search label="${defaultGroupSearchText}" ng-model="groupNameFilter"></silverpeas-search>
            <div class="listing_groups">
              <p class="nb_results" id="group_result_count">{{ groups.maxlength }} <fmt:message key='selection.groupsFound'/></p>
              <a href="#" ng-show="selectedGroups.multipleSelection && !isSelectedUserLimitEnabled()" ng-click="selectAllGroups()" title="<fmt:message key='selection.AddAllGroupsToSelection'/>" class="add_all"><fmt:message key="selection.AddAllGroups"/></a>
              <ul id="group_list">
                <li ng-repeat="group in groups" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img alt="" src="/silverpeas/util/icons/component/groupe_Type_gestionCollaborative.png"/></div>
                  <span class="name_group"><a href="#" title="<fmt:message key='selection.group.goto'/>" ng-click="goToGroup(group)">{{ group.name }}</a></span>
                  <span class="nb_user_group">{{ group.userCount + ' ' + '<fmt:message key="GML.user_s"/>' }}</span>
                  <span class="sep_nb_user_group" ng-show="showDomainData()"> - </span><span class="domain_group" ng-show="showDomainData()">{{ group.domainName }}</span>
                  <a href="#" ng-show="isGroupSelectable(group)" ng-click="selectGroup(group)" id="{{ 'add_group_' + group.id }}" title="<fmt:message key='selection.AddToSelection'/>" class="add group"><fmt:message key="selection.AddToSelection"/></a>
                </li>
              </ul>
              <silverpeas-pagination page-size="groupPageSize" items-size="totalGroupsSize" on-page="changeGroupListingPage(page)"></silverpeas-pagination>
            </div>
          </div>
        </c:if>
        <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_results_userPanel">
            <silverpeas-search label="${defaultUserSearchText}" ng-model="userNameFilter"></silverpeas-search>
            <div class="listing_users">
              <p class="nb_results" id="user_result_count">{{ users.maxlength }} <fmt:message key='selection.usersFound'/></p>
              <a href="#" ng-show="selectedUsers.multipleSelection && !isSelectedUserLimitEnabled()" ng-click="selectAllUsers()" title="<fmt:message key='selection.AddAllUsersToSelection'/>" class="add_all"><fmt:message key="selection.AddAllUsers"/></a>
              <ul id="user_list">
                <li ng-repeat="user in users" ng-class="user.deactivatedState ? 'state-deactivated' : ''" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img ng-src="{{ user.avatar }}" alt="avatar"/></div>
                  <span class="name_user">{{ userFullName(user) }}<span ng-show="user.deactivatedState"> (${DEACTIVATED_SHORT_LABEL})</span> </span>
                  <span class="mail_user">{{ user.eMail }}</span>
                  <span class="sep_mail_user" ng-show="showDomainData()"> - </span><span class="domain_user" ng-show="showDomainData()">{{ user.domainName }}</span>
                  <a href="#" ng-show="isUserSelectable(user)" ng-click="selectUser(user)" id="{{ 'add_user_' + user.id }}" title="<fmt:message key='selection.AddToSelection'/>" class="add user"><fmt:message key="selection.AddToSelection"/></a>
                </li>
              </ul>
              <silverpeas-pagination page-size="userPageSize" items-size="totalUsersSize" on-page="changeUserListingPage(page)"></silverpeas-pagination>
            </div>
          </div>
        </c:if>
      </div>

      <div id="selected_userPanel">
        <div class="container">
          <h4 class="title">${selectionTitle}</h4>
          <c:if test="${not empty selectedUserLimit and selectedUserLimit > 0}">
            <div class="inlineMessage">
              <fmt:message bundle="${notificationBundle}" key="notif.manual.receiver.limit.message.warning"><fmt:param value="${selectedUserLimit}"/></fmt:message>
            </div>
          </c:if>
          <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          <div class="groups_selected_userPanel">
            <div class="listing_groups">
              <p class="nb_results" id="group_selected_count">{{ selectedGroups.length }} ${selectedGroupText}</p>
              <a href="#" ng-show="selectedGroups.multipleSelection" ng-click="deselectAllGroups()" title="${removeAllGroupsFromSelectionText}" class="remove_all"><fmt:message key="selection.Empty"/></a>
              <ul id="selected_group_list">
                <li ng-repeat="group in selectedGroups.currentpage()" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img alt="" src="/silverpeas/util/icons/component/groupe_Type_gestionCollaborative.png"/></div>
                  <span class="name_group">{{ group.name }}</span>
                  <span class="nb_user_group">{{ group.userCount + ' ' + '<fmt:message key="GML.user_s"/>' }}</span>
                  <span class="sep_nb_user_group" ng-show="showDomainData()"> - </span><span class="domain_group" ng-show="showDomainData()">{{ group.domainName }}</span>
                  <a ng-click="deselectGroup(group)" title="${deselectText}" href="#" class="remove group">${deselectText}</a>
                </li>
              </ul>
              <silverpeas-pagination page-size="groupSelectionPageSize" items-size="selectedGroups.length()" on-page="changeGroupSelectionPage(page)"></silverpeas-pagination>
            </div>
          </div>
          </c:if>

          <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_selected_userPanel">
            <div class="listing_users">
              <p class="nb_results" id="user_selected_count">{{ selectedUsers.length }} ${selectedUserText}</p>
              <a href="#" ng-show="selectedUsers.multipleSelection" ng-click="deselectAllUsers()" title="${removeAllUsersFromSelectionText}" class="remove_all"><fmt:message key="selection.Empty"/></a>
              <ul id="selected_user_list">
                <li ng-repeat="user in selectedUsers.currentpage()" ng-class="user.deactivatedState ? 'state-deactivated' : ''" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img ng-src="{{ user.avatar }}" alt="avatar"/></div>
                  <span class="name_user">{{ userFullName(user) }}<span ng-show="user.deactivatedState"> (${DEACTIVATED_SHORT_LABEL})</span> </span>
                  <span class="mail_user">{{ user.eMail }}</span>
                  <span class="sep_mail_user" ng-show="showDomainData()"> - </span><span class="domain_user" ng-show="showDomainData()">{{ user.domainName }}</span>
                  <a ng-click="deselectUser(user)" title="${deselectText}" href="#" class="remove user">${deselectText}</a>
                </li>
              </ul>
              <silverpeas-pagination page-size="userSelectionPageSize" items-size="selectedUsers.length()" on-page="changeUserSelectionPage(page)"></silverpeas-pagination>
            </div>
          </div>
          </c:if>

          <form action="${validationURL}" id="selection" method="POST">
            <input type="hidden" name="UserOrGroupSelection" value="true"/>
            <input id="group-selection" type="hidden" name="GroupSelection" value=""/>
            <input id="user-selection" type="hidden" name="UserSelection" value=""/>
            <br clear="all"/>
            <div id="validate" class="buttonPane milieuBoutonV5">
              <fmt:message var="selectLabel" key="GML.validate"/>
              <fmt:message var="cancelLabel" key="GML.cancel"/>
              <a class="button" href="#"  ng-click="validate()">${selectLabel}</a>
              <c:if test='${not fn:endsWith(cancelationURL, "userpanel.jsp")}'>
                <a class="button" href="#" ng-click="cancel()">${cancelLabel}</a>
              </c:if>
            </div>
          </form>
        </div>
      </div>
    </div>
    <script type="text/javascript">

        /* configure for the current application the context of the module silverpeas in which all
         * are defined the business objects and services */
        angular.module('silverpeas').
                value('context', {
          currentUserId: '${currentUserId}',
          multiSelection: ${multipleSelection},
          selectionScope: '${selectionScope}',
          selectedUserLimit: ${selectedUserLimit},
          hidingDeactivatedState : ${hidingDeactivatedState},
          component: '${instanceId}',
          resource: '${resourceId}',
          roles: '${roles}',
          domain: '${domainId}'});

        /* declare the module userSelector and its dependencies (here on the silverpeas module) */
        var userSelector = angular.module('silverpeas.userSelector', ['silverpeas.services', 'silverpeas.directives']);

        /* the main controller of the application */
        userSelector.controller('mainController', function(context, User, UserGroup, $scope) {
            /* some constants */
            PageSize = 6;
            PageMaxSize = 10;
            GroupsFilterSizeStep = 60;
            GroupSearchDefaultText = '<fmt:message key="selection.searchUserGroups"/>';
            UserSearchDefaultText = '<fmt:message key="selection.searchUsers"/>';
            UserSource = {
              All: 0, // all the users
              Groups: 1, // the users coming from a given group or belonging to all groups
              Relationships: 2 // the users coming from my relationships
            };

            // what are the preselected user profiles and user groups (get at web page build)
      <c:choose>
        <c:when test="${selection.selectedElements != null && fn:length(selection.selectedElements) > 0}">
        var preselectedUsers = '<c:out value="${fn:join(selection.selectedElements, ',')}"/>'.split(',');
        if (preselectedUsers[0] === "")
          preselectedUsers = [];
        </c:when>
        <c:otherwise>
          var preselectedUsers = [];
        </c:otherwise>
      </c:choose>
      <c:choose>
        <c:when test="${selection.selectedSets != null && fn:length(selection.selectedSets) > 0}">
        var preselectedUserGroups = '<c:out value="${fn:join(selection.selectedSets, ',')}"/>'.split(',');
        if (preselectedUserGroups[0] === "")
          preselectedUserGroups = [];
        </c:when>
        <c:otherwise>
          var preselectedUserGroups = [];
        </c:otherwise>
      </c:choose>

            /* the type of users displayed in the user listing: relationships or users in a group */
            var displayedUserType;
            /* keeps in memory the current filters on the user name and on the group name */
            var listingFilters = {
              userName: null,
              groupName: null
            };

          /**
           * Sets the common parameters
           */
          function __applyCommonParameters(params) {
            if (typeof params === 'undefined') {
              params = {};
            }
            if (typeof params === 'object') {
              if (context.hidingDeactivatedState) {
                params.userStatesToExclude = ['${DEACTIVATED_USER_STATE}'];
              }
            }
            return params;
          }

          /**
           * Centralization of getting user data in order to handle common parameters.
           */
          function __getUsers(params) {
            return User.get(__applyCommonParameters(params));
          }

          /**
           * Centralization of getting user group data in order to handle common parameters.
           */
          function __getUserGroups(params) {
            return UserGroup.get(__applyCommonParameters(params));
          }

            /* The root of the user group tree. It can be then considered as any other user group */
            var rootGroup = { name: '<fmt:message key="selection.RootUserGroups"/>', root: true,
              subgroups: function() {
                return __getUserGroups(arguments[0]);
              },
              users: function() {
                var params = (arguments.length > 0 && arguments[0] ? arguments[0]:{});
                params.group = 'all';
                return __getUsers(params);
              }
            };

             /* me, the current user */
             var me;
          __getUsers(${currentUserId}).then(function(user) {
               me = user;
             });

            /*
             *  Local functions
             */

            /* fetch the users matching the specified parameters. The kind of users that fetched
             * depends on the current user displaying context (relationships, all users, or users of a
             * group) */
            function fetchUsers(params) {
              var users;
              switch(displayedUserType) {
                case UserSource.All:
                  users = __getUsers(params);
                  break;
                case UserSource.Groups:
                  users = $scope.currentGroup.users(__applyCommonParameters(params));
                  break;
                case UserSource.Relationships:
                  users = me.relationships(__applyCommonParameters(params));
                  break;
              }
              return users;
            }

            /* sets the specified array of groups for the filter on the groups */
            function setGroupsFilter(groups) {
              $scope.groupsFilter = groups;
            }

             /* update the groups in the filter on the groups */
            function updateGroupsFilter(groups) {
              if ($scope.groupsFilter)
                for (var i = 0; i < groups.length; i++)
                  $scope.groupsFilter.push(groups[i]);
              else
                $scope.groupsFilter = groups;
            }

            /* updates the groups listing with the specified ones */
            function updateGroupsListing(groups) {
              $scope.groups = groups;
              if ($scope.totalGroupsSize !== $scope.groups.maxlength)
                $scope.totalGroupsSize = $scope.groups.maxlength;
            }

            /* updates the users listing with the specified ones */
            function updateUsersListing(users) {
              $scope.users = users;
              if ($scope.totalUsersSize !== $scope.users.maxlength)
                $scope.totalUsersSize = $scope.users.maxlength;
            }

            /* maximize the the listing of groups over the listing of users */
            function maximizeGroupsListingPanel() {
              $scope.groupPageSize = PageMaxSize;
              $('.users_results_userPanel').hide();
            }

            /* maximize the the listing of users over the listing of groups */
            function maximizeUsersListingPanel() {
              $scope.userPageSize = PageMaxSize;
              $('.groups_results_userPanel').hide();
            }

            /* unmaximize the the listing of users and the listing of groups reappears */
            function unmaximizeUsersListingPanel() {
              $scope.userPageSize = PageSize;
              $('.groups_results_userPanel').show();
            }

            /* Resets explicitly the search queries on the user and group names */
            function resetSearchQueries() {
              $scope.groupNameFilter = '';
              $scope.userNameFilter = '';
            }

            /* search the users matching the specified name */
            function searchUsers(name) {
                listingFilters.userName = (name === '*' ? null:name);
                fetchUsers({name: name, page: {number: 1, size: $scope.userPageSize}}).then(updateUsersListing);
            };

            /* search the groups matching the specified name */
            function searchGroups(name) {
                listingFilters.groupName = (name === '*' ? null:name);
              $scope.currentGroup.subgroups(__applyCommonParameters({
                name : name, page : {number : 1, size : $scope.groupPageSize}
              })).then(updateGroupsListing);
            };

            /* Total number of selected users (from user and group lists) */
            function getTotalNumberOfSelectedUsers () {
              var nbSelectedUsers = $scope.selectedUsers.length();
              $.each($scope.selectedGroups.items, function(index, group) {
                nbSelectedUsers += group.userCount;
              });
              return nbSelectedUsers;
            };

            /* initialize the userSelection app by filling it with some values */
            function init() {
              switch(context.selectionScope) {
                case 'group':
                  maximizeGroupsListingPanel();
                  $scope.userPageSize = 0;
                  $scope.userSelectionPageSize = 0;
                  $scope.groupSelectionPageSize = PageMaxSize;
                  displayedUserType = UserSource.Groups;
                  __getUserGroups({
                    page : {
                      number : 1,
                      size : $scope.groupPageSize
                    }
                  }).then(updateGroupsListing);
                  break;
                case 'user':
                  maximizeUsersListingPanel();
                  displayedUserType = UserSource.All;
                  $scope.groupPageSize = 0;
                  $scope.userSelectionPageSize = PageMaxSize;
                  $scope.groupSelectionPageSize = 0;
                  __getUsers({
                    page : {
                      number : 1,
                      size : $scope.userPageSize
                    }
                  }).then(updateUsersListing);
                  break;
                default:
                  maximizeUsersListingPanel();
                  displayedUserType = UserSource.All;
                  $scope.groupPageSize = PageSize;
                  $scope.userSelectionPageSize = PageSize;
                  $scope.groupSelectionPageSize = PageSize;
                  __getUsers({
                    page : {
                      number : 1,
                      size : $scope.userPageSize
                    }
                  }).then(updateUsersListing);
                  __getUserGroups({
                    page : {
                      number : 1,
                      size : $scope.groupPageSize
                    }
                  }).then(updateGroupsListing);
              }
              $scope.currentGroup = rootGroup;
              __getUserGroups({
                page : {
                  number : 1,
                  size : GroupsFilterSizeStep
                }
              }).then(setGroupsFilter);
              $scope.selectedUsers = new Selection(context.multiSelection, $scope.userSelectionPageSize);
              if(preselectedUsers.length) {
                __getUsers({"id":preselectedUsers}).then(function(users) {
                  $scope.selectedUsers.add(users);
                });
              }
              $scope.selectedGroups = new Selection(context.multiSelection, $scope.groupSelectionPageSize);
              if(preselectedUserGroups.length) {
                __getUserGroups({"ids":preselectedUserGroups}).then(function(groups) {
                  $scope.selectedGroups.add(groups);
                });
              }
              resetSearchQueries();
            }

           /* Watchers */

           $scope.$watch('groupNameFilter', searchGroups);
           $scope.$watch('userNameFilter', searchUsers);

           /* Functions provided by the scope in order to be used in GUI */

          /* The full name of a user by starting with its last name */
          $scope.userFullName = function(user) {
            return user.lastName + ' ' + user.firstName;
          }

          /* Indicates if the information of domain has to be displayed */
          $scope.showDomainData = function() {
            return ${fn:length(registeredServerDomains) > 1};
          };

           /* Indicates if a limitation exist around the maximum number of selectable users (from group & user lists) */
           $scope.isSelectedUserLimitEnabled = function() {
             return !(typeof context.selectedUserLimit === 'undefined') && context.selectedUserLimit > 0;
           };

           /* Indicates if the current group is selectable */
           $scope.isGroupSelectable = function(group) {
             var isSelectable = $scope.selectedGroups.indexOf(group) < 0;
             if (isSelectable && $scope.isSelectedUserLimitEnabled()) {
               var totalOfSelectedUsers = getTotalNumberOfSelectedUsers();
               isSelectable = group.userCount > 0
                              && context.selectedUserLimit >= (totalOfSelectedUsers + group.userCount);
             }
             return isSelectable;
           };

           /* Indicates if the current user is selectable */
           $scope.isUserSelectable = function(user) {
             var isSelectable = $scope.selectedUsers.indexOf(user) < 0;
             if (isSelectable && $scope.isSelectedUserLimitEnabled()) {
               isSelectable = context.selectedUserLimit > getTotalNumberOfSelectedUsers();
             }
             return isSelectable;
           };

           /* pagination in the listings and in the selections panels */
           $scope.changeGroupListingPage = function(pageNumber) {
              var params = {page: {number: pageNumber, size: $scope.groupPageSize}};
              if (listingFilters.groupName)
                params.name = listingFilters.groupName;
             $scope.currentGroup.subgroups(__applyCommonParameters(params)).then(updateGroupsListing);
            };
            $scope.changeUserListingPage = function(pageNumber) {
              var params = {page: {number: pageNumber, size: $scope.userPageSize}};
              if (listingFilters.userName)
                params.name = listingFilters.userName;
              fetchUsers(params).then(updateUsersListing);
            };
            $scope.changeUserSelectionPage = function(pageNumber) {
              $scope.selectedUsers.page(pageNumber);
              $scope.$apply();
            };
            $scope.changeGroupSelectionPage = function(pageNumber) {
              $scope.selectedGroups.page(pageNumber);
              $scope.$apply();
            };

            /* displays the nexts given number of groups in the filter of groups */
            $scope.displayNextGroups = function() {
              var pageNumber = ($scope.groupsFilter.length / GroupsFilterSizeStep) + 1;
              $scope.currentGroup.subgroups(__applyCommonParameters({
                page : {
                  number : pageNumber,
                  size : GroupsFilterSizeStep
                }
              })).then(updateGroupsFilter);
            };

            /* select all the user groups present in the corresponding listing panel */
            $scope.selectAllGroups = function() {
              $scope.currentGroup.subgroups(__applyCommonParameters()).then(function(groups) {
                $scope.selectedGroups.add(groups);
              });
            };

            /* select all the users present in the corresponding listing panel */
            $scope.selectAllUsers = function() {
              fetchUsers().then(function(users) {
                $scope.selectedUsers.add(users);
              });
            };

            /* select the specified user */
            $scope.selectUser = function(user) {
              if (!context.multiSelection)
                $scope.selectedGroups.clear();
              $scope.selectedUsers.add(user);
            };

            /* select the specified group */
            $scope.selectGroup = function(group) {
              if (!context.multiSelection)
                $scope.selectedUsers.clear();
              $scope.selectedGroups.add(group);
            };

            /* deselect all the selected groups */
            $scope.deselectAllGroups = function() {
              $scope.selectedGroups.clear();
            };

            /* deselect all the selected users */
            $scope.deselectAllUsers = function() {
              $scope.selectedUsers.clear();
            };

            /* deselect the specified user */
            $scope.deselectUser = function(user) {
              $scope.selectedUsers.remove(user);
            };

            /* deselect the specified group */
            $scope.deselectGroup = function(group) {
              $scope.selectedGroups.remove(group);
            };

            /* renders all the current user relationships */
            $scope.goToMyContacts = function() {
              maximizeUsersListingPanel();
              displayedUserType = UserSource.Relationships;
              fetchUsers({page: {number:1, size: $scope.userPageSize}}).then(updateUsersListing);
              resetSearchQueries();
              highlightFilter($('#filter_contact'));
              maximizeUsersListingPanel();
            };

            /* renders all the users */
            $scope.goToAllUsers = function() {
              maximizeUsersListingPanel();
              displayedUserType = UserSource.All;
              fetchUsers({page: {number:1, size: $scope.userPageSize}}).then(updateUsersListing);
              resetSearchQueries();
              highlightFilter($('#filter_users'));
            };

            /* renders the subgroups of the specified group or all the groups if it is the root of the tree */
            $scope.goToGroup = function(group) {
              $('#breadcrumb').breadcrumb('set', group);
            };

             /* validate the selection of users and/or of user groups */
            $scope.validate = function() {
              var selectedGroupIds = $scope.selectedGroups.itemIdsAsString();
              var selectedUserIds = $scope.selectedUsers.itemIdsAsString();
       <c:choose>
        <c:when test="${hotSetting}">
              var selectedGroupNames = $scope.selectedGroups.itemNamesAsString();
              var selectedUserNames = $scope.selectedUsers.itemNamesAsString();
              var selectionIdField = '<c:out value="${selection.htmlFormElementId}"/>';
              var selectionNameField = '<c:out value="${selection.htmlFormElementName}"/>';
              var selectionTypeField = '<c:out value="${selection.htmlFormElementType}"/>';
              window.opener.$('#' + selectionIdField).val((selectedUserIds.length > 0 ? selectedUserIds : selectedGroupIds));
              window.opener.$('#' + selectionNameField).val((selectedUserNames.length > 0 ? selectedUserNames : selectedGroupNames));
              if(selectionTypeField != null && selectionTypeField.length > 0) {
		window.opener.$('#' + selectionTypeField).val((selectedUserIds.length > 0 ? '<%=Selection.TYPE_SELECTED_ELEMENT%>' : '<%=Selection.TYPE_SELECTED_SET%>'));
              }

              window.opener.$('#' + selectionIdField + "-userIds").val(selectedUserIds);
              window.opener.$('#' + selectionIdField + "-groupIds").val(selectedGroupIds);
              window.opener.$('#' + selectionIdField + "-userIds").trigger("change");
              window.opener.$('#' + selectionIdField + "-groupIds").trigger("change");

              window.close();
        </c:when>
        <c:otherwise>
              $("input#group-selection").val(selectedGroupIds);
              $("input#user-selection").val(selectedUserIds);
              $("#selection").submit();
        </c:otherwise>
      </c:choose>
            };

            /* cancel the selection and go back to the caller */
            $scope.cancel = function() {
      <c:choose>
        <c:when test="${hotSetting}">
          window.close();
        </c:when>
        <c:otherwise>
            $('input[name="UserOrGroupSelection"]').val('false');
            $("#selection").attr("action", "<c:out value='${cancelationURL}'/>");
            $("#selection").submit();
        </c:otherwise>
      </c:choose>
            };

            /* breadcrumb on the user groups. It initializes also the app and it performs the change
             *  coming from the navigation on the groups */
            $('#breadcrumb').breadcrumb({
              root: rootGroup,
              oninit: function() {
              },
              onchange: function(group) {
                displayedUserType = UserSource.Groups;
                group.subgroups(__applyCommonParameters({
                  page : {
                    number : 1,
                    size : GroupsFilterSizeStep
                  }
                })).then(setGroupsFilter);
                if (context.selectionScope !== 'user') {
                  unmaximizeUsersListingPanel();
                  group.subgroups(__applyCommonParameters({
                    page : {
                      number : 1, size : $scope.groupPageSize
                    }
                  })).then(updateGroupsListing);
                }
                if (context.selectionScope !== 'group')
                  group.users(__applyCommonParameters({
                    page : {
                      number : 1, size : $scope.userPageSize
                    }
                  })).then(updateUsersListing);
                $scope.currentGroup = group;
                resetSearchQueries();
                highlightFilter($('#breadcrumb'));
              }
            });

            init();
        });

        /* highlight the specified HTML element */
        function highlightFilter($this) {
          $('.filter').removeClass('select');
          $this.addClass('select');
        }

        /* autoresize the filters area according to the width and height of the window */
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
            var documentWidth = 990;
            if ($(window).width() < documentWidth) {
              window.resizeTo(documentWidth, 600);
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
