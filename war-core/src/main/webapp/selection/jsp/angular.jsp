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
<fmt:message key='selection.userSelected' var="selectedUserText"/>
<fmt:message key='selection.groupSelected' var="selectedGroupText"/>
<c:if test="${multipleSelection}">
  <fmt:message key='selection.usersSelected' var="selectedUserText"/>
  <fmt:message key='selection.groupsSelected' var="selectedGroupText"/>
</c:if>

<fmt:message key="selection.RemoveAllGroupsFromSelection" var="removeAllGroupsFromSelection"/>
<fmt:message key="selection.RemoveAllUsersFromSelection" var="removeAllUsersFromSelectionText"/>
<fmt:message key="selection.RemoveFromSelection" var="deselectText"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="userSelector">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <view:includePlugin name="pagination"/>
    <view:includePlugin name="breadcrumb"/>
    <script type="text/javascript" src="/silverpeas/util/javaScript/angular.min.js"></script>
    <script type="text/javascript" src="/silverpeas/selection/jsp/javaScript/silverpeas-angular.js"></script>
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
            <div class="container_input_search">
              <input type="text" class="search autocompletion" id="group_search" ng-change="searchMatchingGroups()" ng-model="searchedGroups"/>
            </div>
            <div class="listing_groups">
              <p class="nb_results" id="group_result_count">{{ groups.maxlength }} <fmt:message key='selection.groupsFound'/></p>
              <a href="#" ng-show="selectedGroups.multipleSelection" ng-click="selectAllGroups()" title="<fmt:message key='selection.AddAllGroupsToSelection'/>" class="add_all"><fmt:message key="selection.AddAllGroups"/></a>
              <ul id="group_list">
                <li ng-repeat="group in groups" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img alt="" src="/silverpeas/util/icons/component/groupe_Type_gestionCollaborative.png"></img></div>
                  <span class="name_group">{{ group.name }}</span><span class="nb_user_group">{{ group.userCount + ' ' + '<fmt:message key="GML.user_s"/>' }}</span>
                  <span class="sep_nb_user_group"> - </span><span class="domain_group">{{ group.domainName }}</span>
                  <a href="#" ng-show="selectedGroups.indexOf(group) < 0" ng-click="selectGroup(group)" id="{{ 'add_group_' + group.id }}" title="<fmt:message key='selection.AddToSelection'/>" class="add group"><fmt:message key="selection.AddToSelection"/></a>
                </li>
              </ul>
              <div id="group_list_pagination" class="pageNav_results_userPanel"></div>
            </div>
          </div>
        </c:if>
        <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_results_userPanel">
            <div class="container_input_search">
              <input type="text" class="search autocompletion" id="user_search" ng-change="searchMatchingUsers()" ng-model="searchedUsers"/>
            </div>
            <div class="listing_users">
              <p class="nb_results" id="user_result_count">{{ users.maxlength }} <fmt:message key='selection.usersFound'/></p>
              <a href="#" ng-show="selectedUsers.multipleSelection" ng-click="selectAllUsers()" title="<fmt:message key='selection.AddAllUsersToSelection'/>" class="add_all"><fmt:message key="selection.AddAllUsers"/></a>
              <ul id="user_list">
                <li ng-repeat="user in users" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img ng-src="{{ user.avatar }}" alt="avatar"/></div>
                  <span class="name_user">{{ user.lastName + ' ' + user.firstName }} </span>
                  <span class="mail_user">{{ user.eMail }}</span><span class="sep_mail_user"> - </span><span class="domain_user">{{ user.domainName }}</span>
                  <a href="#" ng-show="selectedUsers.indexOf(user) < 0" ng-click="selectUser(user)" id="{{ 'add_user_' + user.id }}" title="<fmt:message key='selection.AddToSelection'/>" class="add user"><fmt:message key="selection.AddToSelection"/></a>
                </li>
              </ul>
                <div id="user_list_pagination" class="pageNav_results_userPanel"></div>
            </div>
          </div>
        </c:if>
      </div>

      <div id="selected_userPanel">
        <div class="container">
          <h4 class="title">${selectionTitle}</h4>
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
                  <span class="sep_nb_user_group"> - </span><span class="domain_group">{{ group.domainName }}</span>
                  <a ng-click="deselectGroup(group)" title="${deselectText}" href="#" class="remove group">${deselectText}</a>
                </li>
              </ul>
              <div class="pageNav_results_userPanel" id="selected_group_list_pagination">
              </div>
            </div>
          </div>
          </c:if>

          <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_selected_userPanel">
            <div class="listing_users">
              <p class="nb_results" id="user_selected_count">{{ selectedUsers.length }} ${selectedUserText}</p>
              <a href="#" ng-show="selectedUsers.multipleSelection" ng-click="deselectAllUsers()" title="${removeAllUsersFromSelectionText}" class="remove_all"><fmt:message key="selection.Empty"/></a>
              <ul id="selected_user_list">
                <li ng-repeat="user in selectedUsers.currentpage()" ng-class-odd="'line odd'" ng-class-even="'line even'">
                  <div class="avatar"><img ng-src="{{ user.avatar }}" alt="avatar"/></div>
                  <span class="name_user">{{ user.lastName + ' ' + user.firstName }} </span>
                  <span class="mail_user">{{ user.eMail }}</span><span class="sep_mail_user"> - </span><span class="domain_user">{{ user.domainName }}</span>
                  <a ng-click="deselectUser(user)" title="${deselectText}" href="#" class="remove user">${deselectText}</a>
                </li>
              </ul>
              <div class="pageNav_results_userPanel" id="selected_user_list_pagination">
              </div>
            </div>
          </div>
          </c:if>

          <form action="${validationURL}" id="selection" method="POST">
            <input type="hidden" name="UserOrGroupSelection" value="true"/>
            <input id="group-selection" type="hidden" name="GroupSelection" value=""/>
            <input id="user-selection" type="hidden" name="UserSelection" value=""/>
            <br clear="all"/>
            <div id="validate">
              <fmt:message var="selectLabel" key="GML.validate"/>
              <fmt:message var="cancelLabel" key="GML.cancel"/>
              <a class="milieuBoutonV5" href="#"  ng-click="validate()">${selectLabel}</a>
              <c:if test='${not fn:endsWith(cancelationURL, "userpanel.jsp")}'>
                <a class="milieuBoutonV5" href="#" ng-click="cancel()">${cancelLabel}</a>
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
                constant('context', {
          currentUserId: '${currentUserId}',
          multiSelection: ${multipleSelection},
          selectionScope: '${selectionScope}',
          component: '${instanceId}',
          resource: '${resourceId}',
          roles: '${roles}',
          domain: '${domainId}'});

        /* declare the module userSelector and its dependencies (here on the silverpeas module) */
        var userSelector = angular.module('userSelector', ['silverpeas']);

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

            /* The root of the user group tree. It can be then considered as any other user group */
            var rootGroup = { name: '<fmt:message key="selection.RootUserGroups"/>', root: true,
                    subgroups: function() {
                      return UserGroup.get(arguments[0]);
                    },
                    users: function() {
                      var params = (arguments.length > 0 && arguments[0] ? arguments[0]:{});
                      params.group = 'all';
                      return User.get(params);
                    }
             };

            /* size of a page (items in a listing) within a pagination */
            var userPageSize = (context.selectionScope === 'user'? PageMaxSize:PageSize);
            var groupPageSize = (context.selectionScope === 'group'? PageMaxSize:PageSize);

            // fetch the users matching the specified parameters. The kind of users that fetched
            // depends on the current user displaying context (relationships, all users, or users of a
            // group)
            function fetchUsers(params) {
              var users;
              switch(displayedUserType) {
                case UserSource.All:
                  users = User.get(params);
                  break;
                case UserSource.Groups:
                  users = $scope.currentGroup.users(params);
                  break;
                case UserSource.Relationships:
                  users = me.relationships(params);
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
              var $pagination = $('#group_list_pagination');
              $pagination.children().remove();
              renderPagination($pagination, 'group_list', groupPageSize, $scope.groups.maxlength, function(pageNumber) {
                $scope.groups = $scope.currentGroup.subgroups({page: {number: pageNumber, size: groupPageSize}});
              });
            }

            /* updates the users listing with the specified ones */
            function updateUsersListing(users) {
              $scope.users = users;
              var $pagination = $('#user_list_pagination');
              $pagination.children().remove();
              renderPagination($pagination, 'user_list', userPageSize, $scope.users.maxlength, function(pageNumber) {
                $scope.users = fetchUsers({page: {number: pageNumber, size: userPageSize}});
              });
            }

            /* updates the groups selection with the specified ones */
            function updateGroupsSelection(groups) {
              $scope.selectedGroups.add(groups);
              var $pagination = $('#selected_group_list_pagination');
              $pagination.children().remove();
              renderPagination($pagination, 'selected_group_list', PageSize, $scope.selectedGroups.length(), function(pageNumber) {
                $scope.selectedGroups.page(pageNumber);
              });
            }

            /* updates the users selection with the specified ones */
            function updateUsersSelection(users) {
              $scope.selectedUsers.add(users);
              var $pagination = $('#selected_user_list_pagination');
              $pagination.children().remove();
              renderPagination($pagination, 'selected_user_list', PageSize, $scope.selectedUsers.length(), function(pageNumber) {
                $scope.selectedUsers.page(pageNumber);
              });
            }

            /* reset the text of the search input fields to their default value */
            function resetSearchFieldTexts() {
              $scope.searchedGroups = GroupSearchDefaultText;
              $scope.searchedUsers = UserSearchDefaultText;
            }

            /* maximize the the listing of groups over the listing of users */
            function maximizeGroupsListingPanel() {
              groupPageSize = PageMaxSize;
              $('.users_results_userPanel').hide();
            }

            /* maximize the the listing of users over the listing of groups */
            function maximizeUsersListingPanel() {
              userPageSize = PageMaxSize;
              $('.groups_results_userPanel').hide();
            }

            /* unmaximize the the listing of users and the listing of groups reappears */
            function unmaximizeUsersListingPanel() {
              userPageSize = PageSize;
              $('.groups_results_userPanel').show();
            }

            /* initialize the userSelection app by filling it with some values */
            function init() {
              if (context.selectionScope === 'group')
                maximizeGroupsListingPanel();
              else
                maximizeUsersListingPanel();
              displayedUserType = UserSource.Groups;
              groupPageSize = PageSize;
              UserGroup.get({page: {number: 1, size: GroupsFilterSizeStep}}).then(setGroupsFilter);
              UserGroup.get({page: {number:1, size: groupPageSize}}).then(updateGroupsListing);
              $scope.currentGroup = rootGroup;
              User.get({page: {number:1, size: userPageSize}}).then(updateUsersListing);
              User.get(${currentUserId}).then(function(user) {
                $scope.me = user;
              });
              $scope.selectedUsers = new Selection(context.multiSelection, PageSize);
              for(var i = 0; i < preselectedUsers.length; i++) {
                User.get(preselectedUsers[i]).then(function(user) {
                  $scope.selectedUsers.add(user);
                });
              }
              $scope.selectedGroups = new Selection(context.multiSelection, PageSize);
              for(var i = 0; i < preselectedUserGroups.length; i++) {
                UserGroup.get(preselectedUserGroups[i]).then(function(group) {
                  $scope.selectedGroups.add(group);
                });
              }
              resetSearchFieldTexts();
            }

            /* displays the nexts given number of groups in the filter of groups */
            $scope.displayNextGroups = function() {
              var pageNumber = ($scope.groupsFilter.length / GroupsFilterSizeStep) + 1;
              $scope.currentGroup.subgroups({page: {number: pageNumber, size: GroupsFilterSizeStep}}).then(updateGroupsFilter);
            };

            /* select all the user groups present in the corresponding listing panel */
            $scope.selectAllGroups = function() {
              $scope.currentGroup.subgroups().then(updateGroupsSelection);
            };

            /* select all the users present in the corresponding listing panel */
            $scope.selectAllUsers = function() {
              fetchUsers().then(updateUsersSelection);
            };

            /* select the specified user */
            $scope.selectUser = function(user) {
              if (!context.multipleSelection)
                $scope.selectedGroups.clear();
              $scope.selectedUsers.add(user);
            };

            /* select the specified group */
            $scope.selectGroup = function(group) {
              if (!context.multipleSelection)
                $scope.selectedUsers.clear();
              $scope.selectedGroups.add(group);
            };

            $scope.deselectAllGroups = function() {
              $scope.selectedGroups.clear();
            };

            $scope.deselectAllUsers = function() {
              $scope.selectedUsers.clear();
            };

            $scope.deselectUser = function(user) {
              $scope.selectedUsers.remove(user);
            };

            $scope.deselectGroup = function(group) {
              $scope.selectedGroups.remove(group);
            };

            /* renders all the current user relationships */
            $scope.goToMyContacts = function() {
              maximizeUsersListingPanel();
              displayedUserType = UserSource.Relationships;
              $scope.me.relationships({page: {number:1, size: userPageSize}}).then(updateUsersListing);
              resetSearchFieldTexts();
              highlightFilter($('#filter_contact'));
              maximizeUsersListingPanel();
            };

            /* renders all the users */
            $scope.goToAllUsers = function() {
              maximizeUsersListingPanel();
              displayedUserType = UserSource.All;
              User.get({page: {number:1, size: userPageSize}}).then(updateUsersListing);
              resetSearchFieldTexts();
              highlightFilter($('#filter_users'));
            };

            /* renders the subgroups of the specified group or all the groups if it is the root of the tree */
            $scope.goToGroup = function(group) {
              $('#breadcrumb').breadcrumb('set', group);
            };

            /* search the users that match the name entered by the user in the searchbox */
            $scope.searchMatchingUsers = function() {
              var text = $scope.searchedUsers;
              if (text && text !== UserSearchDefaultText && text.length >= 3)
                $scope.users = fetchUsers({name: name + '*', page: {number: 1, size: userPageSize}});
            };

            /* search the groups that match the name entered by the group in the searchbox */
            $scope.searchMatchingGroups = function() {
              var text = $scope.searchedGroups;
              if (text && text !== GroupSearchDefaultText && text.length >= 3)
                $scope.groups = $scope.currentGroup.subgroups({name: text + '*', page: {number: 1, size: groupPageSize}});
            };

             // validate the selection of users and/or of user groups.
            $scope.validate = function() {
              var selectedGroupIds = $scope.selectedGroups.itemIdsAsString();
              var selectedUserIds = $scope.selectedUsers.itemIdsAsString();
       <c:choose>
        <c:when test="${hotSetting}">
              var selectedGroupNames = $scope.selectedGroups.itemNamesAsString();
              var selectedUserNames = $scope.selectedUsers.itemNamesAsString();
              var selectionIdField = '<c:out value="${selection.htmlFormElementId}"/>';
              var selectionNameField = '<c:out value="${selection.htmlFormElementName}"/>';
              window.opener.$('#' + selectionIdField).val((selectedUserIds.length > 0 ? selectedUserIds : selectedGroupIds));
              window.opener.$('#' + selectionNameField).val((selectedUserNames.length > 0 ? selectedUserNames : selectedGroupNames));
              window.close();
        </c:when>
        <c:otherwise>
              $("input#group-selection").val(selectedGroupIds);
              $("input#user-selection").val(selectedUserIds);
              $("#selection").submit();
        </c:otherwise>
      </c:choose>
            };

            // cancel the selection and go back to the caller.
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
                unmaximizeUsersListingPanel();
                displayedUserType = UserSource.Groups;
                group.subgroups({page: {number:1, size: GroupsFilterSizeStep}}).then(setGroupsFilter);
                group.subgroups({page: {number:1, size: groupPageSize}}).then(updateGroupsListing);
                group.users({page: {number:1, size: userPageSize}}).then(updateUsersListing);
                $scope.currentGroup = group;
                resetSearchFieldTexts();
                highlightFilter($('#breadcrumb'));
              }
            });

            /* the search input field's treatment for the user groups */
            $('#group_search').focus(function() {
              if ($scope.searchedGroups === GroupSearchDefaultText) {
                $scope.searchedGroups = "";
                $scope.$apply();
              }
            }).blur(function() {
              if ($scope.searchedGroups.length === 0) {
                  $scope.searchedGroups = GroupSearchDefaultText;
                  $scope.$apply();
              }
            }).keypress(function(event) {
              if (event.which === 13)
                searchGroupsMatching($scope.searchedGroups);
            });;

            /* the search input field's treatment for the users */
            $('#user_search').focus(function() {
              if ($scope.searchedUsers === UserSearchDefaultText) {
                $scope.searchedUsers = "";
                $scope.$apply();
              }
            }).blur(function() {
              if ($scope.searchedUsers.length === 0) {
                  $scope.searchedUsers = UserSearchDefaultText;
                  $scope.$apply();
              }
            }).keypress(function(event) {
              if (event.which === 13)
                searchUsersMatching($scope.searchedUsers);
            });

            /* render pagination on the specified HTML element and according to the other pagination parameters */
            function renderPagination($this, dataContainerId, pageSize, totalCount, onpage) {
              if (totalCount > 0) {
                $this.show();
                $this.smartpaginator({
                  display: 'single',
                  totalrecords: totalCount,
                  recordsperpage: pageSize,
                  length: 6,
                  datacontainer: dataContainerId,
                  dataelement: 'li',
                  next: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowRight.gif"/>'}),
                  prev: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowLeft.gif"/>'}),
                  first: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowDoubleLeft.gif"/>'}),
                  last: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowDoubleRight.gif"/>'}),
                  theme: 'pageNav',
                  onchange: onpage
                });
              } else {
                $this.hide();
              }
            };

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
