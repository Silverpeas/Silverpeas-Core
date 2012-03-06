<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
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
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" />
<fmt:message var="selectLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<fmt:message var="searchLabel" key="GML.search"/>

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
    <script type="text/javascript" src="<c:url value='/util/javaScript/jquery/smartpaginator.js'/>"></script>
    <link type="text/css" rel="stylesheet" href="<c:url value='/util/styleSheets/jquery/smartpaginator.css'/>"/>
    <title><fmt:message key="GML.selection"/></title>
    <script type="text/javascript" >
      // the path of the current user group from a given root user group
      var groupPath = [];
      
      // prepare the preselection of the users and of the user groups.
      var preselection = '<c:out value="${fn:join(selection.selectedElements, ',')}"/>';
      var selectedUsers = [], selectedGroups = [];
      if (preselection != null && preselection.length > 0)
        selectedUsers = preselection.split(',');
        
      preselection = '<c:out value="${fn:join(selection.selectedSets, ',')}"/>';
      if (preselection != null && preselection.length > 0)
        selectedGroups = preselection.split(',');
      
      // the selection object.
      var selection = new function () {
        this.isMultiple = <c:out value="${multipleSelection}"/>;
        this.selectedUsers = [];
        this.selectedGroups = [];
        
        function isAUser(item) {
          return item.type == 'user';
        }
      
        function isAGroup(item) {
          return item.type == 'group';
        }
        
        this.add = function(item) {
          if (isAGroup(item) && this.selectedGroups.indexOf(item) < 0)
            this.selectedGroups.push(item);
          else if (isAUser(item) && this.selectedUsers.indexOf(item) < 0)
            this.selectedUsers.push(item);
        }
        
        this.remove = function(item) {
          if (isAGroup(item)) {
            var index = this.selectedGroups.indexOf(item);
            if (index > -1)
              this.selectedGroups.splice(index, 1);
          } else if (isAUser(item)) {
            var index = this.selectedUsers.indexOf(item);
            if (index > -1)
              this.selectedUsers.splice(index, 1);
          }
        }
      
        this.isSelected = function(item) {
          return (isAUser(item) && this.selectedUsers.indexOf(item) > -1) ||
            (isAGroup(item) && this.selectedGroups.indexOf(item) > -1);
        }
        
        this.selectedUserIdsToString = function() {
          var selection = '';
          for (var i =0; i < this.selectedUsers.length - 1; i++)
            selection += this.selectedUsers[i].id + ',';
          if (this.selectedUsers.length > 0)
            selection += this.selectedUsers[this.selectedUsers.length - 1].id;
          return selection;
        }
        
        this.selectedGroupIdsToString = function() {
          var selection = '';
          for (var i =0; i < this.selectedGroups.length - 1; i++)
            selection += this.selectedGroups[i].id + ',';
          if (this.selectedGroups.length > 0)
            selection += this.selectedGroups[this.selectedGroups.length - 1].id;
          return selection;
        }
      
        this.selectedUserNamesToString = function() {
          var selection = '';
          for (var i =0; i < this.selectedUsers.length - 1; i++)
            selection += this.selectedUsers[i].fullName + ',';
          if (this.selectedUsers.length > 0)
            selection += this.selectedUsers[this.selectedUsers.length - 1].fullName;
          return selection;
        }
        
        this.selectedGroupNamesToString = function() {
          var selection = '';
          for (var i =0; i < this.selectedGroups.length - 1; i++)
            selection += this.selectedGroups[i].name + '\n';
          if (this.selectedGroups.length > 0)
            selection += this.selectedGroups[this.selectedGroups.length - 1].name;
          return selection;
        }
      }
  
      // print out the selector widget through which a user or a group can be selected.
      function selector(name, item) {
        var type = 'radio', validate = validateSelection;
        if (selection.isMultiple) {
          type = 'checkbox';
          validate = function() {};
        }
        return $('<input>', {type: type, name: name, value: item.id, checked: selection.isSelected(item)}).change(function () {
          if (this.checked)
            selection.add(item);
          else
            selection.remove(item);
          validate();
        });
      }
      
      // searchs all the Silverpeas users whose the name matches the first characters printed by the
      // user in the search field.
      function searchUsers() {
        var name = $('input#user-search-field').val() + "*";
        var group = (groupPath.length <= 1 ? null:groupPath[groupPath.length - 1]);
        loadUsers(group, name);
      }
      
      // loads all the users matching the defined filter and print out each of them for a selection.
      function loadUsers(inGroup, withName) {
        $('tr.user').remove();
        var uriOfUsers = webContext + '/services/profile/users', sep = '?';
    <c:if test="${instanceId != null && fn:length(instanceId) > 0}">
        uriOfUsers += '<c:out value="/application/${instanceId}"/>';
    <c:if test="${roles != null && fn:length(roles) > 0}">
        uriOfUsers += sep + 'roles=<c:out value="${roles}"/>';
        sep = '&';
    </c:if>
    </c:if>
        if (inGroup) {
          uriOfUsers += sep + "group=" + inGroup.id;
          sep = '&';
        }
        if (withName)
          uriOfUsers += sep + "name=" + withName;
        $.ajax({
          url: uriOfUsers,
          type: 'GET',
          dataType: 'json',
          cache: false,
          success: function(users) {
            var style = 'even';
            $.each(users, function(i, user) {
              user.type = 'user';
              if (selectedUsers.indexOf(user.id) > -1)
                selection.add(user);
              $('<tr>').addClass('user').addClass(style).
                append($('<td>').append(selector('user', user))).
                append($('<td>').append($('<img>', {src: user.avatar, alt: user.lastName + ' ' + user.firstName}).addClass('avatar'))).
                append($('<td>').addClass('name').text(user.lastName)).
                append($('<td>').addClass('fistname').text(user.firstName)).
                append($('<td>').addClass('email').text(user.eMail)).
                append($('<td>').addClass('domain').text(user.domainName))
              .appendTo('#user_list');
              if (style == 'even')
                style = 'odd';
              else
                style = 'even';
            });
            $('#user_list_pagination').smartpaginator({
              totalrecords: users.length,
              recordsperpage: 6,
              length: 6,
              datacontainer: 'user_list', 
              dataelement: 'tr',
              next: '>>',
              prev: '<<',
              first: 'Première page',
              last: 'Dernière page',
              go: 'Aller',
              theme: 'green'
            });
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });
      }
 
      // the root group
      var rootGroup = { childrenUri: webContext + '/services/profile/groups', name: '<fmt:message key="GML.groupes"/>' };
    <c:if test="${instanceId != null && fn:length(instanceId) > 0}">
      rootGroup.childrenUri += '<c:out value="/application/${instanceId}"/>';
    <c:if test="${roles != null && fn:length(roles) > 0}">
      rootGroup.childrenUri += '?roles=<c:out value="${roles}"/>';
    </c:if>
    </c:if>
    
    // if users should be displayed, they will be loaded with the sub groups of the current group
    // they are part of.
    <c:choose>
      <c:when test='${selectionScope == "usergroup"}'>
      var onGroupChange = function(newGroup) {
        if (newGroup != rootGroup)
          loadUsers(newGroup);
        else
          loadUsers();
      }
      </c:when>
      <c:otherwise>
      var onGroupChange = null;
      </c:otherwise>
    </c:choose>
      
      // searchs all the user groups whose the name matches the first characters printed by the
      // user in the search field.
      function searchGroups() {
        var name = $('input#group-search-field').val() + "*";
        loadGroups(rootGroup, name, onGroupChange);
      }

      // update the path to the groups with the new current user group.
      function updateGroupPathWith(group) {
        for(var i = 0; i < groupPath.length; i++) {
          if (groupPath[i].id == group.id)
            break;
        }
        if (i < groupPath.length)
          groupPath.splice(i + 1);
        else
          groupPath.push(group);
      }
      
      // render the group path
      function renderGroupPathAt(elt) {
        elt.text('');
        for(var i = 0; i < groupPath.length - 1; i++) {
          var group = groupPath[i];
          elt.append($('<a>', {href: '#'}).click(function() {
            loadGroups(group, null, onGroupChange);
          }).text(group.name)).append(' :: ');
        }
        elt.append(groupPath[groupPath.length - 1].name);
      }
      
      // loads all the user groups matching the defined filter and print out each of them for a
      // selection.
      function loadGroups(theGroup, withName, onGroupLoaded) {
        $('tr.group').remove();
        var uriOfGroups = theGroup.childrenUri;
        if (withName) {
          var sep = uriOfGroups.indexOf('?') > -1 ? '&':'?';
          uriOfGroups += sep + "name=" + withName;
        }
        $.ajax({
          url: uriOfGroups,
          type: 'GET',
          dataType: 'json',
          cache: false,
          success: function(groups) {
            var style = 'even';
            updateGroupPathWith(theGroup);
            renderGroupPathAt($('#group-path'));
            $.each(groups, function(i, group) {
              group.type = 'group';
              if (selectedGroups.indexOf(group.id) > -1)
                selection.add(group);
              $('<tr>').addClass('group').addClass(style).
                append($('<td>').append(selector('group', group))).
                append($('<td>').addClass('name').append($('<a>', {href: '#'}).click(function() {
                  loadGroups(group, null, onGroupLoaded);
                }).text(group.name))).
                append($('<td>').addClass('description').text(group.description)).
                append($('<td>').addClass('users').text(group.userCount)).
                append($('<td>').addClass('domain').text(group.domainId))
              .appendTo('#group_list');
              if (style == 'even')
                style = 'odd';
              else
                style = 'even';
            });
            $('#group_list_pagination').smartpaginator({
              totalrecords: groups.length,
              recordsperpage: 6,
              length: 6,
              datacontainer: 'group_list', 
              dataelement: 'tr',
              next: '>>',
              prev: '<<',
              first: 'Première page',
              last: 'Dernière page',
              go: 'Aller',
              theme: 'green'
            });
            if (onGroupLoaded && !withName)
              onGroupLoaded(theGroup);
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
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
        var selectedGroupNames = selection.selectedGroupNamesToString();
        var selectedUserNames = selection.selectedUserNamesToString();
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
      
      // load the users/groups for the selection
      $(document).ready(function() {
    <c:choose>
      <c:when test='${selectionScope == "group" || selectionScope == "usergroup"}'>
        loadGroups(rootGroup, null, onGroupChange);
      </c:when>
      <c:otherwise>
        loadUsers(null, null);
      </c:otherwise>
    </c:choose>
      });
    </script>
  </head>
  <body>
    <form action="<c:out value='${validationURL}'/>" id="selection" method="POST">
      <input type="hidden" name="UserOrGroupSelection" value="true"/>
      <input id="group-selection" type="hidden" name="GroupSelection" value=""/>
      <input id="user-selection" type="hidden" name="UserSelection" value=""/>
      <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
        <div id="groups">
          <div id="group-search">
            <input id="group-search-field" type="text" value=""/>
            <view:button label="${searchLabel}" action="javascript:searchGroups();"/>
          </div>
          <table id="group_list">
            <caption id="group-path"></caption>
            <tr class="heading">
              <th></th>
              <th><fmt:message key="GML.name"/></th>
              <th><fmt:message key="GML.description"/></th>
              <th><fmt:message key="GML.users"/></th>
              <th><fmt:message key="GML.domain"/></th>
            </tr>
          </table>
          <div id="group_list_pagination"></div>
        </div>
      </c:if>
      <c:if test='${selectionScope == "usergroup"}'>
        <hr/>
      </c:if>
      <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
        <div id="users">
          <div id="user-search">
            <input id="user-search-field" type="text" value=""/>
            <view:button label="${searchLabel}" action="javascript:searchUsers();"/>
          </div>
          <table id="user_list">
            <caption><fmt:message key="GML.users"/></caption>
             <tr class="heading">
              <th></th>
              <th></th>
              <th><fmt:message key="GML.lastName"/></th>
              <th><fmt:message key="GML.firstName"/></th>
              <th><fmt:message key="GML.eMail"/></th>
              <th><fmt:message key="GML.domain"/></th>
            </tr>
          </table>
          <div id="user_list_pagination"></div>
        </div>
      </c:if>
        <br clear="all"/>
        <div id="validate">
          <view:buttonPane>
            <view:button label="${selectLabel}" action="javascript: validateSelection();"/>
            <view:button label="${cancelLabel}" action="javascript: cancelSelection();"/>
          </view:buttonPane>
        </div>
    </form>
  </body>
</html>
