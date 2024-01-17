<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page isELIgnored="false" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<view:sp-page>
  <fmt:setLocale value="${requestScope.userLanguage}"/>
  <view:setBundle basename="org.silverpeas.sharing.multilang.fileSharingBundle"/>
  <view:setBundle basename="org.silverpeas.sharing.settings.fileSharingIcons" var="icons"/>
  <view:sp-head-part noLookAndFeel="true">
    <style type="text/css">
      td {
        font-family: "Verdana", "Arial", sans-serif;
        font-size: 10px
      }
      fieldset {
        vertical-align: middle;
        text-align: center;
      }
      legend {
        display: none;
      }
    </style>
  </view:sp-head-part>
  <view:sp-body-part>
    <fieldset>
      <fmt:message key="sharing.expiredTicket"/>
      <legend></legend>
    </fieldset>
  </view:sp-body-part>
</view:sp-page>