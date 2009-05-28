<%@ include file="check.jsp" %>

<%
Form 				formUpdate 	= (Form) request.getAttribute("Form");
DataRecord 			data 		= (DataRecord) request.getAttribute("Data");
PagesContext		context		= (PagesContext) request.getAttribute("context"); 

%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<% formUpdate.displayScripts(out, context); %>
</HEAD>
<BODY class="yui-skin-sam">
<%
browseBar.setDomainName(resource.getString("templateDesigner.toolName"));
browseBar.setComponentName(resource.getString("templateDesigner.templateList"), "Main");
browseBar.setPath(resource.getString("templateDesigner.template"));

TabbedPane tabbedPane = gef.getTabbedPane();
tabbedPane.addTab(resource.getString("GML.preview"), "#", true);
tabbedPane.addTab(resource.getString("templateDesigner.template"), "EditTemplate", false);
tabbedPane.addTab(resource.getString("templateDesigner.fields"), "ViewFields", false);

	out.println(window.printBefore());
	
	out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
<FORM NAME="myForm" METHOD="POST" ACTION="UpdateXMLForm" ENCTYPE="multipart/form-data">
	<% 
		formUpdate.display(out, context, data); 
	%>
</FORM>
<%
	out.println(board.printAfter());
    out.println(frame.printAfter());
    out.println(window.printAfter()); 
%>
</BODY>
</HTML>