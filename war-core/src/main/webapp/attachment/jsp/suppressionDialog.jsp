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