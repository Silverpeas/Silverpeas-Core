<%--
  CDDL HEADER START
  The contents of this file are subject to the terms
  of the Common Development and Distribution License
  (the License). You may not use this file except in
  compliance with the License.

  You can obtain a copy of the License at
  http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
  See the License for the specific language governing
  permission and limitations under the License.

  When distributing Covered Code, include this CDDL
  Header Notice in each file and include the License file
  at legal/CDDLv1.0.txt.
  If applicable, add the following below the CDDL Header,
  with the fields enclosed by brackets [] replaced by
  your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  CDDL HEADER END
--%>

<%@page contentType="text/html"%>
<%@page import="com.silverpeas.portlets.portal.DriverUtil, 
                com.sun.portal.portletcontainer.driver.admin.AdminConstants, 
                com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants" %>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle" />

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
				<td class="txtlibform">Affichage :</td>
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