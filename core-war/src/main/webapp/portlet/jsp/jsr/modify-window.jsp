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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.portlets.portal.DriverUtil" %>
<%@page import="com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants" %>
<%@page import="com.sun.portal.portletcontainer.driver.admin.AdminConstants" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!--Load the resource bundle for the page -->
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle" />

<h1 class="portal-content-header" id="modify-window-content"><fmt:message key="portlets.modifyWindow"/></h1>

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.modifyFailed']}" var="msgFail" />
<c:if test="${msgFail != null}" >
    <h2 id="modify-failed"><c:out value="${msgFail}" escapeXml="false"/></h2>
</c:if>

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.modifySucceeded']}" var="msgSuccess" />
<c:if test="${msgSuccess != null}" >
    <h2 id="modify-success"><c:out value="${msgSuccess}" escapeXml="false"/></h2>
</c:if>

<form id="modify-window" name="modifyForm" method="post" action="<%=DriverUtil.getAdminURL(request)%>" >
    <fieldset>
        <table cellpadding="5">
            <tr>
                <td class="txtlibform"><fmt:message key="portlets.selectBasePortletWindow"/> :</td>
				<td>
                    <c:set var="list" value="${sessionScope['com.sun.portal.portletcontainer.driver.admin.portletWindows']}" />
                    <select id="portletWindowList" name="<%=AdminConstants.PORTLET_WINDOW_LIST%>" onchange="modifyForm.submit(); return false;" >
                        <c:forEach items="${list}" var="portletWindow">
                            <option <c:if test="${sessionScope['com.sun.portal.portletcontainer.driver.admin.selectedPortletWindow'] == portletWindow}">selected</c:if> value="<c:out value="${portletWindow}" />" >
                                <c:out value="${portletWindow}" />
                            </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
              <td class="txtlibform"><fmt:message key="portlets.show"/>&nbsp;:</td>
                <td>
                    <table>
                        <tr>
                            <th id="visibleId"></th>
                            <td headers="visibleId">
                                <label for="visibleShow"><fmt:message key="portlets.show"/></label>
                            </td>
                            <td headers="visibleId">
                                <input id="visibleShow" type="radio" name="<%=AdminConstants.VISIBLE_LIST%>" value="<%=PortletRegistryConstants.VISIBLE_TRUE%>" ${sessionScope['com.sun.portal.portletcontainer.driver.admin.showWindow']} />
                            </td>
                            <td headers="visibleId">
                                <label for="visibleHide"><fmt:message key="portlets.hide"/></label>
                            </td>
                            <td headers="visibleId">
                                <input id="visibleHide" type="radio" name="<%=AdminConstants.VISIBLE_LIST%>" value="<%=PortletRegistryConstants.VISIBLE_FALSE%>" ${sessionScope['com.sun.portal.portletcontainer.driver.admin.hideWindow']} />
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>
                <td class="txtlibform"><fmt:message key="portlets.selectWidth"/> :</td>
				<td>
                    <select id="widthList" name="<%=AdminConstants.WIDTH_LIST%>" >
                        <option ${sessionScope['com.sun.portal.portletcontainer.driver.admin.thickWindow']} value="<%=PortletRegistryConstants.WIDTH_THICK%>">
                            <fmt:message key="portlets.thick"/>
                        </option>
                        <option ${sessionScope['com.sun.portal.portletcontainer.driver.admin.thinWindow']} value="<%=PortletRegistryConstants.WIDTH_THIN%>" >
                            <fmt:message key="portlets.thin"/>
                        </option>
                    </select>
                </td>
            </tr>
            <tr>
                <td align="center" colspan="2"><input id="modify" type="Submit" name="<%=AdminConstants.MODIFY_PORTLET_WINDOW_SUBMIT%>" value="<fmt:message key="portlets.modifyPortletWindow"/>"/></td>
            </tr>
        </table>
    </fieldset>
</form>