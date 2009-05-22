<%@ page import="com.stratelia.webactiv.calendar.model.ToDoHeader"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>

<%
RenderRequest 	pReq 	= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator 		todos 	= (Iterator) pReq.getAttribute("Todos");

if (!todos.hasNext()) {
	out.println(message.getString("NoTodos"));
} else {
	ToDoHeader todo = null;
	while (todos.hasNext()) {
		todo = (ToDoHeader) todos.next();
		if (todo.getPercentCompleted() != 100) {
			out.println("&#149; <a href=\""+m_sContext+URLManager.getURL(URLManager.CMP_TODO)+"todo.jsp\">" + Encode.convertHTMLEntities(todo.getName()) + "</a>");
			if (todo.getPercentCompleted() != -1)
				out.println(" <i>("+todo.getPercentCompleted()+"%)</i><br/>");
			else
				out.println(" <i>(0%)</i><br/>");
		}
	}
}
out.flush();
%>