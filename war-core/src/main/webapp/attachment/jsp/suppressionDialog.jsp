<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="checkAttachment.jsp"%>

<%
String attachmentId		= request.getParameter("IdAttachment");
String componentId		= (String) session.getAttribute("Silverpeas_Attachment_ComponentId");

AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(attachmentId, componentId));
String attachmentName 	= attachment.getLogicalName(language);
int nbTranslations = attachment.getTranslations().size();
Iterator languages = attachment.getLanguages();
%>

<%
	ButtonPane buttonPane = gef.getButtonPane();
	buttonPane.addButton(gef.getFormButton(attResources.getString("GML.delete"), "javascript:onClick=removeAttachment("+attachmentId+");", false));
	buttonPane.addButton(gef.getFormButton(attResources.getString("GML.cancel"), "javascript:onClick=closeMessage()", false));
%>
	<center>
	<form name="removeForm" action="<%=m_Context %>/Attachment" method="POST">
		<input type="hidden" name="id" value="<%=attachmentId%>"/>
		<input type="hidden" name="Action" value="Delete"/>
		<table>
			<tr>
				<td align="center">
					<% if (I18NHelper.isI18N && nbTranslations > 1) { %>
						<table border="0">
						<tr><td colspan="2"><%=attResources.getStringWithParam("attachment.suppressionWhichTranslations", attachmentName)%></td></tr>
						<tr><td><input type="checkbox" id="languagesToDelete" name="languagesToDelete" value="all"/></td><td width="100%"><%=Encode.convertHTMLEntities(attResources.getString("attachment.allTranslations"))%></td></tr>
						<%
							String attLanguage = null;
							while (languages.hasNext())
							{
								attLanguage = (String) languages.next();
								%>
									<tr><td><input type="checkbox" id="languagesToDelete" name="languagesToDelete" value="<%=attLanguage%>"/></td><td><%=Encode.convertHTMLEntities(I18NHelper.getLanguageLabel(attLanguage, language))%></td></tr>
								<%
							}
						%>
						</table>
					<% } else { %>
						<br/>
						<%=Encode.convertHTMLEntities(attResources.getStringWithParam("attachment.suppressionConfirmation", attachmentName))%><br/>
						<br/>
					<% } %>
					<%=buttonPane.print()%>
				</td>
			</tr>
		</table>
	</form>
	</center>