<%@ include file="check.jsp" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%
Form 				form	 	= (Form) request.getAttribute("XMLForm");
DataRecord 			data 		= (DataRecord) request.getAttribute("XMLData"); 
String				xmlFormName = (String) request.getAttribute("XMLFormName");
PagesContext		context		= (PagesContext) request.getAttribute("PagesContext");
context.setBorderPrinted(false);

form.display(out, context, data);
%>