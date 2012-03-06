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

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle basename="com.silverpeas.selection.multilang.selectionBundle" />

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
    </style>
    <script type="text/javascript" src="<c:url value='/util/javaScript/silverpeas-profile.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/selection/jsp/javaScript/selection.js'/>"></script>
    <script type="text/javascript">
      rootUserGroup.name = '<fmt:message key="selection.RootUserGroups"/>';
      rootUserGroup.inComponent('<c:out value="${instanceId}"/>').withRoles('<c:out value="${roles}"/>');
      
      var userSelection = new Selection();
      userSelection.isMultiple = <c:out value="${multipleSelection}"/>;
      userSelection.onAdded = function(users) {
        for (var i = 0; i < users.length; i++) {
          $('#add_user_' + users[i].id).hide();
        }
        renderUserSelection();
      };
      userSelection.onRemoved = function(users) {
        for (var i = 0; i < users.length; i++) {
          $('#add_user_' + users[i].id).show();
        }
        renderUserSelection();
      };
      
      var groupSelection = new Selection();
      groupSelection.isMultiple = <c:out value="${multipleSelection}"/>;
      groupSelection.onAdded = function(groups) {
        for (var i = 0; i < groups.length; i++) {
          $('#add_group_' + groups[i].id).hide();
        }
        renderUserGroupSelection();
      };
      groupSelection.onRemoved = function(groups) {
        for (var i = 0; i < groups.length; i++) {
          $('#add_group_' + groups[i].id).show();
        }
        renderUserGroupSelection();
      };
      
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
      
      function renderPaginationFor(size, dataContainer) {
        var pagination;
        if (dataContainer == 'group_list')
          pagination = $('#group_list_pagination');
        else if (dataContainer == 'selected_group_list')
          pagination = $('#selected_group_list_pagination');
        else if (dataContainer == 'user_list')
          pagination = $('#user_list_pagination');
        else if (dataContainer == 'selected_user_list')
          pagination = $('#selected_user_list_pagination');
        
        pagination.smartpaginator({
          totalrecords: size,
          recordsperpage: 6,
          length: 6,
          datacontainer: dataContainer, 
          dataelement: 'li',
          next: '<fmt:message key="GML.pagination.ScrollNextPages"/>',
          prev: '<fmt:message key="GML.pagination.ScrollPreviousPages"/>',
          first: '<fmt:message key="GML.pagination.FirstPage"/>',
          last: '<fmt:message key="GML.pagination.LastPage"/>',
          go: '<fmt:message key="GML.pagination.GoToPage"/>',
          theme: 'pageNav'
        });
      }
      
      function renderUserGroupSelection() {
        $('#group_selected_count').text(groupSelection.items.length + ' <fmt:message key="selection.groupSelected"/>');
        $('#selected_group_list').slideUp('slow', function() {
          $('#selected_group_list').children().remove();
          for(var i = 0; i < groupSelection.items.length; i++) {
            renderUserGroup($('#selected_group_list'), i, groupSelection.items[i]);
          }
          $('#selected_group_list').slideDown('slow');
          renderPaginationFor(groupSelection.items.length, 'selected_group_list');
        });
      }
      
      function renderUserSelection() {
        $('#user_selected_count').text(userSelection.items.length + ' <fmt:message key="selection.userSelected"/>');
        $('#selected_user_list').slideUp('slow', function() {
          $('#selected_user_list').children().remove();
          for(var i = 0; i < userSelection.items.length; i++) {
            renderUser($('#selected_user_list'), i, userSelection.items[i]);
          }
          $('#selected_user_list').slideDown('slow');
          renderPaginationFor(userSelection.items.length, 'selected_user_list');
        });
      }
      
      function renderUserGroup($container, order, theGroup) {
        var style = (order % 2 == 0 ? 'odd':'even'), $operation;
        if ($container.attr('id') == 'group_list') {
          var id = 'group_' + theGroup.id;
          $operation = $('<a>', {id: 'add_group_' + theGroup.id, title: '<fmt:message key="selection.AddToSelection"/>', href: '#'}).
            addClass('add').addClass('group').text('<fmt:message key="selection.AddToSelection"/>').click(function() {
            groupSelection.add(theGroup);
          });
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
          append($('<span>').addClass('domain_group').text(theGroup.domainId)).
          append($operation).
          appendTo($container);
      }
      
      function renderUserGroups(groups) {
        renderFilteredUserGroups(groups, function(group) {
          $('<li>').append($('<a>', { href: '#' }).addClass('filter').
            append(group.name).
            append($('<span>').addClass('nb_results_by_filter').append('(' + group.userCount + ')')).click(function() {
            $('#breadcrumb').breadcrumb('set', group);
          })).
            appendTo($('ul.listing_groups_filter'));
        });
      }
      
      function renderFilteredUserGroups(groups, additionalRendering) {
        $('#group_result_count').text(groups.length + ' <fmt:message key="selection.groupFound"/>');
        $('#group_list').slideUp('slow', function() {
          $('#group_list').children().remove();
          for(var i = 0; i < groups.length; i++) {
            renderUserGroup($('#group_list'), i, groups[i]);
            if (additionalRendering)
              additionalRendering(groups[i]);
          }
          $('#group_list').slideDown('slow');
          renderPaginationFor(groups.length, 'group_list');
        });
      }
      
      function renderUser($container, order, theUser) {
        var style = (order % 2 == 0 ? 'odd':'even'), $operation;
        if ($container.attr('id') == 'user_list') {
          var id = 'user_' + theUser.id;
          $operation = $('<a>', {id: 'add_user_' + theUser.id, title: '<fmt:message key="selection.AddToSelection"/>', href: '#'}).
            addClass('add').addClass('user').text('<fmt:message key="selection.AddToSelection"/>').click(function() {
            userSelection.add(theUser);
          });
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
      
      function renderFilteredUsers(users) {
        $('#user_result_count').text(users.length + ' <fmt:message key="selection.userFound"/>');
        $('#user_list').slideUp('slow', function() {
          $('#user_list').children().remove();
          for(var i = 0; i < users.length; i++) {
            renderUser($('#user_list'), i, users[i]);
          }
          $('#user_list').slideDown('slow');
          renderPaginationFor(users.length, 'user_list');
        });
      }
      
      // validate the selection of users and/or of user groups.
      // the validation can be performed in two different ways:
      // - the form with the user and group selections is posted to the caller,
      // - or the caller form parameters for the selection are set directly, leaving the caller to
      //   process the selection
      function validateSelection() {
        var selectedGroupIds = selection.selectedGroupIdsToString();
        var selectedUserIds = selection.selectedUserIdsToString();
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
      
        $(document).ready(function() {
        
          $('#breadcrumb').breadcrumb( {
            root: rootUserGroup,
            onchange: function(group) {
              var $container = $('ul.listing_groups_filter');
              $container.slideUp('slow', function() {
                $container.children().remove();
              });
              $container.slideDown('slow');
              group.onChildren(renderUserGroups);
              group.onUsers(renderFilteredUsers);
            }
          });
          
          $('ul.listing_filter li a.filter').click(function() {
            rootUserGroup.onUsers(renderFilteredUsers);
          });
        
          $('.listing_groups a.add_all').click(function() {
            $('#breadcrumb').breadcrumb('current', function(group) {
              groupSelection.add(group.children());
            });
          });
          
          $('.listing_users a.add_all').click(function() {
            $('#breadcrumb').breadcrumb('current', function(group) {
              userSelection.add(group.users());
            });
          });
        
          $('#group_search').focus(function() {
            $(this).val('');
          }).blur(function() {
            $(this).val('<fmt:message key="selection.searchUserGroups"/>');
          }).autocomplete({
            minLength: 0,
            source: [],
            search: function() {
              var name = $(this).val();
              $('#breadcrumb').breadcrumb('current', function(group) {
                group.onChildren(name + '*', renderFilteredUserGroups);
              });
            } 
          });
        
          $('#user_search').focus(function() {
            $(this).val('');
          }).blur(function() {
            $(this).val('<fmt:message key="selection.searchUsers"/>');
          }).autocomplete({
            minLength: 0,
            source: [],
            search: function() {
              var name = $(this).val();
              $('#breadcrumb').breadcrumb('current', function(group) {
                group.onUsers(name + '*', renderFilteredUsers);
              });
            } 
          });
          
          renderUserGroupSelection();
          renderUserSelection();
        
          var height_container = $('.container_userPanel').outerHeight();
          var height_listing_groups = $('.listing_groups_filter').outerHeight();
          var height_listing = $('.listing_filter').outerHeight();
          var height_title = $('#filter_userPanel .title').outerHeight();
			
          var new_height_listing_groups = height_container - (height_listing-height_listing_groups)-height_title;

          $('.listing_groups_filter').css('height',new_height_listing_groups+'px');
				
          $(window).resize(function() {
            height_container = $('.container_userPanel').outerHeight();
            height_listing_groups = $('.listing_groups_filter').outerHeight();
            height_listing = $('.listing_filter').outerHeight();
            height_title = $('#filter_userPanel .title').outerHeight();
				
            new_height_listing_groups = height_container - (height_listing-height_listing_groups)-height_title;

            $('.listing_groups_filter').css('height',new_height_listing_groups+'px');
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
        <h4 class="title"><fmt:message key="selection.Filter"/>"</h4>
        <ul class="listing_filter">
          <li><a class="filter" href="#"><fmt:message key="selection.AllUsers"/> <span id="user_count" class="nb_results_by_filter"></span></a></li>
          <li><div id="breadcrumb" class="select filter"></div>
            <ul class="listing_groups_filter">
            </ul>
          </li>
        </ul>
      </div>
      <div id="results_userPanel">
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
      </div>

      <div id="selected_userPanel">
        <div class="container">
          <h4 class="title"><fmt:message key="selection.SelectedUsersAndGroups"/></h4>
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
        </div>
      </div>

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
            <view:button label="${cancelLabel}" action="javascript: cancelSelection();"/>
          </view:buttonPane>
        </div>
      </form>
          
    </div>

  </body>
</html>
