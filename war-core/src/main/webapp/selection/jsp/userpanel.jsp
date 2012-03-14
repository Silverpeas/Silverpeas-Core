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
<c:set var="roles"             value="${selection.extraParams.joinedProfileNames}"/>
<c:set var="validationURL"     value="${selection.goBackURL}"/>
<c:set var="cancelationURL"    value="${selection.cancelURL}"/>
<c:set var="hotSetting"        value="${selection.htmlFormName != null && fn:length(selection.htmlFormName) > 0}"/>

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
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <view:includePlugin name="pagination"/>
    <view:includePlugin name="breadcrumb"/>
    <title><fmt:message key="selection.UserSelectionPanel"/></title>
    <style  type="text/css" >
      html, body {height:100%; overflow:hidden; margin:0px; padding:0px}
      div.pageNav .pages_indication {display: none}
    </style>
    <script type="text/javascript" src="<c:url value='/util/javaScript/silverpeas-profile.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/selection/jsp/javaScript/selection.js'/>"></script>
    <script type="text/javascript">
      var selectedUsersLabel  = "<fmt:message key='selection.usersSelected'/>";
      var selectedUserLabel   = "<fmt:message key='selection.userSelected'/>";
      var selectedGroupsLabel = "<fmt:message key='selection.groupsSelected'/>";
      var selectedGroupLabel  = "<fmt:message key='selection.groupSelected'/>";
      var foundUsersLabel     = "<fmt:message key='selection.usersFound'/>";
      var foundUserLabel      = "<fmt:message key='selection.userFound'/>";
      var foundGroupsLabel    = "<fmt:message key='selection.groupsFound'/>";
      var foundGroupLabel     = "<fmt:message key='selection.groupFound'/>";
      var itemToSelect        = "<c:out value='${selectionScope}'/>";
      
      var language            = '<c:out value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>';
      var MaximizedPageSize   = 10;
      
      rootUserGroup.name      = '<fmt:message key="selection.RootUserGroups"/>';
      rootUserGroup.inComponent('<c:out value="${instanceId}"/>').withRoles('<c:out value="${roles}"/>');
      
      var allUsers = new UserProfileManagement({
        component: '<c:out value="${instanceId}"/>',
        roles: '<c:out value="${roles}"/>'
      });
      
      var me = new UserProfile({id: '<c:out value="${currentUserId}"/>'}).
        inComponent('<c:out value="${instanceId}"/>').
        withRoles('<c:out value="${roles}"/>');      
      var nameInUserSearch = null; // required for the pagination with the results of a search (as at each page, the users are loaded for that page, so
      // we have to remind the name on which users are filtered if any)
      
      // with a selection of both users and groups, the listing of users can be maximized, hidden the listing of
      // groups; in that case, only the users are displayed. By default, the listing of groups and the one of users
      // are printed out.
      var userListingPanelStatus = {
        maximized: false,
        maximizationTriggered: false,
        contactsToRender: false
      };
      
      function maximizeUserListingPanel() {
        $('.groups_results_userPanel').hide();
        userListingPanelStatus.maximized = true;
        userListingPanelStatus.maximizationTriggered = false;
      }
      
      function unmaximizeUserListingPanel() {
        $('.groups_results_userPanel').show();
        userListingPanelStatus.maximized = false;
        userListingPanelStatus.contactsToRender = false;
      }
    
      // the size of items to render within a pagination page
      function pageSize() {
        return (itemToSelect == 'usergroup' && !userListingPanelStatus.maximized ? CountPerPage: MaximizedPageSize);
      }
      
      <c:choose>
          <c:when test="${selection.selectedElements != null && fn:length(selection.selectedElements) > 0}">
            var preselectedUsers = '<c:out value="${fn:join(selection.selectedElements, ',')}"/>'.split(',');
            if (preselectedUsers[0] == "")
              preselectedUsers = [];
        </c:when>
        <c:otherwise>
          var preselectedUsers = [];
        </c:otherwise>
      </c:choose>
      <c:choose>
          <c:when test="${selection.selectedSets != null && fn:length(selection.selectedSets) > 0}">
            var preselectedUserGroups = '<c:out value="${fn:join(selection.selectedSets, ',')}"/>'.split(',');
            if (preselectedUserGroups[0] == "")
              preselectedUserGroups = [];
        </c:when>
        <c:otherwise>
          var preselectedUserGroups = [];
        </c:otherwise>
      </c:choose>
     
        var userSelection = new Selection(<c:out value="${multipleSelection}"/>, {
          itemsAdded: function(users) {
            for (var i = 0; i < users.length; i++) {
              $('#add_user_' + users[i].id).hide();
            }
            if (!groupSelection.isMultiple())
              groupSelection.clear();
            renderUserSelection();
          },
          itemsRemoved: function(users) {
            for (var i = 0; i < users.length; i++) {
              $('#add_user_' + users[i].id).show();
            }
            renderUserSelection();
          }
        });
        
        var groupSelection = new Selection(<c:out value="${multipleSelection}"/>, {
          itemsAdded: function(groups) {
            for (var i = 0; i < groups.length; i++) {
              $('#add_group_' + groups[i].id).hide();
            }
            if (!userSelection.isMultiple())
              userSelection.clear();
            renderUserGroupSelection();
          },
          itemsRemoved: function(groups) {
            for (var i = 0; i < groups.length; i++) {
              $('#add_group_' + groups[i].id).show();
            }
            renderUserGroupSelection();
          }
        });
      
        function selectedUserNamesToString() {
          var selection = '';
          for (var i =0; i <  userSelection.items.length - 1; i++)
            selection += userSelection.items[i].fullName + ',';
          if (userSelection.items.length > 0)
            selection += userSelection.items[userSelection.items.length - 1].fullName;
          return selection;
        }
        
        function selectedGroupNamesToString() {
          var selection = '';
          for (var i = 0; i < groupSelection.item.length - 1; i++)
            selection += groupSelection.item[i].name + '\n';
          if (groupSelection.item.length > 0)
            selection += groupSelection.item[groupSelection.item.length - 1].name;
          return selection;
        }
        
        function resetSearchText() {
      <c:if test='${fn:startsWith(selectionScope, "user")}'>
            $('#user_search').val('<fmt:message key="selection.searchUsers"/>');      
      </c:if>
      <c:if test='${fn:endsWith(selectionScope, "group")}'>
            $('#group_search').val('<fmt:message key="selection.searchUserGroups"/>');
      </c:if>
        }
      
        function autoresizeUserGroupFilters() {
          var height_container = $('.container_userPanel').outerHeight();
          var height_listing_groups = $('.listing_groups_filter').outerHeight();
          var height_listing = $('.listing_filter').outerHeight();
          var height_title = $('#filter_userPanel .title').outerHeight();		
          var new_height_listing_groups = height_container - (height_listing-height_listing_groups)-height_title;

          $('.listing_groups_filter').css('height',new_height_listing_groups+'px');
        }
      
        function renderSearchBox($container) {
          var keyEnter = 13;
          if ($container.attr('id') == 'group_search') {
            var defaultText = "<fmt:message key='selection.searchUserGroups'/>";
            var search = function() {
              var name = $container.val();
              $('#breadcrumb').breadcrumb('current', function(group) {
                group.onChildren(name + '*', renderFilteredUserGroups);
              });
            }
          } else if ($container.attr('id') == 'user_search') {
            var defaultText = "<fmt:message key='selection.searchUsers'/>";
            var search = function() {
              nameInUserSearch = $container.val() + '*';
              if (userListingPanelStatus.contactsToRender)
                me.onRelationships(nameInUserSearch, 1, pageSize(), updateFilteredUsers);
              else
                $('#breadcrumb').breadcrumb('current', function(group) {
                  group.onUsers(nameInUserSearch, 1, pageSize(), renderFilteredUsers);
                });
            }
          }
          $container.focus(function() {
            var currentVal = $(this).val();
            if (currentVal == defaultText)
            $(this).val('');
          }).blur(function() {
            var currentVal = $(this).val();
            if (currentVal.length == 0)
              $(this).val(defaultText);
          }).autocomplete({
            minLength: 3,
            source: [],
            search: function() {
              search();
            } 
          }).keypress(function(event) {
            if (event.which == keyEnter)
              search();
          });
        }
      
        function renderPaginationFor(size, dataContainer) {
          var pagination, changedPage = null, pagesize = CountPerPage;
          if (dataContainer == 'group_list')
            pagination = $('#group_list_pagination');
          else if (dataContainer == 'selected_group_list')
            pagination = $('#selected_group_list_pagination');
          else if (dataContainer == 'user_list') {
            pagination = $('#user_list_pagination');
            changedPage = function(page) {
              if (userListingPanelStatus.contactsToRender)
                me.onRelationships(nameInUserSearch, page, pageSize(), updateFilteredUsers);
              else if (userListingPanelStatus.maximized)
                allUsers.get({
                  pagination: {
                    page: page,
                    count: MaximizedPageSize
                  }
                }, updateFilteredUsers);
              else
                $('#breadcrumb').breadcrumb('current', function(group) {
                  group.onUsers(nameInUserSearch, page, pageSize(), updateFilteredUsers);
                });
            }
          } else if (dataContainer == 'selected_user_list')
            pagination = $('#selected_user_list_pagination');
        
          if (size > 0) {
            pagination.show();
            pagination.smartpaginator({
              display: 'single',
              totalrecords: size,
              recordsperpage: pageSize(),
              length: 6,
              datacontainer: dataContainer, 
              dataelement: 'li',
              next: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowRight.gif"/>'}),
              prev: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowLeft.gif"/>'}),
              first: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowDoubleLeft.gif"/>'}),
              last: $('<img>', {src: '<c:url value="/util/viewGenerator/icons/arrows/arrowDoubleRight.gif"/>'}),
              theme: 'pageNav',
              onchange: changedPage
            });
          } else {
            pagination.hide();
          }
        }
      
        function renderUserGroupSelection() {
          var text = (groupSelection.items.length <= 1 ? selectedGroupLabel: selectedGroupsLabel);
          $('#group_selected_count').text(groupSelection.items.length + ' ' + text);
          if (groupSelection.isMultiple()) {
            if (groupSelection.items.length == 0)
              $('.listing_groups a.remove_all').hide();
            else
              $('.listing_groups a.remove_all').show();
          }
          $('#selected_group_list').slideUp('fast', function() {
            $('#selected_group_list').children().remove();
            for(var i = 0; i < groupSelection.items.length; i++) {
              renderUserGroup($('#selected_group_list'), i, groupSelection.items[i]);
            }
            $('#selected_group_list').slideDown('fast');
            renderPaginationFor(groupSelection.items.length, 'selected_group_list');
          });
        }
      
        function renderUserSelection() {
          var text = (userSelection.items.length <= 1 ? selectedUserLabel: selectedUsersLabel);
          $('#user_selected_count').text(userSelection.items.length + ' ' + text);
          if (userSelection.isMultiple()) {
            if (userSelection.items.length == 0)
              $('.listing_users a.remove_all').hide();
            else
              $('.listing_users a.remove_all').show();
          }
          $('#selected_user_list').slideUp('fast', function() {
            $('#selected_user_list').children().remove();
            for(var i = 0; i < userSelection.items.length; i++) {
              renderUser($('#selected_user_list'), i, userSelection.items[i]);
            }
            $('#selected_user_list').slideDown('fast');
            renderPaginationFor(userSelection.items.length, 'selected_user_list');
          });
        }
      
        function renderUserGroup($container, order, theGroup) {
          var style = (order % 2 == 0 ? 'odd':'even'), $operation = null;
          if ($container.attr('id') == 'group_list') {
            var id = 'group_' + theGroup.id;
            $operation = $('<a>', {id: 'add_group_' + theGroup.id, title: '<fmt:message key="selection.AddToSelection"/>', href: '#'}).
              addClass('add').addClass('group').text('<fmt:message key="selection.AddToSelection"/>').click(function() {
              groupSelection.add(theGroup);
            });
            if (groupSelection.isSelected(theGroup))
              $operation.hide();
            
            var index = preselectedUserGroups.indexOf(theGroup.id);
            if (index > -1) {
              groupSelection.add(theGroup);
              preselectedUserGroups.splice(index, 1);
            }
          } else {
            var id = 'selected_group_' + theGroup.id;
            $operation = $('<a>', {title: '<fmt:message key="selection.RemoveFromSelection"/>', href: '#'}).
              addClass('remove').addClass('group').text('<fmt:message key="selection.RemoveFromSelection"/>').click(function() {
              groupSelection.remove(theGroup);
            });
          }
          
          $('<li>', {id: id}).addClass('line').addClass(style).
            append($('<div>').addClass('avatar').append($('<img>', {alt: '', src: webContext + '/util/icons/component/groupe_Type_gestionCollaborative.png'}))).
            append($('<span>').addClass('name_group').text(theGroup.name)).
            append($('<span>').addClass('nb_user_group').text(theGroup.userCount + ' ' + '<fmt:message key="GML.user_s"/>')).
            append($('<span>').addClass('sep_nb_user_group').text(' - ')).
            append($('<span>').addClass('domain_group').text(theGroup.domainName)).
            append($operation).appendTo($container);
        }
        
        function renderUserGroupFilter(group) {
          $('<li>').append($('<a>', { href: '#' }).addClass('filter').
            append(group.name).
            append($('<span>').addClass('nb_results_by_filter').append('(' + group.userCount + ')')).click(function() {
            $('#breadcrumb').breadcrumb('set', group);
          })).
            appendTo($('ul.listing_groups_filter'));
        }
      
        function renderFilteredUserGroups(groups, additionalRendering) {
          var text = (groups.length <= 1 ? foundGroupLabel: foundGroupsLabel);
          if (groupSelection.isMultiple()) {
            if (groups.length == 0 && groupSelection.isMultiple())
              $('.listing_groups a.add_all').hide();
            else
              $('.listing_groups a.add_all').show();
          }
          $('#group_result_count').text(groups.length + ' ' + text);
          $('#group_list').slideUp('fast', function() {
            $('#group_list').children().remove();
            for(var i = 0; i < groups.length; i++) {
              renderUserGroup($('#group_list'), i, groups[i]);
              if (additionalRendering)
                additionalRendering(groups[i]);
            }
            $('#group_list').slideDown('fast');
            renderPaginationFor(groups.length, 'group_list');
          });
        }
        
        function renderUserGroups(groups) {
          if (itemToSelect == 'user')
            for(var i = 0; i < groups.length; i++)
              renderUserGroupFilter(groups[i]);
          else
            renderFilteredUserGroups(groups, renderUserGroupFilter);
        }
        
        function loadPreselectionOfUsers() {
          if (preselectedUsers && preselectedUsers.length > 0) {
            for(var i in preselectedUsers) {
              var aPreselectedUser = new UserProfile({id: preselectedUsers[i]}).load();
              userSelection.add(aPreselectedUser);
            }
          }
          if (userSelection.size() == 0) {
            renderUserSelection();
          }
        }
      
        function renderUser($container, order, theUser) {
          var style = (order % 2 == 0 ? 'odd':'even'), $operation = null;
          if ($container.attr('id') == 'user_list') {
            var id = 'user_' + theUser.id;
            $operation = $('<a>', {id: 'add_user_' + theUser.id, title: '<fmt:message key="selection.AddToSelection"/>', href: '#'}).
              addClass('add').addClass('user').text('<fmt:message key="selection.AddToSelection"/>').click(function() {
              userSelection.add(theUser);
            });
            if (userSelection.isSelected(theUser))
              $operation.hide();
          } else {
            var id = 'selected_user_' + theUser.id;
            $operation = $('<a>', {title: '<fmt:message key="selection.RemoveFromSelection"/>', href: '#'}).
              addClass('remove').addClass('user').text('<fmt:message key="selection.RemoveFromSelection"/>').click(function() {
              userSelection.remove(theUser);
            });
          }
          $('<li>', {id: id}).addClass('line').addClass(style).
            append($('<div>').addClass('avatar').append($('<img>', {alt: '', src: theUser.avatar}))).
            append($('<span>').addClass('name_user').text(theUser.lastName + ' ' + theUser.firstName)).
            append($('<span>').addClass('mail_user').text(theUser.eMail)).
            append($operation).appendTo($container);
        }
      
        function renderFilteredUsers(users, paginationChange) {
          var text = (users.length <= 1 ? foundUserLabel: foundUsersLabel);
          if (userSelection.isMultiple()) {
            if (users.length == 0)
              $('.listing_users a.add_all').hide();
            else
              $('.listing_users a.add_all').show();
          }
          $('#user_result_count').text(users.maxlength + ' ' + text);
          $('#user_list').slideUp('fast', function() {
            $('#user_list').children().remove();
            for(var i = 0; i < users.length; i++) {
              renderUser($('#user_list'), i, users[i]);
            }
            $('#user_list').slideDown('fast');
            if (!paginationChange)
              renderPaginationFor(users.maxlength, 'user_list');
          });
        }
        
        function updateFilteredUsers(users) {
          renderFilteredUsers(users, true);
        }
      
        // validate the selection of users and/or of user groups.
        // the validation can be performed in two different ways:
        // - the form with the user and group selections is posted to the caller,
        // - or the caller form parameters for the selection are set directly, leaving the caller to
        //   process the selection
        function validateSelection() {
          var selectedGroupIds = groupSelection.selectedItemIdsToString();
          var selectedUserIds = userSelection.selectedItemIdsToString();
      <c:choose>
        <c:when test="${hotSetting}">
            var selectedGroupNames = selectedGroupNamesToString();
            var selectedUserNames = selectedUserNamesToString();
            var selectionIdField   = '<c:out value="${selection.htmlFormElementId}"/>';
            var selectionNameField = '<c:out value="${selection.htmlFormElementName}"/>';
            window.opener.$('#' + selectionIdField).val((selectedUserIds.length > 0 ? selectedUserIds:selectedGroupIds));
            window.opener.$('#' + selectionNameField).val((selectedUserNames.length > 0 ? selectedUserNames:selectedGroupNames));
            window.close();
        </c:when>
        <c:otherwise>
            $("input#group-selection").val(selectedGroupIds);
            $("input#user-selection").val(selectedUserIds);
            $("#selection").submit();
        </c:otherwise>
      </c:choose>
        }
      
        // cancel the selection and go back to the caller.
        function cancelSelection() {
          $('input[name="UserOrGroupSelection"]').val('false');
          $("#selection").attr("action", "<c:out value='${cancelationURL}'/>");
          $("#selection").submit();
        }
        
        function highlightFilter($this) {
           $('.filter').removeClass('select');
           $this.addClass('select');
        }
      
        $(document).ready(function() {
        
          // the rendering of the content within both the group listing panel and the user listing
          // panel is triggered by the breadcrumb through user actions
          $('#breadcrumb').breadcrumb( {
            root: rootUserGroup,
            onchange: function(group) {
              resetSearchText();
              if (userListingPanelStatus.maximizationTriggered)
                maximizeUserListingPanel();
              else if (userListingPanelStatus.maximized)
                unmaximizeUserListingPanel();
              var $container = $('ul.listing_groups_filter');
              $container.slideUp('fast', function() {
                $container.children().remove();
                group.onChildren(renderUserGroups);
              });
              $container.slideDown('fast', function() {
                autoresizeUserGroupFilters();
              });
              if (itemToSelect.indexOf('user') > -1) {
                if (userListingPanelStatus.contactsToRender) {
                  me.onRelationships(1, pageSize(), renderFilteredUsers)
                } else if (userListingPanelStatus.maximized) {
                  allUsers.get({
                    pagination: {
                      page: 1,
                      count: MaximizedPageSize
                    }
                  }, renderFilteredUsers);
                } else {
                  group.onUsers(1, pageSize(), renderFilteredUsers);
                }
              }
              highlightFilter($('#breadcrumb'));
            }
          });
      
      <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          if (groupSelection.isMultiple()) {
            $('.listing_groups a.add_all').click(function() {
              $('#breadcrumb').breadcrumb('current', function(group) {
                groupSelection.add(group.children());
              });
            });
          } else {
            $('.listing_groups a.add_all').remove();
            $('.listing_groups a.remove_all').remove();
          }
          
          renderSearchBox($('#group_search'));
          renderUserGroupSelection();
      </c:if>
    
      <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
            $('<li>').append($('<a>', {href: '#'}).addClass('filter').text('<fmt:message key="selection.AllUsers"/>').click(function() {
              userListingPanelStatus.maximizationTriggered = true;
              userListingPanelStatus.contactsToRender = false;
              $('#breadcrumb').breadcrumb('set', rootUserGroup);
              highlightFilter($(this));
            })).prependTo($('ul.listing_filter'));
            
            $('<li>').append($('<a>', {href: '#'}).addClass('filter').text('<fmt:message key="selection.MyContacts"/>').click(function() {
              userListingPanelStatus.maximizationTriggered = true;
              userListingPanelStatus.contactsToRender = true;
              $('#breadcrumb').breadcrumb('set', rootUserGroup);
              highlightFilter($(this));
            })).prependTo($('ul.listing_filter'));
        
            renderSearchBox($('#user_search'));
          
            if (userSelection.isMultiple()) {
              $('.listing_users a.add_all').click(function() {
                if (userListingPanelStatus.contactsToRender)
                  me.onRelationships(nameInUserSearch, userSelection.add);
                else
                  $('#breadcrumb').breadcrumb('current', function(group) {
                    group.onUsers(nameInUserSearch, userSelection.add);
                  });
              });
            } else {
              $('.listing_users a.add_all').remove();
              $('.listing_users a.remove_all').remove();
            }    
 
            loadPreselectionOfUsers();
      </c:if>
				
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
  </head>
  <body class="userPanel">
    <div class="container_userPanel">
      <div id="filter_userPanel">
        <h4 class="title"><fmt:message key="selection.Filter"/></h4>
        <ul class="listing_filter">
          <li><div id="breadcrumb" class="select filter"></div>
            <ul class="listing_groups_filter">
            </ul>
          </li>
        </ul>
      </div>
      <div id="results_userPanel">
        <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          <div class="groups_results_userPanel">
            <div class="container_input_search"><input id="group_search" class="search autocompletion" type="text" value="<fmt:message key='selection.searchUserGroups'/>" /></div>
            <div class="listing_groups">
              <p id="group_result_count" class="nb_results"></p>
              <a class="add_all" title="<fmt:message key='selection.AddAllGroupsToSelection'/>" href="#"><fmt:message key="selection.AddAllGroups"/></a>
              <ul id="group_list">
              </ul>
              <div id="group_list_pagination" class="pageNav_results_userPanel">
              </div>
            </div>
          </div>
        </c:if>
        <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          <div class="users_results_userPanel">
            <div class="container_input_search"><input id="user_search" class="search autocompletion " type="text" value="<fmt:message key='selection.searchUsers'/>" /></div>
            <div class="listing_users">
              <p id="user_result_count" class="nb_results"></p>
              <a class="add_all" title="<fmt:message key='selection.AddAllUsersToSelection'/>" href="#"><fmt:message key="selection.AddAllUsers"/></a>
              <ul  id="user_list">
              </ul>
              <div id="user_list_pagination" class="pageNav_results_userPanel">
              </div>
            </div>
          </div>
        </c:if>
      </div>

      <div id="selected_userPanel">
        <div class="container">
          <h4 class="title"><c:out value="${selectionTitle}"/></h4>
          <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
            <div class="groups_selected_userPanel">
              <div class="listing_groups">
                <p id="group_selected_count" class="nb_results"></p>
                <a class="remove_all" title="<fmt:message key="selection.RemoveAllGroupsFromSelection"/>" href="javascript: groupSelection.clear();"><fmt:message key="selection.Empty"/></a>
                <ul id="selected_group_list">
                </ul>
                <div id="selected_group_list_pagination" class="pageNav_results_userPanel">
                </div>
              </div>
            </div>
          </c:if>
          <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
            <div class="users_selected_userPanel">
              <div class="listing_users">
                <p id="user_selected_count" class="nb_results"></p>
                <a class="remove_all" title="<fmt:message key="selection.RemoveAllUsersFromSelection"/>" href="javascript: userSelection.clear();"><fmt:message key="selection.Empty"/></a>
                <ul id="selected_user_list">
                </ul>
                <div id="selected_user_list_pagination" class="pageNav_results_userPanel">
                </div>
              </div>
            </div>
          </c:if>

          <form action="<c:out value='${validationURL}'/>" id="selection" method="POST">
            <input type="hidden" name="UserOrGroupSelection" value="true"/>
            <input id="group-selection" type="hidden" name="GroupSelection" value=""/>
            <input id="user-selection" type="hidden" name="UserSelection" value=""/>
            <br clear="all"/>
            <div id="validate">
              <fmt:message var="selectLabel" key="GML.validate"/>
              <fmt:message var="cancelLabel" key="GML.cancel"/>
              <view:buttonPane>
                <view:button label="${selectLabel}" action="javascript: validateSelection();"/>
                <c:if test='${not fn:endsWith(cancelationURL, "userpanel.jsp")}'>
                  <view:button label="${cancelLabel}" action="javascript: cancelSelection();"/>
                </c:if>
              </view:buttonPane>
            </div>
          </form>
        </div>
      </div>

    </div>

  </body>
</html>
