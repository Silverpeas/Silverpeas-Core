<%@ page import="java.io.*"%>
<%@ include file="checkCommunicationUser.jsp" %>

<%
	String creationDate	= resources.getOutputDate(new Date());
	File fileDiscussion = (File) request.getAttribute("FileDiscussion");

	FileReader file_read = new FileReader(fileDiscussion);
    BufferedReader flux_in = new BufferedReader(file_read);
%>

<html>
<head>
<TITLE><%=resources.getString("exportDiscussion")+" "+creationDate%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
</head>
<body bgcolor="#FFFFFF" leftmargin="1" topmargin="1" marginwidth="1" marginheight="1">
<% 
	out.print("<BR><BR>");
	String ligne;
	while ((ligne = flux_in.readLine()) != null) {
		out.print("<font color=\"#000000\" size=\"2\" face=\"Courier New, Courier, mono\">"+Encode.convertHTMLEntities(ligne)+"</font>");
		out.print("<BR>");
	}
	flux_in.close();
	file_read.close();
%>
</body>
</html>