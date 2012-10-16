<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<!--Definition des informations globales de la page --->
<%@ page language="java" %>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.attachment.control.AttachmentController"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
        String url = request.getParameter("Url");
        String idAttachment = request.getParameter("IdAttachment");
        String fileLanguage = request.getParameter("FileLanguage");
        MainSessionController mainSessionController = (MainSessionController) request.getSession(
            ).getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
        boolean update = Boolean.valueOf(request.getParameter("update_attachment")).booleanValue();
        boolean force = Boolean.valueOf(request.getParameter("force_release")).booleanValue()
                && "A".equals(mainSessionController.getCurrentUserDetail().getAccessLevel()) ;
        if(!AttachmentController.checkinFile(idAttachment, mainSessionController.getUserId(), false, update, force, fileLanguage)) {
          if(url.indexOf('?') > 0) {
            url = url + '&';
          }else {
            url = url + '?';
          }
          url = url + "warning=locked";
        }
        response.sendRedirect(URLManager.getApplicationURL()+url);
%>