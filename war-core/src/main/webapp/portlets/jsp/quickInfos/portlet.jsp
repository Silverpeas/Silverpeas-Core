<%@ page import="com.silverpeas.portlets.FormNames" %>

<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail" %>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail" %>

<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>

<%
RenderRequest 	pReq 	= (RenderRequest)request.getAttribute("javax.portlet.request");
Iterator 		infos 	= (Iterator) pReq.getAttribute("QuickInfos");

String description = "";
while (infos.hasNext()) 
{
    PublicationDetail pub = (PublicationDetail) infos.next();
	UserDetail pubCreator = m_MainSessionCtrl.getOrganizationController().getUserDetail(pub.getCreatorId());
	    
    description = pub.getWysiwyg();
    if (!StringUtil.isDefined(description))
    	description = Encode.javaStringToHtmlParagraphe(pub.getDescription(language));
    
    %>
	
	<table cellpadding="3" cellspacing="0" border="0" width="98%"><tr><td class="intfdcolor"><span class="textePetitBold"><%=Encode.convertHTMLEntities(pub.getName(language)) %></span><br><%=pubCreator.getDisplayedName()%> - <%=DateUtil.getOutputDate(pub.getCreationDate(), language)%></td></tr></table>
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