<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="org.silverpeas.web.socialnetwork.myprofil.servlets.MyProfileRoutes" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<c:set var="preferences" value="${requestScope['preferences']}"/>
<!--
<%
  pageContext.setAttribute("availableLooks", gef.getAvailableLooks());
%>-->
<div id="settings">
<form name="UserForm" action="<%=MyProfileRoutes.UpdateMySettings %>" method="post">
  <table border="0" cellspacing="0" cellpadding="5" width="100%">
    <tr id="language">
      <td class="txtlibform"><fmt:message key="${'myProfile.settings.FavoriteLanguage'}"/> :</td>
      <td>
        <viewTags:userPreferredLanguageSelector userPreferences="${preferences}"/>
      </td>
    </tr>
    <c:choose>
      <c:when test="${empty availableLooks}">
        <input type="hidden" name="SelectedLook" value="<c:out value="${preferences.look}" />"/>
      </c:when>
      <c:otherwise>
        <tr>
          <td class="txtlibform"><fmt:message key="${'myProfile.settings.FavoriteLook'}"/> :</td>
          <td><select name="SelectedLook" size="1">
            <c:forEach items="${availableLooks}" var="look">
              <c:choose>
                <c:when test="${preferences.look eq look}">
                  <option value="<c:out value="${look}"/>" selected="selected"><c:out
                      value="${look}"/></option>
                </c:when>
                <c:otherwise>
                  <option value="<c:out value="${look}"/>"><c:out value="${look}"/></option>
                </c:otherwise>
              </c:choose>
            </c:forEach>
          </select>
          </td>
        </tr>
      </c:otherwise>
    </c:choose>
    <tr id="defaultSpace">
      <td class="txtlibform"><fmt:message key="${'myProfile.settings.DefaultWorkSpace'}"/> :</td>
      <td>
        <select name="SelectedWorkSpace" size="1">
          <option value="" <c:if
              test="${empty preferences.personalWorkSpaceId || 'null' eq  preferences.personalWorkSpaceId}">selected="selected" </c:if>></option>
          <c:forEach items="${requestScope['SpaceTreeview']}" var="space">
            <c:set var="indentation" value=''/>
            <c:if test="${space.level > 0}">
            <c:forEach begin="0" end="${space.level}">
              <c:set var="indentation">&nbsp;&nbsp;<c:out value="${indentation}" escapeXml="false"/></c:set>
            </c:forEach>
            </c:if>
            <option value="<c:out value="${space.id}"/>"
                    <c:if test="${space.id eq preferences.personalWorkSpaceId}">selected="selected"</c:if> >
              <c:out value="${indentation}" escapeXml="false"/><c:out
                value="${space.name}"/></option>
          </c:forEach>
        </select>
      </td>
    </tr>
    <c:if test="${true == requestScope['MenuDisplay']}" >
      <tr>
      <td class="txtlibform"><fmt:message key="${'myProfile.settings.menuDisplay'}"/> :</td>
      <td>
        <select name="MenuDisplay" size="1">
        <c:forEach items="${requestScope['MenuDisplayOptions']}" var="menuOption">
         <option value="<c:out value="${menuOption}"/>" <c:if test="${menuOption eq preferences.display}">selected="selected" </c:if>><fmt:message key="myProfile.settings.${menuOption}" /></option>
        </c:forEach>
        </select>
      </td>
    </tr>
    </c:if>
    <tr id="thesaurus">
      <td class="txtlibform"><fmt:message key="${'myProfile.settings.Thesaurus'}"/> :</td>
      <td>
        <input name="opt_thesaurusStatus" type="checkbox"
               value="true"
               <c:if test="${preferences.thesaurusEnabled}">checked="checked"</c:if>  />
      </td>
    </tr>
    <tr id="dragndrop">
      <td class="txtlibform"><fmt:message key="${'myProfile.settings.DragDrop'}"/> :</td>
      <td>
        <input name="opt_dragDropStatus" type="checkbox"
               value="true"
               <c:if test="${preferences.dragAndDropEnabled}">checked="checked"</c:if> />
      </td>
    </tr>
    <tr id="webdav">
      <td class="txtlibform"><fmt:message key="${'myProfile.settings.WebdavEditing'}"/> :</td>
      <td>
        <input name="opt_webdavEditingStatus" type="checkbox"
               value="true"
               <c:if test="${preferences.webdavEditionEnabled}">checked="checked"</c:if> />
      </td>
    </tr>
  </table>
  <br/>
  <fmt:message key="GML.validate" var="validate"/>
  <fmt:message key="GML.cancel" var="cancel"/>
  <center>
    <view:buttonPane>
      <view:button action="javascript:onClick=submitForm();" label="${validate}" disabled="false"/>
      <view:button action="javascript:onClick=history.back();" label="${cancel}" disabled="false"/>
    </view:buttonPane>
  </center>
</form>
</div>
<script type="text/javascript">
  function submitForm() {
    var currentLook = '<caption:out balue="${preferences.look}"/>';
    if (document.UserForm.SelectedLook.value != currentLook) {
      alert("<fmt:message key="${'myProfile.settings.ChangeLookAlert'}"/>");
    }

    document.UserForm.submit();
  }
</script>