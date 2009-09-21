<%@ page import="com.stratelia.silverpeas.versioning.model.Document"%>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
    ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
    String pleaseFill = messages.getString("pleaseFill");
		String okLabel = messages.getString("ok");
    String nokLabel = messages.getString("close");

    String name = "";
    String description = "";
    String versionType = new Integer(VersioningSessionController.WORK_VERSION).toString();
    String comments = "";
    String mimeType = "";
    File dir = null;
    int size = 0;

    String documentNameLabel = messages.getString("name");
    String descriptionLabel = messages.getString("description");
    String documentPathLabel = messages.getString("document");
    String versionTypeLabel = messages.getString("typeOfVersion");
    String commentsLabel = messages.getString("comments");
    String[] radioButtonLabel = {messages.getString("public"),messages.getString("archive")};
    String requiredFieldLabel = messages.getString("required");

    String mandatoryField = m_context+"/util/icons/mandatoryField.gif";

    String 			pubId       = (String) request.getAttribute("PubId");
    Form 			formUpdate 	= (Form) request.getAttribute("XMLForm");
    DataRecord 		data 		= (DataRecord) request.getAttribute("XMLData"); 
    String			xmlFormName = (String) request.getAttribute("XMLFormName");
    PagesContext	context		= (PagesContext) request.getAttribute("PagesContext");
    if (context != null)
    {
    	context.setBorderPrinted(false);
    	context.setFormIndex("0");
    	context.setCurrentFieldIndex("7");
    }

    Document document = null;
%>
<html>
<head>
<TITLE><%=messages.getString("popupTitle")%></TITLE>
<script language="Javascript">
function rtrim(texte){
        while (texte.substring(0,1) == ' '){
                texte = texte.substring(1, texte.length);
        }
        return texte;
}

function ltrim(texte){
        while (texte.substring(texte.length-1,texte.length) == ' ') {
                texte = texte.substring(0, texte.length-1);
        }

        return texte;
}
function isFormFilled(){

        var isEmpty = false;
        if (trim(document.addForm.name.value) == "")
		      isEmpty = true;
        return isEmpty;
}

function trim(texte){
        var len = texte.length;
        if (len == 0){
                texte = "";
        }
        else {
            texte = rtrim(texte);
            texte = ltrim(texte);
        }
        return texte;
}

function addFile()
{
        if (!isFormFilled() && isCorrectForm()) {
			document.addForm.submit();
        } else {
            alert("<%=pleaseFill%>");
        }
}
</script>
<% if (formUpdate != null) { %>
	<script type="text/javascript" src="<%=m_context%>/wysiwyg/jsp/FCKeditor/fckeditor.js"></script>
	<% formUpdate.displayScripts(out, context); %>
<% } else { %>
	<script type="text/javascript">
		function isCorrectForm()
		{
			return true;
		}
	</script>
<% } %>
</head>
<body class="yui-skin-sam">
<%
    out.println(gef.getLookStyleSheet());
	out.println(window.printBefore());
	out.println(frame.printBefore());

    Board board = gef.getBoard();
    out.println(board.printBefore());
%>
	<form name="addForm" action="SaveNewDocument" method="POST" enctype="multipart/form-data">
	<input type="hidden" name="publicationId" value="<%=pubId%>">
	<table CELLPADDING="2" CELLSPACING="0" BORDER="0" WIDTH="100%">
	        <tr>
	                <td class="txtlibform"><%=documentNameLabel%> :</td>
	                <td align="left" valign="baseline"><input type="text" name="name" size="50" maxlength="100" value="<%=name%>">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	        </tr>
	        <tr>
	                <td class="txtlibform"><%=descriptionLabel%> :</td>
	                <td align="left" valign="baseline"><textarea name="description" rows="3" cols="50" ><%=description%></textarea></td>
	        </tr>
	        <tr>
	                <td class="txtlibform"><%=documentPathLabel%> :</td>
	                <td align="left" valign="baseline"><input type="file" name="file_upload"></td>
	        </tr>
	        <tr>
	                <td class="txtlibform"><%=versionTypeLabel%> :</td>
	                <td align="left" valign="baseline">
                        <input value="0" type="radio" name="versionType"><%=radioButtonLabel[0]%><input type="radio" value="1" name="versionType" checked><%=radioButtonLabel[1]%>
                        
	                </td>
	        </tr>
	        <tr>
	                <td class="txtlibform"><%=commentsLabel%> :</td>
	                <td align="left" valign="baseline"><textarea name="comments" rows="5" cols="50" ><%=comments%></textarea></td>
	        </tr>
	        <tr>
	                <td colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">: <%=requiredFieldLabel%>)</td>
	        </tr>
	</table>
	<%
		if (formUpdate != null)
		{
			%>
			<br/>
			<table CELLPADDING="2" CELLSPACING="0" BORDER="0" WIDTH="100%">
				<tr><td class="intfdcolor6outline"><span class="txtlibform"><%=messages.getString("versioning.xmlForm.XtraData")%></span></td></tr>
			</table>
			<%formUpdate.display(out, context, data); 
		}
	%>
	</form>
<%
		out.println(board.printAfter());

    ButtonPane buttonPane = gef.getButtonPane();
		buttonPane.addButton(gef.getFormButton(okLabel,"javascript:addFile()", false));
		buttonPane.addButton(gef.getFormButton(nokLabel, "javascript:window.close()", false));

    out.flush();
    out.println("<BR><center>");
    out.println(buttonPane.print());
    out.println("</center>");

	  out.println(frame.printAfter());
	  out.println(window.printAfter()); 
%>

<script language="Javascript">
	document.addForm.name.focus();
</script>
</body>
</html>