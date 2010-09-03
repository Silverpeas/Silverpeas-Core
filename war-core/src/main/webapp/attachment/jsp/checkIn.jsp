<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!--D�finition des informations globales de la page --->
<%@ page language="java" %>
<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.File"%>
<%@ page import="java.lang.Integer"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="javax.ejb.RemoveException" %>
<%@ page import="javax.ejb.CreateException"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="javax.naming.NamingException"%>
<%@ page import="java.rmi.RemoteException"%>
<%@ page import="javax.ejb.FinderException"%>
<%@ page import="java.util.Collection, java.util.ArrayList, java.util.Iterator, java.util.Date"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.util.attachment.control.AttachmentController"%>
<%@ page import="com.stratelia.webactiv.util.attachment.ejb.AttachmentException"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

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