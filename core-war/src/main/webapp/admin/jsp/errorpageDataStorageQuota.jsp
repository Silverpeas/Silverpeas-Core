<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="exception" value="${pageContext.exception}" />
<c:set var="templateLocationBase" value="core:admin/space/quota" />
<c:set var="titleTemplate" value="dataStorageQuotaExceptionTitle" />
<c:set var="messageTemplate" value="dataStorageQuotaExceptionMessage" />

<fmt:setLocale value="${exception.language}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><view:applyTemplate locationBase="${templateLocationBase}" name="${titleTemplate}" /></title>
    <link href="<c:url value="/style.css" />" rel="stylesheet" type="text/css" />
    <style type="text/css">
      .titre {
	white-space: nowrap;
	left: 0px;
	position: relative;
	text-align: center;
      }
    </style>
  </head>
  <body>
    <div class="page">
      <div class="titre"><view:applyTemplate locationBase="${templateLocationBase}" name="${titleTemplate}" /></div>
      <div id="background">
        <div class="cadre">
          <div id="header">
            <img src="<c:url value="/images/logo.jpg" />" class="logo" alt="logo" />
            <p class="information"></p>
          </div>
          <div class="fnfinformation">
            <br/><br/><br/>
            <view:applyTemplate locationBase="${templateLocationBase}" name="${messageTemplate}">
              <view:templateParam name="maxCountFormated" value="${view:formatMemSize(exception.quota.maxCount)}" />
              <view:templateParam name="spaceName" value="${exception.space.name}" />
              <view:templateParam name="isPersonalSpace" value="${exception.space.personalSpace}" />
              <view:templateParam name="fromComponentName" value="${exception.fromComponent.label}" />
              <view:templateParam name="fromComponentUrl" value="${exception.fromComponentUrl}" />
            </view:applyTemplate>
          </div>
        </div>
      </div>
    </div>
  </body>
</html>