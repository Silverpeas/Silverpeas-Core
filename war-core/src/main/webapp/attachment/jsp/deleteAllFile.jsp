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

<%@ page import="javax.ejb.RemoveException, javax.ejb.CreateException, java.sql.SQLException, javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.FinderException"%>
<%@ page import="java.util.Collection, java.util.ArrayList, java.util.Iterator, java.util.Date"%>
<%@ page import="com.oreilly.servlet.multipart.*"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.util.WAPrimaryKey"%>

<%@ page import="com.stratelia.webactiv.almanach.control.*"%>
<%@ page import="com.stratelia.webactiv.almanach.model.*"%>

<%@ page import="com.stratelia.webactiv.util.attachment.control.AttachmentController"%>
<%@ page import="com.stratelia.webactiv.util.attachment.ejb.AttachmentPK"%>
<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetail"%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%  
    String componentId = request.getParameter("componentId");
    String idExt = request.getParameter("idExt");
    String context = request.getParameter("context");
    String url = request.getParameter("url");

    //test delete avec primary key de l'objet utilisateur
    //create foreignKey with spaceId, componentId and idExt
    //use AttachmentPK to build the foreign key of customer object.

    AttachmentPK foreignKey =  new AttachmentPK(idExt, componentId);
    AttachmentController.deleteAttachmentByCustomerPK(foreignKey);
%>