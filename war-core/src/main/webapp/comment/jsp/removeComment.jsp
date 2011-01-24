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

<%@ include file="checkComment.jsp" %>
<%@ include file="graphicUtil.jsp.inc"%>

<%
    String comment_id 	= request.getParameter("comment_id");
	String component_id = request.getParameter("component_id");
    String action 		= request.getParameter("action");
    String url 			= request.getParameter("url");
    String pub_id 		= request.getParameter("pub_id");
    CommentService commentService = CommentServiceFactory.getFactory().getCommentService();

    if ( "delete".equals(action) && comment_id != null && !"null".equals(comment_id) )
    {
        CommentPK pk = new CommentPK(comment_id, component_id);
        commentService.deleteComment(pk);
    }

    response.sendRedirect(URLManager.getApplicationURL()+ url);
%>
