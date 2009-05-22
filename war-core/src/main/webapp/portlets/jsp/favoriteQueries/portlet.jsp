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