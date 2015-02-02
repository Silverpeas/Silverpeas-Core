<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<c:url var="iconGroupSync" value="/jobDomainPeas/jsp/icons/scheduledGroup.gif"/>
<c:url var="iconGroup" value="/util/icons/groupe.gif"/>
<c:url var="iconUser" value="/util/icons/user.gif"/>
<c:url var="iconUserBlocked" value="/util/icons/user-blocked.png"/>
<c:url var="iconUserExpired" value="/util/icons/user-expired.png"/>

<fmt:message key="GML.delete" bundle="${generalBundle}" var="labelDelete"/>
<fmt:message key="GML.modify" bundle="${generalBundle}" var="labelUpdate"/>
<fmt:message key="GML.delete" bundle="${generalBundle}" var="labelRemove"/>

<%-- Creator --%>
<%@ attribute name="users" required="false" type="java.util.List"
              description="The list of users to display" %>

<%@ attribute name="groups" required="false" type="java.util.List"
              description="The list of groups to display" %>

<%@ attribute name="label" required="false" type="java.lang.String"
              description="Label to use as fieldset legend" %>

<%@ attribute name="updateCallback" required="false" type="java.lang.String"
              description="Javascript function or URL to update list" %>

<%@ attribute name="displayLabel" required="false" type="java.lang.Boolean"
              description="Display label to use as fieldset legend" %>

<%@ attribute name="displayUserZoom" required="false" type="java.lang.Boolean"
              description="Activate the user zoom plugin on each user displayed" %>

<%@ attribute name="displayAvatar" required="false" type="java.lang.Boolean"
              description="Display avatar of each user or just user icon if false" %>

<%@ attribute name="hideEmptyList" required="false" type="java.lang.Boolean"
              description="Hide empty list" %>

<%@ attribute name="id" required="false" type="java.lang.String"
              description="CSS id" %>

<c:if test="${displayUserZoom == null}">
  <c:set var="displayUserZoom" value="${true}"/>
</c:if>

<c:if test="${displayAvatar == null}">
  <c:set var="displayAvatar" value="${true}"/>
</c:if>

<c:if test="${hideEmptyList == null}">
  <c:set var="hideEmptyList" value="${false}"/>
</c:if>

<c:if test="${label != null && displayLabel == null}">
  <c:set var="displayLabel" value="${true}"/>
</c:if>

<c:set var="readOnly" value="${empty updateCallback}"/>

<div id="root-${id}">
<c:choose>
  <c:when test="${hideEmptyList && empty groups && empty users}">
    <!-- Do not display empty list -->
  </c:when>
  <c:otherwise>
    <c:if test="${displayLabel}">
      <fieldset id="${id}" class="skinFieldset">
      <legend class="without-img">${label}</legend>
    </c:if>
    <div class="fields">
      <c:if test="${not readOnly}">
        <a href="#" onclick="openUserPanel('${id}', '${updateCallback}')" class="explorePanel"><span>${labelUpdate}</span></a>
        <a href="#" onclick="removeItems('${id}')" class="emptyList"><span>${labelRemove}</span></a>
      </c:if>
      <div class="field entireWidth" id="${id}-area">
        <ul class="access-list group">
          <c:forEach var="group" items="${groups}">
            <li class="type-group" id="${group.id}">
              <c:choose>
                <c:when test="${group.synchronized}">
                  <img src="${iconGroupSync}" alt=""/>
                </c:when>
                <c:otherwise>
                  <img src="${iconGroup}" alt=""/>
                </c:otherwise>
              </c:choose>
                ${group.name} (${group.totalNbUsers})
              <c:if test="${not readOnly}">
              <div class="operation">
                <a href="#" onclick="javascript:removeItem('${id}', 'group', '${group.id}');return false;" title="${labelDelete}"><img border="0" src="../../util/icons/delete.gif" alt="${labelDelete}" title="${labelDelete}" /></a>
              </div>
              </c:if>
            </li>
          </c:forEach>
        </ul>

        <ul class="access-list user">
          <c:forEach var="user" items="${users}">
            <li class="type-user" id="${user.id}">
              <c:if test="${displayAvatar}">
                <view:image src="${user.avatar}" alt="" size="20x" css="user-avatar"/>
              </c:if>
              <c:if test="${not displayAvatar}">
                <c:choose>
                  <c:when test="${user.blockedState}">
                    <img src="${iconUserBlocked}" alt="" />
                  </c:when>
                  <c:when test="${user.expiredState}">
                    <img src="${iconUserExpired}" alt="" />
                  </c:when>
                  <c:otherwise>
                    <img src="${iconUser}" alt="" />
                  </c:otherwise>
                </c:choose>
              </c:if>
              <view:username userId="${user.id}" zoom="${displayUserZoom}"/>
              <c:if test="${not readOnly}">
                <div class="operation">
                  <a href="#" onclick="javascript:removeItem('${id}', 'user', '${user.id}');return false;" title="${labelDelete}"><img border="0" src="../../util/icons/delete.gif" alt="${labelDelete}" title="${labelDelete}" /></a>
                </div>
              </c:if>
            </li>
          </c:forEach>
        </ul>
      </div>
    </div>
    <c:if test="${displayLabel}">
      </fieldset>
    </c:if>
  </c:otherwise>
