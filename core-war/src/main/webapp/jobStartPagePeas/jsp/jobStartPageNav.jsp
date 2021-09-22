<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="startSpaceId" value="${requestScope.CurrentSpaceId}"/>
<c:set var="startSubSpaceId" value="${requestScope.CurrentSubSpaceId}"/>

<fmt:message var="domainsLabel" key="GML.domains"/>
<fmt:message var="chooseLabel" key="JSPP.Choose"/>
<fmt:message var="pxUrl" key="JSPP.px" bundle="${icons}"/>
<c:url var="pxUrl" value="${pxUrl}"/>
<fmt:message var="homeSpaceIconUrl" key="JSPP.homeSpaceIcon" bundle="${icons}"/>
<c:url var="homeSpaceIconUrl" value="${homeSpaceIconUrl}"/>
<fmt:message var="backToMainSpaceLabel" key="JSPP.BackToMainSpacePage"/>

<%@ include file="check.jsp" %>

<view:script src="/jobStartPagePeas/jsp/javascript/vuejs/admin-navigation.js"/>

<style>
  .component-icon {
    margin: 1px;
    vertical-align: middle;
  }

  #space-icon {
    vertical-align: middle;
  }
</style>
<div class="intfdcolor">
  <span class="treeview-label">${domainsLabel} : </span>
</div>
<div id="admin-navigation">
  <admin-navigation v-on:api="apiAvailable"></admin-navigation>
</div>
<script type="text/javascript">
  function jumpToSpace(spaceId) {
    spAdminWindow.loadSpace(spaceId);
  }

  function jumpToSubSpace(spaceId) {
    spAdminWindow.loadSubSpace(spaceId);
  }

  function jumpToComponent(componentId) {
    spAdminWindow.loadComponent(componentId);
  }

  window.adminVm = new Vue({
    el : '#admin-navigation',
    data : function() {
      return {
        api : undefined
      }
    },
    methods: {
      apiAvailable: function (api) {
        this.api = api;
        const startSpaceId = '${startSpaceId}';
        const startSubSpaceId = '${startSubSpaceId}';
        setTimeout(function (){
          if (startSubSpaceId) {
            jumpToSubSpace(startSubSpaceId);
          } else if (startSpaceId) {
            jumpToSpace(startSpaceId);
          } else {
            jumpToSpace();
          }
        }, 0);
      }
    }
  });
  spAdminLayout.getBody().getNavigation().addEventListener('json-load', function(e) {
    adminVm.api.injectData(e.detail.data);
    spProgressMessage.hide();
  }, 'jobStartPageNav');
  spAdminLayout.getBody().getNavigation().dispatchEvent('load');
</script>