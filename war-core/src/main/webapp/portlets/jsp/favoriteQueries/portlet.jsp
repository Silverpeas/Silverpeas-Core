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
<%@ page import="java.net.URLEncoder"%>

<%@ page import="com.silverpeas.interestCenter.model.InterestCenter"%>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>

<%
RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator queries = (Iterator) pReq.getAttribute("Queries");

String icLink = m_sContext+"/RpdcSearch/jsp/AdvancedSearch?urlToRedirect="+URLEncoder.encode(m_sContext+"/admin/jsp/Main.jsp?ViewPersonalHomePage=true")+"&icId=";
out.println("<table>");
if (!queries.hasNext())
{
	out.println("<tr><td width='100%'>"+message.getString("NoPDCSubscriptions")+"</td></tr>");
}
else
{
	int j = 0;
	while (queries.hasNext())
	{
		InterestCenter ic = (InterestCenter) queries.next();
		if (j == 0)
			out.println("<tr>");
		out.println("<td width='50%'>&#149; <a href='" + icLink + ic.getId() + "'>" + Encode.convertHTMLEntities(ic.getName()) + "</a></td>");
		if (j != 0)
		{
			out.println("</tr>");
			j=0;
		}
		else
		{
			j=1;
		}
	}
}
out.println("</table>");
%>