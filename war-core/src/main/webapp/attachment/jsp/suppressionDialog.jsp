<%@ include file="checkAttachment.jsp"%>

<%
String objectId			= request.getParameter("Id");
String componentId		= request.getParameter("ComponentId");
String url				= request.getParameter("Url");
String attachmentName 	= request.getParameter("Name");
String attachmentId		= request.getParameter("IdAttachment");
String indexIt			= request.getParameter("IndexIt");
String languages		= request.getParameter("Languages");

StringTokenizer tokenizer = new StringTokenizer(languages, ",");
%>

<html>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<head>
</head>
<body>
<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(resources.getString("GML.delete"), "javascript:document.removeForm.submit();", false));
	buttonPane.addButton(gef.getFormButton(resources.getString("GML.cancel"), "javascript:onClick=closeMessage()", false));
%>
	<center>
	<form name="removeForm" action="<%=m_Context%>/attachment/jsp/removeFile.jsp" method="POST">
		<input type="hidden" name="ComponentId" value="<%=componentId%>"/>
		<input type="hidden" name="IdAttachment" value="<%=attachmentId%>"/>
		<input type="hidden" name="Url" value="<%=url%>"/>
		<input type="hidden" name="Id" value="<%=objectId%>"/>
		<input type="hidden" name="IndexIt" value="<%=indexIt%>"/>
		<table>
			<tr>
				<td align="center">
					<br/>
					<% if (I18NHelper.isI18N && StringUtil.isDefined(languages) && tokenizer.countTokens() > 1) { %>
						<table border="0">
						<tr><td colspan="2"><%=resources.getStringWithParam("attachment.suppressionWhichTranslations", attachmentName)%></td></tr>
						<tr><td><input type="checkbox" name="languagesToDelete" value="all"/></td><td width="100%"><%=Encode.convertHTMLEntities(resources.getString("attachment.allTranslations"))%></td></tr>
						<%
							String attLanguage = null;
							while (tokenizer.hasMoreTokens())
							{
								attLanguage = tokenizer.nextToken();
								%>
									<tr><td><input type="checkbox" name="languagesToDelete" value="<%=attLanguage%>"/></td><td><%=Encode.convertHTMLEntities(I18NHelper.getLanguageLabel(attLanguage, language))%></td></tr>
								<%
							}
						%>
						</table>
					<% } else { %>
						<%=Encode.convertHTMLEntities(resources.getStringWithParam("attachment.suppressionConfirmation", attachmentName))%><br/>
					<% } %>
					<br/>
					<%=buttonPane.print()%>
				</td>
			</tr>
		</table>
	</form>
	</center>
</body>
</html>