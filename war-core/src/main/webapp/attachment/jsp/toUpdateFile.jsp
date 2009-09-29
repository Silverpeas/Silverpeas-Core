<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkAttachment.jsp"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>

<%
  //initialisation des variables
  String id				= request.getParameter("Id");
  String componentId	= request.getParameter("ComponentId");
  String context		= request.getParameter("Context");
  String url			= request.getParameter("Url");
  String attachmentId	= request.getParameter("IdAttachment");
  String indexIt		= request.getParameter("IndexIt");
  String dNdVisible 	= request.getParameter("DNDVisible");

  //récupération des fichiers attachés à un événement
  //create foreignKey with componentId and custumer id
  //use AttachmentPK to build the foreign key of customer object.

  AttachmentPK		primaryKey = new AttachmentPK(attachmentId, componentId);
  AttachmentDetail	attachment = AttachmentController.searchAttachmentByPK(primaryKey);

  String title		= attachment.getTitle(language);
  String info		= attachment.getInfo(language);
  String fileName	= attachment.getLogicalName(language);

  if (title == null || title.length()==0)
	title = "";

  if (info == null || info.length()==0)
	info = "";

  Window window = gef.getWindow();
%>

<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<% out.println(gef.getLookStyleSheet()); %>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_Context%>/util/javaScript/i18n.js"></script>
<script language='Javascript'>
var attachmentMandatory = false;

function update()
{
	if (attachmentMandatory && isWhitespace(document.updateForm.file_upload.value))
		alert("<%=messages.getString("nomVide")%>");
	else
		document.updateForm.submit();
}

<%
if (attachment != null) {
	String lang = "";
	Iterator codes = attachment.getTranslations().keySet().iterator();
	while (codes.hasNext())
	{
		lang = (String) codes.next();
		out.println("var name_"+lang+" = \""+Encode.javaStringToJsString(attachment.getLogicalName(lang))+"\";");
		out.println("var title_"+lang+" = \""+Encode.javaStringToJsString(attachment.getTitle(lang))+"\";");
		out.println("var desc_"+lang+" = \""+Encode.javaStringToJsString(attachment.getInfo(lang))+"\";");
	}
}
%>

function showTranslation(lang)
{
	try
	{
		document.getElementById('fileName').innerHTML = eval('name_'+lang);
	} catch (e) {
		document.getElementById('fileName').innerHTML = "";
		attachmentMandatory = true;
	}
	showFieldTranslation('fileTitle', 'title_'+lang);
	showFieldTranslation('fileDesc', 'desc_'+lang);
}

function removeTranslation()
{
    document.updateForm.submit();
}
</script>
</head>
<body>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	
	Button update = (Button) gef.getFormButton(resources.getString("GML.validate"), "javascript:update()", false);
	buttonPane.addButton(update);

	Button close = (Button) gef.getFormButton(resources.getString("GML.cancel"), "javascript:window.close()", false);
	buttonPane.addButton(close);
	
	Frame frame	= gef.getFrame();
	Board board = gef.getBoard();

    out.println(frame.printBefore());
	out.println("<CENTER>");
	out.println(board.printBefore());
%>

	<table border="0" cellspacing="0" cellpadding="5" width="100%">
		<form name="updateForm" action="<%=m_Context%>/attachment/jsp/updateFile.jsp" method="POST" enctype="multipart/form-data">
		<%=I18NHelper.getFormLine(resources, attachment, resources.getLanguage())%>
		<tr align="justify">
			<td class="txtlibform" nowrap align="left"><%=resources.getString("GML.file")%> :</td>
			<td id="fileName"><%=fileName%></td>
		</td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap align="left"><%=messages.getString("fichierJoint")%> :</td>
			<td>
				<input type="file" name="file_upload" size="60" class="INPUT">
				<input type="hidden" name="Id" value="<%=id%>">
				<input type="hidden" name="ComponentId" value="<%=componentId%>">
				<input type="hidden" name="Context" value="<%=context%>">
				<input type="hidden" name="Url" value="<%=url%>">
				<input type="hidden" name="IdAttachment" value="<%=attachmentId%>">
				<input type="hidden" name="IndexIt" value="<%=indexIt%>">
			</td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap align="left"><%=messages.getString("Title")%> :</td>
			<td><input type="text" name="Title" size="60" id="fileTitle" value="<%=title%>"></td>
		</tr>
		<tr>
			<td class="txtlibform" nowrap align="left" valign="top"><%=resources.getString("GML.description")%> :</td>
			<td><textarea name="Description" cols="60" rows="3" id="fileDesc"><%=info%></textarea></td>
		</tr>
		</form>
	</table>
<%
	out.println(board.printAfter());
	out.println("<BR>"+buttonPane.print());
	out.println("</CENTER>");
	out.println(frame.printAfter());
%>
</body>
</html>