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

<c:set var="selectionType"  value="${param.type}"/>
<c:set var="selectionScope" value="${param.scope}"/>
<c:set var="instanceId"     value="${param.instanceId}"/>
<c:set var="roles"          value="${param.roles}"/>
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
      var callbackUrl = '<c:out value="${param.url}"/>';
      var path = [];
    <c:choose>
      <c:when test='${selectionType == "multiple"}'>
      var type = 'checkbox';
      </c:when>
      <c:otherwise>
      var type = 'radio';
      </c:otherwise>
    </c:choose>
      
    <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
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
            $.each(users, function() {
              $('<tr>').addClass('user').addClass(style).
                append($('<td>').append($('<input>', {type: type, name: 'user', value: this.id}))).
                append($('<td>').append($('<img>', {src: webContext + this.avatar, alt: this.lastName + ' ' + this.firstName}).addClass('avatar'))).
                append($('<td>').addClass('name').text(this.lastName)).
                append($('<td>').addClass('fistname').text(this.firstName)).
                append($('<td>').addClass('email').text(this.eMail)).
                append($('<td>').addClass('domain').text(this.domainName))
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
        $("input#user-selection").val($.trim(usersSelection));
      }
    </c:if>
    
    <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
      var rootGroup = { childrenUri: webContext + '/services/profile/groups', name: '<fmt:message key="GML.groupes"/>' };
      <c:if test="${instanceId != null && fn:length(instanceId) > 0}">
      rootGroup.childrenUri += '<c:out value="/application/${instanceId}"/>';
      <c:if test="${roles != null && fn:length(roles) > 0}">
      rootGroup.childrenUri += '?roles=<c:out value="${roles}"/>';
      </c:if>
      </c:if>
    <c:choose>
      <c:when test='${selectionScope == "user" || selectionScope == "usergroup"}'>
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
            loadSubGroups(group, onGroupChange);
          }).text(group.name)).append(' :: ');
        }
        elt.append(path[path.length - 1].name);
      }
      
      function loadSubGroups(theGroup, onGroupLoaded) {
        $('tr.group').remove();
        $.ajax({
          url: theGroup.childrenUri,
          type: 'GET',
          dataType: 'json',
          cache: false,
          success: function(groups) {
            var style = 'even';
            updateGroupPathWith(theGroup);
            renderGroupPathAt($('#group-path'));
            $.each(groups, function(i, group) {
              $('<tr>').addClass('group').addClass(style).
                append($('<td>').append($('<input>', {type: type, name: 'group', value: group.id}))).
                append($('<td>').addClass('name').append($('<a>', {href: '#'}).click(function() {
                  loadSubGroups(group, onGroupLoaded);
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
            if (onGroupLoaded)
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
        $("input#group-selection").val($.trim(groupsSelection));
      }
    </c:if>
      
      function validateSelection() {
        if(callbackUrl) {
    <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
          valGroupsSelection();
    </c:if>
    <c:if test='${selectionScope == "user" || selectionScope == "usergroup"}'>
          valUsersSelection();
    </c:if>
          $("#selection").submit();
        }
      }
      
      $(document).ready(function() {
    <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
        loadSubGroups(rootGroup, onGroupChange);
    </c:if>
      });
    </script>
  </head>
  <body>
    <form action="<c:out value='${requestScope.url}'/>" id="selection" method="POST">
      <c:if test='${selectionScope == "group" || selectionScope == "usergroup"}'>
        <input id="group-selection" type="hidden" value=""/>
        <div id="groups">
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
        <input id="user-selection" type="hidden" value=""/>
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
    </form>
    <br clear="all"/>
    <div id="validate">
      <view:buttonPane>
        <view:button label="${selectLabel}" action="javascript: validateSelection();"/>
        <view:button label="${cancelLabel}" action="javascript: window.close();"/>
      </view:buttonPane>
    </div>
  </body>
</html>
