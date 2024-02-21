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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.web.notificationserver.channel.silvermail.SILVERMAILSessionController"%>

<%@ page import=" org.silverpeas.core.util.WebEncodeHelper"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.kernel.bundle.ResourceLocator"%>
<%
      SILVERMAILSessionController silvermailScc = (SILVERMAILSessionController) request.getAttribute(
          "SILVERMAIL");

      if (silvermailScc == null) {
        // No session controller in the request -> security exception
        String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString(
            "sessionTimeout");
        getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request,
            response);
        return;
      }

      MultiSilverpeasBundle resource = (MultiSilverpeasBundle) request.getAttribute("resources");
      String m_Context = ResourceLocator.getGeneralSettingBundle().getString(
          "ApplicationURL");
%>