<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ page import="com.silverpeas.publicationTemplate.*"%>
<%@ page import="com.silverpeas.form.*"%>
<%@ include file="checkVersion.jsp" %>

<%
// declaration of labels !!!
ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());

String documentId = request.getParameter("documentId");

String documentPathLabel = messages.getString("newDocumentVersion");
String versionTypeLabel = messages.getString("typeOfVersion");
String commentsLabel = messages.getString("comments");
String okLabel = messages.getString("ok");
String nokLabel = messages.getString("cancel");
String[] radioButtonLabel = {messages.getString("public"),messages.getString("archive")};
String requiredFieldLabel = messages.getString("required");
String pleaseFill = messages.getString("pleaseFill");
String mandatoryField = m_context+"/util/icons/mandatoryField.gif";

String spaceId        = request.getParameter("SpaceId");
String componentId    = request.getParameter("ComponentId");
String publicationId  = request.getParameter("Id");
String hide_radio     = request.getParameter("hide_radio");

Form 				formUpdate 	= (Form) request.getAttribute("XMLForm");
DataRecord 			data 		= (DataRecord) request.getAttribute("XMLData"); 
String				xmlFormName = (String) request.getAttribute("XMLFormName");
PagesContext		context		= (PagesContext) request.getAttribute("PagesContext");
if (context != null)
{
	context.setBorderPrinted(false);
	context.setFormIndex("0");
	context.setCurrentFieldIndex("7");
}
%>

<html>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<head>
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

function onRadioClick(i) {
 document.addForm.radio.value = i;
}


function isFormFilled(){

        var isEmpty = false;
<%
    if ( !"true".equals(hide_radio) )
    {
%>
        if ( (trim(document.addForm.file_upload.value) == "") ||
         (trim(document.addForm.radio.value) == "") )
<%
    }
    else
    {
%>
    document.addForm.radio.value = "0";
        if ( (trim(document.addForm.file_upload.value) == "") )
<%
    }
%>
    {
      isEmpty = true;
    }

        return isEmpty;
}

function addFile(){
		if (!isFormFilled() && isCorrectForm()){
        	document.addForm.submit();
        } else {
                document.addForm.file_upload.value = '';
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
Board board = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>

<!-- <form name="addForm" action="<%=m_context%>/RVersioningPeas/jsp/saveFile.jsp?save=true&componentId=<%=componentId%>&spaceId=<%=spaceId%>&documentId=<%=documentId%>" method="POST" enctype="multipart/form-data"> -->
<form name="addForm" action="<%=m_context%>/RVersioningPeas/jsp/SaveNewVersion" method="POST" enctype="multipart/form-data">
<input type="hidden" name="documentId" value="<%=documentId%>">
<input type="hidden" name="publicationId" value="<%=publicationId%>">
<input type="hidden" name="radio">

<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH="100%">
        <tr>
                <td class="txtlibform">
                        <%=documentPathLabel%> :
                </td>
                <td align=left valign="baseline">
                        <input type="file" name="file_upload"> &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
                </td>
        </tr>
<%
    if ( !"true".equals(hide_radio) )
    {
%>
        <tr>
                <td class="txtlibform">
                        <%=versionTypeLabel%> :
                </td>
                <td align=left valign="baseline">
                        <%
                                // display radio button
                                for (int i=0;i<radioButtonLabel.length ;i++ ){
                                        out.println(" <input type=\"radio\" name=\"versionType\" onClick=\"onRadioClick(" + i + ")\"> "+radioButtonLabel[i]);
                                }
                        %>
                        &nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5">
                </td>
        </tr>
<%
    }
%>
        <tr>
                <td class="txtlibform" valign="top"><%=commentsLabel%> :</td>
                <td align="left" valign="baseline">
                        <textarea name="comments" rows="3" cols="70"></textarea>
                </td>
        </tr>
        <tr>
                <td colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5">
                                : <%=requiredFieldLabel%>)
                </td>
        </tr>

</table>
<%
		if (formUpdate != null)
		{
			%><br/>
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

        out.println("<br><center>");
        out.println(buttonPane.print());
        out.println("</center>");

		out.println(frame.printAfter());
        out.println(window.printAfter());
%>
</body>
</html>