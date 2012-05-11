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
        renderedItems: null
      };
      
      // maximize the area within which the user profiles are rendered
      // the area hides the one within which the user groups are rendered
      function maximizeUserListingPanel() {
        $('.groups_results_userPanel').hide();
        userListingPanelStatus.maximized = true;
      }
      
      // unmaximize the area with which the user profiles are rendered
      // the area within which the user groups are rendered is yet showed
      function unmaximizeUserListingPanel() {
        $('.groups_results_userPanel').show();
        userListingPanelStatus.maximized = false;
        userListingPanelStatus.renderedItems = null;
      }
    
      // the size of items to render within a pagination page
      function pageSize() {
        return (itemToSelect == 'usergroup' && !userListingPanelStatus.maximized ? CountPerPage: MaximizedPageSize);
      }
      
      // what are the preselected user profiles and user groups (get at web page build)
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
     
        // the selection of user profiles
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
        
        // the selection of user groups
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
        
        // loads the profile of the preselected users so that they can be displayed in the user
        // selection panel
        function loadPreselectionOfUsers() {
          if (preselectedUsers && preselectedUsers.length > 0) {
            for(var i in preselectedUsers) {
              new UserProfile({id: preselectedUsers[i]}).load(userSelection.add);
            }
          }
          if (userSelection.size() == 0) {
            renderUserSelection();
          }
        }
        
        // the ui event processing in the user panel
        // each event is sent by an action of the user with an ui element and drives to a computation
        // into which different others ui elements can be updated
        var eventProcessing = new function() {
          
          var self = this;
          
          function renderUsersOfGroup(group) {
            if (itemToSelect.indexOf('user') > -1)
              group.onUsers(1, pageSize(), renderFilteredUsers);
          }
          
          function onEvent(event, processor, args) {
            self.event = event;
            processor(args);
            self.event = null;
          }
          
          this.event = null;
          
          // initializes the user panel content
          this.onInit = function() {
            onEvent('init', function() {
              rootUserGroup.onChildren(function(groups) {
                if (groups.length == 0) {
                  itemToSelect = 'user';
                  $('#filter_groups').remove();
                  $('.groups_results_userPanel').remove();
                  $('.groups_selected_userPanel').remove();
                  self.onAllUsers();
                } else {
                  renderUserGroups(groups);
                  renderUsersOfGroup(rootUserGroup);
                }
              });
            });
          }
          
          // a user group is selected by the user (a group in the breadcrumb trail or among the
          // subgroups of the current user group)
          this.onGroupChange = function() {
            onEvent('groupChange', function() {
              resetSearchText();
              if (userListingPanelStatus.maximized)
                unmaximizeUserListingPanel();
              $('#breadcrumb').breadcrumb('current', function(group) {
                group.onChildren(renderUserGroups);
                renderUsersOfGroup(group);
              });
            });
          }
          
          // all users in Silverpeas are asked by the user
          this.onAllUsers = function() {
            onEvent('allUsers', function() {
              resetSearchText();
              maximizeUserListingPanel();
              userListingPanelStatus.renderedItems = self.event;
              $('#breadcrumb').breadcrumb('set', rootUserGroup);
              allUsers.get({
                pagination: {
                  page: 1,
                  count: MaximizedPageSize
                },
                name: null}, renderFilteredUsers);
            });
          }
          
          // the contacts of the user are asked
          this.onMyContacts = function() {
            onEvent('myContacts', function() {
              resetSearchText();
              maximizeUserListingPanel();
              userListingPanelStatus.renderedItems = self.event;
              $('#breadcrumb').breadcrumb('set', rootUserGroup);
              me.onRelationships(1, pageSize(), renderFilteredUsers);
            });
          }
          
          // a search on the user profiles by their name
          this.onUserSearch = function(pattern) {
            onEvent('search', function() {
              nameInUserSearch = arguments[0];
              renderUsersInPage(1);
            }, pattern);
          }
          
          // a search on the user groups by their name
          this.onGroupSearch = function(pattern) {
            onEvent('search', function() {
              var name = arguments[0];
              $('#breadcrumb').breadcrumb('current', function(group) {
                group.onChildren(name, renderFilteredUserGroups);
              });
            }, pattern);
          }
        }
      
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
          for (var i = 0; i < groupSelection.items.length - 1; i++)
            selection += groupSelection.items[i].name + '\n';
          if (groupSelection.items.length > 0)
            selection += groupSelection.items[groupSelection.items.length - 1].name;
          return selection;
        }
        
        function resetSearchText() {
      <c:if test='${fn:startsWith(selectionScope, "user")}'>
            $('#user_search').val('<fmt:message key="selection.searchUsers"/>');
            nameInUserSearch = null;
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
      
        function renderUsersInPage(page, renderUsers) {
          if (renderUsers == null)
            renderUsers = function(users) {
              renderFilteredUsers(users, page > 1);
            }
          if (userListingPanelStatus.renderedItems == 'myContacts')
            me.onRelationships(nameInUserSearch, page, pageSize(), renderUsers);
          else if (userListingPanelStatus.renderedItems == 'allUsers') {
            var pagination = (page == null ? null: {
              page: page,
              count: pageSize()
            });
            allUsers.get({
              pagination: pagination,
              name: nameInUserSearch
            }, renderUsers);
          }    
          else
            $('#breadcrumb').breadcrumb('current', function(group) {
              group.onUsers(nameInUserSearch, page, pageSize(), renderUsers);
            });
        }
      
        function renderSearchBox($container) {
          var keyEnter = 13;
          if ($container.attr('id') == 'group_search') {
            var defaultText = "<fmt:message key='selection.searchUserGroups'/>";
            var search = function() {
              var name = $container.val() + '*';
              eventProcessing.onGroupSearch(name);
            }
          } else if ($container.attr('id') == 'user_search') {
            var defaultText = "<fmt:message key='selection.searchUsers'/>";
            var search = function() {
              var name = $container.val() + '*';
              eventProcessing.onUserSearch(name);
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
              renderUsersInPage(page);
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
          $('#selected_group_list').hide();
            $('#selected_group_list').children().remove();
            for(var i = 0; i < groupSelection.items.length; i++) {
              renderUserGroup($('#selected_group_list'), i, groupSelection.items[i]);
            }
            $('#selected_group_list').show();
            renderPaginationFor(groupSelection.items.length, 'selected_group_list');
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
          $('#selected_user_list').hide();
            $('#selected_user_list').children().remove();
            for(var i = 0; i < userSelection.items.length; i++) {
              renderUser($('#selected_user_list'), i, userSelection.items[i]);
            }
            $('#selected_user_list').show();
            renderPaginationFor(userSelection.items.length, 'selected_user_list');
        }
      
        function renderUserGroup($container, order, theGroup) {
          var style = (order % 2 == 0 ? 'odd':'even'), $operation = null;
          if ($container.attr('id') == 'group_list') {
            var id = 'group_' + theGroup.id;
            $operation = $('<a>', {id: 'add_group_' + theGroup.id, title: '<fmt:message key="selection.AddToSelection"/>', href: '#'}).
              addClass('add').addClass('group').text('<fmt:message key="selection.AddToSelection"/>').click(function() {
              groupSelection.add(theGroup);
            });
            
            var index = preselectedUserGroups.indexOf(theGroup.id);
            if (index > -1) {
              groupSelection.add(theGroup);
              preselectedUserGroups.splice(index, 1);
            }
            if (groupSelection.isSelected(theGroup))
              $operation.hide();
          } else {
            var id = 'selected_group_' + theGroup.id;
            $operation = $('<a>', {title: '<fmt:message key="selection.RemoveFromSelection"/>', href: '#'}).
              addClass('remove').addClass('group').text('<fmt:message key="selection.RemoveFromSelection"/>').click(function() {
              groupSelection.remove(theGroup);
            });
          }
          var domainName = (theGroup.domainName == 'internal' ? "<fmt:message key='GML.internalDomain'/>":theGroup.domainName);
          
          $('<li>', {id: id}).addClass('line').addClass(style).
            append($('<div>').addClass('avatar').append($('<img>', {alt: '', src: webContext + '/util/icons/component/groupe_Type_gestionCollaborative.png'}))).
            append($('<span>').addClass('name_group').text(theGroup.name)).
            append($('<span>').addClass('nb_user_group').text(theGroup.userCount + ' ' + '<fmt:message key="GML.user_s"/>')).
            append($('<span>').addClass('sep_nb_user_group').text(' - ')).
            append($('<span>').addClass('domain_group').text(domainName)).
            append($operation).appendTo($container);
        }
        
        function renderUserGroupFilter(group) {
          $('<li>').append($('<a>', { href: '#' }).addClass('filter').
            append(group.name).
            append($('<span>').addClass('nb_results_by_filter').append(' (' + group.userCount + ')')).click(function() {
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
          $('#group_list').hide();
            $('#group_list').children().remove();
            for(var i = 0; i < groups.length; i++) {
              renderUserGroup($('#group_list'), i, groups[i]);
              if (additionalRendering)
                additionalRendering(groups[i]);
            }
            $('#group_list').show();
            renderPaginationFor(groups.length, 'group_list');
        }
        
        function renderUserGroups(groups) {
          var $container = $('ul.listing_groups_filter');
          $container.hide();
          $container.children().remove();
          if (itemToSelect == 'user' || eventProcessing.event == 'myContacts' ||
              eventProcessing.event == 'allUsers')
            for(var i = 0; i < groups.length; i++)
              renderUserGroupFilter(groups[i]);
          else
            renderFilteredUserGroups(groups, renderUserGroupFilter);
          $container.show();
          autoresizeUserGroupFilters();
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
      
        function renderFilteredUsers(users, inSamePagination) {
          var text = (users.length <= 1 ? foundUserLabel: foundUsersLabel);
          if (userSelection.isMultiple()) {
            if (users.length == 0)
              $('.listing_users a.add_all').hide();
            else
              $('.listing_users a.add_all').show();
          }
          $('#user_result_count').text(users.maxlength + ' ' + text);
          $('#user_list').hide();
          $('#user_list').children().remove();
          for(var i = 0; i < users.length; i++) {
            renderUser($('#user_list'), i, users[i]);
          }
          $('#user_list').show();
          if (!inSamePagination)
            renderPaginationFor(users.maxlength, 'user_list');
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
            oninit: function() {
              eventProcessing.onInit();
            },
            onchange: function(group) {
              if (eventProcessing.event != null)
                group.onChildren(renderUserGroups);
              else
                eventProcessing.onGroupChange(group);
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
            $('<li>').append($('<a>', {href: '#', id: 'filter_users'}).addClass('filter').text('<fmt:message key="selection.AllUsers"/>').click(function() {
              eventProcessing.onAllUsers();
              highlightFilter($(this));
            })).prependTo($('ul.listing_filter'));
            
            $('<li>').append($('<a>', {href: '#', id: 'filter_contact'}).addClass('filter').text('<fmt:message key="selection.MyContacts"/>').click(function() {
              eventProcessing.onMyContacts();
              highlightFilter($(this));
            })).prependTo($('ul.listing_filter'));
        
            renderSearchBox($('#user_search'));
          
            if (userSelection.isMultiple()) {
              $('.listing_users a.add_all').click(function() {
                renderUsersInPage(null, userSelection.add);
              });
            } else {
              $('.listing_users a.add_all').remove();
              $('.listing_users a.remove_all').remove();
            }    
 
            loadPreselectionOfUsers();
      </c:if>
      		
		  try {
	      	  var browser = jQuery.uaMatch(navigator.userAgent).browser;
		      var documentWidth = $(document).width();
		      if (browser == "webkit") {
		      	documentWidth = "980";
		      }
	
	          if ($(window).width() < documentWidth) {
	             window.resizeTo(documentWidth,758); 
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
  </head>
  <body class="userPanel">
    <div class="container_userPanel">
      <div id="filter_userPanel">
        <h4 class="title"><fmt:message key="selection.Filter"/></h4>
        <ul class="listing_filter">
          <li id="filter_groups"><div id="breadcrumb" class="select filter"></div>
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
