<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="com.silverpeas.portlets.FormNames" %>

<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail" %>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
RenderRequest 	pReq 	= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator 		infos 	= (Iterator) pReq.getAttribute("QuickInfos");

String description = "";
while (infos.hasNext())
{
    PublicationDetail pub = (PublicationDetail) infos.next();
	UserDetail pubCreator = m_MainSessionCtrl.getOrganisationController().getUserDetail(pub.getCreatorId());

    description = pub.getWysiwyg();
    if (!StringUtil.isDefined(description))
    	description = EncodeHelper.javaStringToHtmlParagraphe(pub.getDescription(language));

    %>

	<table cellpadding="3" cellspacing="0" border="0" width="98%"><tr><td><span class="textePetitBold"><%=EncodeHelper.convertHTMLEntities(pub.getName(language)) %></span><br/><view:username userId="<%=pubCreator.getId()%>"/> - <%=DateUtil.getOutputDate(pub.getCreationDate(), language)%></td></tr></table>
	<table cellpadding="1" cellspacing="0" border="0" class="intfdcolor" width="98%"><tr><td>
	<table cellpadding="3" cellspacing="0" border="0" class="intfdcolor4" width="100%"><tr><td>

		<span class="txtnote"><%=description%></span>

	</td></tr></table>
	</td></tr></table>

	<%
    if (infos.hasNext())
    	out.println("<br/>");
}
out.flush();
%>