</c:choose>

  <input type="hidden" id="${id}-userIds" name="${id}UserPanelCurrentUserIds"/>
  <input type="hidden" id="${id}-groupIds" name="${id}UserPanelCurrentGroupIds"/>

</div>

<script type="text/javascript">
  $(document).ready(function() {
    getUsersInput("${id}").val(getActualUsersList("${id}"));
    getGroupsInput("${id}").val(getActualGroupsList("${id}"));

    checkEmptyListButton("${id}");

    getUsersInput("${id}").bind("change", function() {
      userListChanged("${id}");
    });

    getGroupsInput("${id}").bind("change", function() {
      groupListChanged("${id}");
    });
  });

  function checkEmptyListButton(id) {
    if (getActualUsersList(id).length == 0 && getActualGroupsList(id).length == 0) {
      $(".emptyList", $("#root-"+id)).hide();
    } else {
      $(".emptyList", $("#root-"+id)).show();
    }
  }

  function getUsersInput(id) {
    return $("#" + id + "-userIds");
  }

  function getGroupsInput(id) {
    return $("#" + id + "-groupIds");
  }

  function isReadOnly(id) {
    return ($(".explorePanel", $("#root-"+id)).length <= 0);
  }

  function userListChanged(id) {
    var actualUsersList = getActualUsersList(id);
    var newUsersList = getNewUsersList(id);
    //alert("actualUsersList = "+actualUsersList + "newUsersList = "+newUsersList);
    // process removed elements
    $.each(actualUsersList, function( index, value ){
      if ($.inArray(value, newUsersList) === -1) {
        removeItem(id, "user", value);
      }
    });

    // process new elements
    $.each(newUsersList, function(index, value) {
      if ($.inArray(value, actualUsersList) === -1) {
        //alert(value + " is new !");
        if (value.isDefined()) {
          generateUserFragment(id, value);
        }
      }
    });
  }

  function groupListChanged(id) {
    var actualList = getActualGroupsList(id);
    var newList = getNewGroupsList(id);

    // process removed elements
    $.each(actualList, function( index, value ){
      if ($.inArray(value, newList) === -1) {
        removeItem(id, "group", value);
      }
    });

    // process new elements
    $.each(newList, function( index, value ){
      if ($.inArray(value, actualList) === -1) {
        //alert(value + " is new !");
        if (value.isDefined()) {
          generateGroupFragment(id, value);
        }
      }
    });
  }

  function generateUserFragment(id, userId) {
    var uri = webContext+"/services/profile/users/"+userId;
    $.get(uri)
        .done(function(user) {
          var li = $("<li>").attr("class", "type-user").attr("id", userId);
          var avatar = $("<img>").attr("alt", "");
          <c:if test="${displayAvatar}">
            avatar.attr("class", "user-avatar");
            avatar.attr("src", user.avatar);
          </c:if>
          <c:if test="${not displayAvatar}">
            if (user.blockedState) {
              avatar.attr("src", "${iconUserBlocked}");
            } else if (user.expiredState) {
              avatar.attr("src", "${iconUserExpired}");
            } else {
              avatar.attr("src", "${iconUser}");
            }
          </c:if>
          avatar.appendTo(li);
          var span = $("<span>").attr("class", "userToZoom").attr("rel", userId);
          span.html(user.firstName + " " + user.lastName);
          span.appendTo(li);
          if (!isReadOnly(id)) {
            var op = generateOperationFragment(id, "user", userId);
            op.appendTo(li);
          }
          $(".access-list.user", $("#root-"+id)).append(li);
          checkEmptyListButton(id);
        });
  }

  function generateGroupFragment(id, groupId) {
    var uri = webContext+"/services/profile/groups/"+groupId;
    $.get(uri)
        .done(function(group) {
          var li = $("<li>").attr("class", "type-group").attr("id", groupId);
          var avatar = $("<img>").attr("alt", "").attr("src", "${iconGroup}");
          avatar.appendTo(li);
          var span = $("<span>").html(group.name + " (" + group.userCount + ")");
          span.appendTo(li);
          if (!isReadOnly(id)) {
            var op = generateOperationFragment(id, "group", groupId);
            op.appendTo(li);
          }
          $(".access-list.group", $("#root-"+id)).append(li);
          checkEmptyListButton(id);
        });
  }

  function generateOperationFragment(id, itemType, itemId) {
    var op = $("<div>").attr("class", "operation");
    var a = $("<a>").attr("href", "#").attr("title", "${labelDelete}");
    a.attr("onclick", "javascript:removeItem('"+id+"','"+itemType+"','"+itemId+"');return false;");
    var img = $("<img>").attr("border", "0").attr("title", "${labelDelete}").attr("alt", "${labelDelete}");
    img.attr("src", "../../util/icons/delete.gif");
    img.appendTo(a);
    a.appendTo(op);
    return op;
  }

  function removeItem(id, itemType, itemId) {
    var li = "ul."+itemType+" li#"+itemId;
    $(li, $("#root-"+id)).remove();

    // update form input
    if (itemType === "user") {
      getUsersInput(id).val(getActualUsersList(id));
    } else {
      getGroupsInput(id).val(getActualGroupsList(id));
    }

    checkEmptyListButton(id);
  }

  function removeItems(id) {
    $("ul.user li", $("#root-"+id)).remove();
    getUsersInput(id).val("");

    $("ul.group li", $("#root-"+id)).remove();
    getGroupsInput(id).val("");

    checkEmptyListButton(id);
  }

  function getNewUsersList(id) {
    var str = getUsersInput(id).val();
    return str.split(',');
  }

  function getNewGroupsList(id) {
    var str = getGroupsInput(id).val();
    return str.split(',');
  }

  function getActualUsersList(id) {
    var userIds = [];
    $("li.type-user", $("#root-"+id)).each(function(index) {
      userIds.push($(this).attr("id"));
    });
    return userIds;
  }

  function getActualGroupsList(id) {
    var groupIds = [];
    $("li.type-group", $("#root-"+id)).each(function(index) {
      groupIds.push($(this).attr("id"));
    });
    return groupIds;
  }

  function openUserPanel(id, initializationAction) {
    var params = "?";
    if (initializationAction.indexOf('?') !== -1) {
      params = "&";
    }
    params += "UserPanelCurrentUserIds="+getUsersInput(id).val();
    params += "&UserPanelCurrentGroupIds="+getGroupsInput(id).val();
    SP_openUserPanel(initializationAction+params, "userPanel");
  }
</script>