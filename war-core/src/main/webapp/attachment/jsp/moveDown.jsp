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
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="org.silverpeas.attachment.AttachmentServiceFactory" %>
<%@ page import="org.silverpeas.attachment.model.SimpleDocument" %>
<%@ page import="org.silverpeas.attachment.model.SimpleDocumentPK" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>


<%
    String componentId = request.getParameter("ComponentId");
    String id = request.getParameter("Id");
    String context = request.getParameter("Context");
    String url = request.getParameter("Url");
    String idAttachment = request.getParameter("IdAttachment");

    //create AttachmentPK with id and spaceId and componentId
    SimpleDocumentPK atPK = new SimpleDocumentPK(idAttachment, componentId);

    //recuperation de l'objet
    SimpleDocument document =
            AttachmentServiceFactory.getAttachmentService().searchAttachmentById(atPK, null);
    List<SimpleDocument> docs  = AttachmentServiceFactory.getAttachmentService().searchAttachmentsByExternalObject(new
            ForeignPK(document.getForeignId(), document.getInstanceId()), null);
    int index = docs.indexOf(document);
    if(index < docs.size())  {
        Collections.swap(docs, index, index + 1);
    }
    AttachmentServiceFactory.getAttachmentService().reorderDocuments(docs);
    if (url.indexOf('?') >= 0) {
        response.sendRedirect(URLManager.getApplicationURL()+ url + "&Id="+id);
    } else {
        response.sendRedirect(URLManager.getApplicationURL()+ url + "?Id="+id);
    }
%>
