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

<%@ page import="org.silverpeas.core.web.portlets.FormNames" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>
<portlet:actionURL var="actionURL"/>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.portlets.multilang.portletsBundle"/>

<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
    RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
    PortletPreferences pref = pReq.getPreferences();
    String error = pReq.getParameter(FormNames.ERROR_BAD_VALUE);
%>
    <!-- START error -->
<%
    if (error != null) {
%>
      <div class="portlet-msg-error" style="color: red">
          <fmt:message key="portlets.portlet.nextEvents.error"/>
      </div>
      <br>
<%
        }
%>

    <form name="inputForm" target="_self" method="POST" action="<%=actionURL.toString()%>">
        <table border="0" width="100%" style="align: center">

            <!-- START "url" text box -->
            <tr>
                <td><fmt:message key="portlets.portlet.nextEvents.pref.nbEvents" /> :</td>
                <td><input class="portlet-form-input-field" name="<%=FormNames.TEXTBOX_NB_ITEMS%>" value="<%=pref.getValue("nbEvents","")%>" type="text" size="10"/></td>
            </tr>

            <!-- START "finished" and "cancel" buttons -->
            <tr>
                <td colspan="2" style="text-align: center; vertical-align: top">
                    <input class="portlet-form-button" name="<%=FormNames.SUBMIT_FINISHED%>" type="submit" value="<fmt:message key="portlets.validate"/>"/>
                    <input class="portlet-form-button" name="<%=FormNames.SUBMIT_CANCEL%>" type="submit" value="<fmt:message key="portlets.cancel"/>"/>
                </td>
            </tr>
        </table>
        <!-- END "finished" and "cancel" buttons -->
    </form>
