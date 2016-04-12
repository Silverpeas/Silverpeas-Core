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

<%@page import="org.silverpeas.core.calendar.model.ToDoHeader"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.util.EncodeHelper"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
RenderRequest 	pReq 	= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator<ToDoHeader> todos 	= (Iterator<ToDoHeader>) pReq.getAttribute("Todos");

if (!todos.hasNext()) {
	out.println(message.getString("NoTodos"));
} else {
	out.println("<ul>");
	while (todos.hasNext()) {
		ToDoHeader todo = todos.next();
		if (todo.getPercentCompleted() != 100) {
			out.println("<li><a href=\""+m_sContext+URLUtil.getURL(URLUtil.CMP_TODO)+"todo.jsp\">" + EncodeHelper.convertHTMLEntities(todo.getName()) + "</a>");
			if (todo.getPercentCompleted() != -1) {
				out.println(" <i>("+todo.getPercentCompleted()+"%)</i>");
			} else {
				out.println(" <i>(0%)</i>");
			}
			out.println("</li>");
		}
	}
	out.println("</ul>");
}
out.flush();
%>