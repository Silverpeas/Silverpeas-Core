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


      </script>

    </div>

    <script type="text/javascript">
      /* some global parameters */
      PageSize = 6;
      PageMaxSize = 10;
      RenderingUserType = { all: 0, relationships: 1, group: 2 };

      /* the user selector as an Ember application */
      window.App = Ember.Application.create();
      /*App.Router.map(function() {
        this.resource('userselector', { path: '/' });
      });*/
      App.Store = new ProfileStore();

      App.User = User;

      App.UserGroup = UserGroup;

      App.Scope = Ember.Object.extend({
        me: App.User.find('${currentUserId}'),
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
