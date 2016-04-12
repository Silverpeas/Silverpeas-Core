<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@page import="org.silverpeas.core.notification.user.server.channel.silvermail.SILVERMAILMessage"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.util.EncodeHelper"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane" %>
<%@ page import="org.silverpeas.core.util.DateUtil" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>
<portlet:actionURL var="actionURL"/>

<script type="text/javascript">
function readMessage(id)
{
	SP_openWindow("<%=m_sContext%>/RSILVERMAIL/jsp/ReadMessage.jsp?ID="+id+"&from=homePage","readMessage","600","380","scrollable=yes,scrollbars=yes");
}
</script>

<%
RenderRequest 	pReq 			= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator 		messageIterator = (Iterator) pReq.getAttribute("Messages");

//Arraypane
ArrayPane list = gef.getArrayPane("silvermail", actionURL, request, session);
ArrayColumn col = list.addArrayColumn(message.getString("notification.date"));
col = list.addArrayColumn(message.getString("notification.source"));
col = list.addArrayColumn(message.getString("notification.from"));
col = list.addArrayColumn(message.getString("notification.url"));
col = list.addArrayColumn(message.getString("notification.subject"));

String	hasBeenReadenOrNotBegin	= "";
String	hasBeenReadenOrNotEnd	= "";

while(messageIterator.hasNext())
{
	hasBeenReadenOrNotBegin = "";
	hasBeenReadenOrNotEnd 	= "";
	SILVERMAILMessage smMessage = (SILVERMAILMessage)messageIterator.next();
	if (smMessage.getReaden() == 0) {
		hasBeenReadenOrNotBegin = "<b>";
		hasBeenReadenOrNotEnd = "</b>";
	}

	String link = "<A HREF =\"javascript:onClick=readMessage(" + smMessage.getId() + ");\">";
	ArrayLine line = list.addArrayLine();
	Date date = smMessage.getDate();
	ArrayCellText cell1 = line.addArrayCellText(hasBeenReadenOrNotBegin + DateUtil
      .getOutputDate(date, language) + hasBeenReadenOrNotEnd );
	cell1.setCompareOn(date);

	ArrayCellText cell2 = line.addArrayCellText(hasBeenReadenOrNotBegin + EncodeHelper.javaStringToHtmlString(smMessage.getSource()) + "</A>" + hasBeenReadenOrNotEnd );
	cell2.setCompareOn(smMessage.getSource());

	ArrayCellText cell3 = line.addArrayCellText(hasBeenReadenOrNotBegin + link + EncodeHelper.javaStringToHtmlString(smMessage.getSenderName()) + "</A>" + hasBeenReadenOrNotEnd );
	cell3.setCompareOn(smMessage.getSenderName());

	if ( smMessage.getUrl()!=null && smMessage.getUrl().length()>0 )
		line.addArrayCellText(hasBeenReadenOrNotBegin + "<A HREF =\"" + EncodeHelper.javaStringToHtmlString(smMessage.getUrl()) + "\" target=_top><img src=\""+m_sContext+"/util/icons/Lien.gif\" border=\"0\"></A>" + hasBeenReadenOrNotEnd );
	else
		line.addArrayCellText( "" );
	ArrayCellText cell5 = line.addArrayCellText(hasBeenReadenOrNotBegin + link + EncodeHelper.javaStringToHtmlString(smMessage.getSubject()) + "</A>" + hasBeenReadenOrNotEnd );
	cell5.setCompareOn(smMessage.getSubject());
}
out.println(list.print());
out.flush();
%>