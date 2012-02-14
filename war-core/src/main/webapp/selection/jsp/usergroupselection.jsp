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
    <title><fmt:message key="GML.selection"/></title>
    <script type="text/javascript" >
      var path = [];
      var selectedUsers = '<c:out value="${fn:join(selection.selectedElements, ',')}"/>';
      if (selectedUsers != null && selectedUsers.length > 0)
        selectedUsers = selectedUsers.split(',');
      else
        selectedUsers = [];
      var selectedGroups = '<c:out value="${fn:join(selection.selectedSets, ',')}"/>';
      if (selectedGroups != null && selectedGroups.length > 0)
        selectedGroups = selectedGroups.split(',');
      else
        selectedGroups = [];
    <c:choose>
      <c:when test='${multipleSelection}'>
      var type = 'checkbox';
      </c:when>
      <c:otherwise>
      var type = 'radio';
      </c:otherwise>
    </c:choose>
    
      function contains(array, elt) {
        return array.indexOf(elt) > -1;
      }
      
      function searchUsers() {
        var name = $('input#user-search-field').val() + "*";
        var group = (path.length <= 1 ? null:path[path.length - 1]);
        loadUsers(group, name);
      }
      
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
              $('<tr>').addClass('user').addClass(style).
                append($('<td>').append($('<input>', {type: type, name: 'user', value: user.id, checked: contains(selectedUsers, user.id)}).change(function () {
                  if (this.checked)
                    selectedUsers.push(user.id)
                  else
                    selectedUsers.splice(selectedUsers.indexOf(user.id), 1);
                }))).
                append($('<td>').append($('<img>', {src: webContext + user.avatar, alt: user.lastName + ' ' + user.firstName}).addClass('avatar'))).
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
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });
      }
      
      function valUsersSelection() {
        var usersSelection = '';
        $('#users :' + type + ':checked').each(function() {
          usersSelection += $(this).val() + ' ';
        });
        /*$("input#user-selection").val($.trim(usersSelection));*/
        $("input#user-selection").val(selectedUsers.join(','));
      }
 
      var rootGroup = { childrenUri: webContext + '/services/profile/groups', name: '<fmt:message key="GML.groupes"/>' };
      <c:if test="${instanceId != null && fn:length(instanceId) > 0}">
      rootGroup.childrenUri += '<c:out value="/application/${instanceId}"/>';
      <c:if test="${roles != null && fn:length(roles) > 0}">
      rootGroup.childrenUri += '?roles=<c:out value="${roles}"/>';
      </c:if>
      </c:if>
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
      
      function searchGroups() {
        var name = $('input#group-search-field').val() + "*";
        loadGroups(rootGroup, name, onGroupChange);
      }

      function updateGroupPathWith(group) {
        for(var i = 0; i < path.length; i++) {
          if (path[i].id == group.id)
            break;
        }
        if (i < path.length)
          path.splice(i + 1);
        else
          path.push(group);
      }
      
      function renderGroupPathAt(elt) {
        elt.text('');
        for(var i = 0; i < path.length - 1; i++) {
          var group = path[i];
          elt.append($('<a>', {href: '#'}).click(function() {
            loadGroups(group, null, onGroupChange);
          }).text(group.name)).append(' :: ');
        }
        elt.append(path[path.length - 1].name);
      }
      
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
              $('<tr>').addClass('group').addClass(style).
                append($('<td>').append($('<input>', {type: type, name: 'group', value: group.id, checked: contains(selectedGroups, group.id)}).change(function () {
                  if (this.checked)
                    selectedGroups.push(group.id)
                  else
                    selectedGroups.splice(selectedGroups.indexOf(group.id), 1);
                }))).
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
            if (onGroupLoaded && !withName)
              onGroupLoaded(theGroup);
          },
          error: function(jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
          }
        });
      }
      
      function valGroupsSelection() {
        var groupsSelection = '';
        $('#groups :' + type + ':checked').each(function() {
          groupsSelection += $(this).val() + ' ';
        });
        /*$("input#group-selection").val($.trim(groupsSelection));*/
        $("input#group-selection").val(selectedGroups.join(','));
      }
      
      function validateSelection() {
        valGroupsSelection();
        valUsersSelection();
        $("#selection").submit();
      }
      
      function cancelSelection() {
        $('input[name="UserOrGroupSelection"]').val('false');
        $("#selection").attr("action", "<c:out value='${cancelationURL}'/>");
        $("#selection").submit();
      }
      
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
