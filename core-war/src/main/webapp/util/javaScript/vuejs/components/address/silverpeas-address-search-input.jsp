<%--
  ~ Copyright (C) 2000 - 2023 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<jsp:useBean id="language" type="java.lang.String"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.address.multilang.addressBundle"/>

<silverpeas-query-input-select v-bind:toElement="target"
                               v-bind:input-title="title"
                               v-bind:input-placeholder="placeholder"
                               v-bind:items="addresses"
                               v-bind:min-query-length="5"
                               v-bind:query-debounce="500"
                               v-on:query="performQuery"
                               v-on:select="$emit('select', $event)"
                               v-slot="{ item }">
  {{ item.getLabel() }}
</silverpeas-query-input-select>