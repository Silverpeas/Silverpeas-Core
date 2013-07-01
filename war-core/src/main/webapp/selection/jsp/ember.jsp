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
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <view:includePlugin name="pagination"/>
    <view:includePlugin name="breadcrumb"/>
    <script type="text/javascript" src="/silverpeas/util/javaScript/handlebars-1.0.0-rc.4.js"></script>
    <script type="text/javascript" src="/silverpeas/util/javaScript/ember-1.0.0-rc.6.js"></script>
    <script type="text/javascript" src="/silverpeas/util/javaScript/ember-data-latest.js"></script>
    <script type="text/javascript" src="/silverpeas/selection/jsp/javaScript/silverpeas-ember.js"></script>
    <title><fmt:message key="selection.UserSelectionPanel"/></title>
    <style  type="text/css" >
      html, body {height:100%; overflow:hidden; margin:0px; padding:0px}
      div.pageNav .pages_indication {display: none}
    </style>
  </head>
  <body class="userPanel">
    <div class="container_userPanel">

      <script type="text/x-handlebars">

        <div id="filter_userPanel">
          <h4 class="title"><fmt:message key="selection.Filter"/></h4>
          <ul class="listing_filter">
          <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
            <li><a href="#" id="filter_contact" class="filter" {{ action 'swithToMyContacts' }} ><fmt:message key="selection.MyContacts"/></a></li>
            <li><a href="#" id="filter_users" class="filter select" {{ action 'switchToAllUsers' }} ><fmt:message key="selection.AllUsers"/></a></li>
            </c:if>
            <li id="filter_groups">
              <div class="filter" id="breadcrumb"></div>
              <ul class="listing_groups_filter">
                {{#each group in groupsFilter}}
                <li>
                  <a href="#" class="filter" {{ action 'switchToGroup' group }}>{{ name }}<span class="nb_results_by_filter"> ({{ group.userCount }})</span></a>
                </li>
                {{/each}}
              </ul>
            </li>
          </ul>
        </div>

        <div id="results_userPanel">
        <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          <div class="groups_results_userPanel">
            <div class="container_input_search">
              <input type="text" class="search autocompletion" id="group_search"/>
            </div>
            <div class="listing_groups">
              <p class="nb_results" id="group_result_count">{{ groups.maxlength }} <fmt:message key='selection.groupsFound'/></p>
              {{#if scope.multipleSelection }}
              <a href="#" title="<fmt:message key='selection.AddAllGroupsToSelection'/>" class="add_all" {{ action 'selectAllGroups' }} ><fmt:message key="selection.AddAllGroups"/></a>
              {{/if}}
              <ul id="group_list">
                {{#each group in groups}}
                {{#if groups.indexOf(group) % 2 === 0}}
                <li class="line even">
                {{else}
                <li class="line odd">
                {{/if}}
                  <div class="avatar"><img alt="" src="/silverpeas/util/icons/component/groupe_Type_gestionCollaborative.png"></img></div>
                  <span class="name_group">{{ group.name }}</span><span class="nb_user_group">{{ group.userCount + ' ' + '<fmt:message key="GML.user_s"/>' }}</span>
                  <span class="sep_nb_user_group"> - </span><span class="domain_group">{{ group.domainName }}</span>
                  {{#if selectedGroups.indexOf(group) < 0}}
                  <a href="#" title="<fmt:message key='selection.AddToSelection'/>" class="add group" {{ action 'selectGroup' group }}"><fmt:message key="selection.AddToSelection"/></a>
                  {{/if}}
                </li>
                {{/each}}
              </ul>
              <div id="group_list_pagination" class="pageNav_results_userPanel"></div>
            </div>
          </div>
        </c:if>
        <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_results_userPanel">
            <div class="container_input_search">
              <input type="text" class="search autocompletion" id="user_search"/>
            </div>
            <div class="listing_users">
              <p class="nb_results" id="user_result_count">{{ users.maxlength }} <fmt:message key='selection.usersFound'/></p>
              {{#if scope.multipleSelection}}
              <a href="#" title="<fmt:message key='selection.AddAllUsersToSelection'/>" class="add_all" {{ action 'selectAllUsers' }}><fmt:message key="selection.AddAllUsers"/></a>
              {{/if}}
              <ul id="user_list">
                {{#each user in users}}
                {{#if users.indexOf(user) % 2 === 0}}
                <li class="line odd">
                {{else}}
                <li class="line even">
                {{/if}}
                  <div class="avatar"><img {{ attrBind src="user.avatar" }} alt="avatar"/></div>
                  <span class="name_user">{{ user.lastName + ' ' + user.firstName }} </span>
                  <span class="mail_user">{{ user.eMail }}</span><span class="sep_mail_user"> - </span><span class="domain_user">{{ user.domainName }}</span>
                  {{#if selectedUsers.indexOf(user) < 0}}
                  <a href="#" title="<fmt:message key='selection.AddToSelection'/>" class="add user" {{ action 'selectUser' user}}><fmt:message key="selection.AddToSelection"/></a>
                  {{/if}}
                </li>
                {{/each}}
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
                {{#if scope.multipleSelection}}
                <a href="#" title="${removeAllGroupsFromSelectionText}" class="remove_all" {{ action "deselectAllGroups" }><fmt:message key="selection.Empty"/></a>
                {{/if}}
                <ul id="selected_group_list">
                  {{#each group in selectedGroups.currentpage}}
                  {{#if selectedGroups.indexOf(group) % 2 === 0}}
                  <li class="line odd">
                  {{else}}
                  <li class="line even">
                  {{/if}}
                    <div class="avatar"><img alt="" src="/silverpeas/util/icons/component/groupe_Type_gestionCollaborative.png"/></div>
                    <span class="name_group">{{ group.name }}</span>
                    <span class="nb_user_group">{{ group.userCount + ' ' + '<fmt:message key="GML.user_s"/>' }}</span>
                    <span class="sep_nb_user_group"> - </span><span class="domain_group">{{ group.domainName }}</span>
                    <a title="${deselectText}" href="#" class="remove group" {{ action "deselectGroup" group }}>${deselectText}</a>
                  </li>
                  {{/each}}
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
                {{#if scope.multipleSelection}}
                <a href="#" {{ action "deselectAllUsers" }} title="${removeAllUsersFromSelectionText}" class="remove_all"><fmt:message key="selection.Empty"/></a>
                {{/if}}
                <ul id="selected_user_list">
                  {{#each group in selectedUsers.currentpage}}
                  {{#if selectedUsers.indexOf(group) % 2 === 0}}
                  <li class="line odd">
                  {{else}}
                  <li class="line even">
                  {{/if}}
                    <div class="avatar"><img {{ attrBind src="user.avatar" }} alt="avatar"/></div>
                    <span class="name_user">{{ user.lastName + ' ' + user.firstName }} </span>
                    <span class="mail_user">{{ user.eMail }}</span><span class="sep_mail_user"> - </span><span class="domain_user">{{ user.domainName }}</span>
                    <a {{ action "deselectUser" user }} title="${deselectText}" href="#" class="remove user">${deselectText}</a>
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
              <a href="#" class="milieuBoutonV5" {{ action "validate" }}>${selectLabel}</a>
              <c:if test='${not fn:endsWith(cancelationURL, "userpanel.jsp")}'>
                <a href="#" class="milieuBoutonV5" {{ action "cancel" }}>${cancelLabel}</a>
              </c:if>
            </div>
          </form>
        </div>
      </div>
      </script>

    </div>

    <script type="text/javascript">
      /* some global parameters */
      PageSize = 6;
      PageMaxSize = 10;
      RenderingUserType = { all: 0, relationships: 1, group: 2 };

      /* the user selector as an Ember application */
      window.app = Ember.Application.create();
      /*UserSelector.Router.map(function() {
        this.resource('userselector', { path: '/' });
      });*/

      App.Scope = Ember.Object.extend({
        me: User.find('${currentUserId}'),
        currentGroup: null,
        multipleSelection: ${multipleSelection},
        renderedUsers: RenderingUserType.all,
        myrelationships: function() {
          var me = this.get('me');
          return me.relationships(arguments);
        }
      });

      /* the main controller between the view and the data (users and groups) */
      App.ApplicationController = Ember.Controller.extend({
        scope: App.Scope.create(),
        selectedGroups: Selection.create({pagesize: PageSize, multipleSelection: ${multipleSelection}}),
        selectedUsers : Selection.create({pagesize: PageSize, multipleSelection: ${multipleSelection}}),
        users: User.find(),
        groups: [],
        groupsFilter: UserGroup.find(),
        swithToMyContacts: function() {
          var appScope = this.get('scope');
          this.set('users', appScope.myrelationships({page: '1;' + PageMaxSize}));
          this.get('scope').set('renderedUsers', RenderingUserType.all);
        },
        switchToAllUsers: function() {
          var appScope = this.get('scope');
          this.set('users', User.find({page: '1;' + PageMaxSize}));
          appScope.set('renderedUsers', RenderingUserType.all);
        },
        switchToGroup: function(group) {
          var appScope = this.get('scope');
          appScope.set('currentGroup', group);
          this.set('groupsFilter', group.subgroups());
          this.set('groups', this.get('groupsFilter').slice(0, PageSize));
          this.set('renderedUsers', RenderingUserType.group);
        },
        selectAllGroups: function() {
          var appScope = this.get('scope');
          if (appScope.get('currentGroup'))
            this.get('selectedGroups').add(appScope.get('currentGroup').subgroups());
          else
            this.get('selectedGroups').add(UserGroup.find());
        },
        selectGroup: function(group) {
          this.get('selectedGroups').add(group);
        },
        selectAllUsers: function() {
          var appScope = this.get('scope');
          if (this.get('renderedUsers') === RenderingUserType.group)
            this.get('selectedUsers').add(appScope.get('currentGroup').users());
          else if (this.scope.renderedUsers === RenderingUserType.all)
            this.get('selectedUsers').add(User.find());
          else
            this.get('selectedUsers').add(appScope.myrelationships());
        },
        selectUser: function(user) {
          this.get('selectedUsers').add(user);
        },
        deselectAllGroups: function() {
          this.get('selectedGroups').clear();
        },
        deselectGroup: function(group) {
          this.get('selectedGroups').remove(group);
        },
        deselectAllUsers: function() {
          this.get('selectedUsers').clear();
        },
        deselectUser: function(group) {
          this.get('selectedUsers').remove(group);
        },
        validate: function() {
          var selectedGroupIds = this.get('selectedGroups').get('itemIdsAsString');
          var selectedUserIds = this.get('selectedUsers').get('itemIdsAsString');
      <c:choose>
        <c:when test="${hotSetting}">
          var selectedGroupNames = this.get('selectedGroups').get('itemNamesAsString');
          var selectedUserNames = this.get('selectedUsers').get('itemNamesAsString');
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
        },
        cancel: function() {
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
        }
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
