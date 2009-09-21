<%@ include file="check.jsp" %>

<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>

<%
Form 				formUpdate 	= (Form) request.getAttribute("XMLForm");
DataRecord 			data 		= (DataRecord) request.getAttribute("XMLData"); 
String				xmlFormName = (String) request.getAttribute("XMLFormName");
PagesContext		context		= (PagesContext) request.getAttribute("PagesContext");

context.setBorderPrinted(false);
context.setFormIndex("0");

%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
<% formUpdate.displayScripts(out, context); %>
<script language="javaScript">
function B_VALIDER_ONCLICK()
{
	if (isCorrectForm())
	{
		document.myForm.submit();
	}
}

function B_ANNULER_ONCLICK() 
{
	window.close();
}
</script>
</HEAD>
<BODY class="yui-skin-sam">
<%
    Board board = gef.getBoard();
    out.println(board.printBefore());
%>
<FORM NAME="myForm" METHOD="POST" ACTION="Update" ENCTYPE="multipart/form-data">
	<% 
		formUpdate.display(out, context, data); 
	%>
	<input type="hidden" name="Name" value="<%=xmlFormName%>">
</FORM>
<%
	out.println(board.printAfter());
	
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK();", false));
	buttonPane.addButton((Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK();", false));
    out.println("<br><center>"+buttonPane.print()+"</center>");
%>
</BODY>
<script language="javascript">
	document.myForm.elements[1].focus();
</script>
</HTML>