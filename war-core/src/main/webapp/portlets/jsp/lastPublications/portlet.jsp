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

<%@ page import="com.stratelia.webactiv.util.publication.model.PublicationDetail" %>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail" %>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
    RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
    PortletPreferences pref = pReq.getPreferences();
    String error = pReq.getParameter(FormNames.ERROR_BAD_VALUE);
%>

<script type="text/javascript">
function goTo(cUrl, componentId)
{
	jumpToComponent(componentId);
	location.href=cUrl;
}

function jumpToComponent(componentId) {
	//Reload DomainsBar
	parent.SpacesBar.document.privateDomainsForm.component_id.value=componentId;
	parent.SpacesBar.document.privateDomainsForm.privateDomain.value="";
	parent.SpacesBar.document.privateDomainsForm.privateSubDomain.value="";
	parent.SpacesBar.document.privateDomainsForm.submit();

	//Reload Topbar
	parent.SpacesBar.reloadTopBar(true);
}
</script>
<%
List<PublicationDetail> publications = (List<PublicationDetail>) pReq.getAttribute("Publications");
int i = 0;
for (PublicationDetail pub : publications) {
    UserDetail pubUpdater = m_MainSessionCtrl.getOrganizationController().getUserDetail(pub.getUpdaterId());
    String url = m_sContext + URLManager.getURL("kmelia", null, pub.getPK().getInstanceId()) + pub.getURL();
%>
	<% if (i != 0) { %>
		<br/><br/>
	<% } %>
    <a href="javaScript:goTo('<%=url %>','<%=pub.getPK().getInstanceId() %>')"><b><%=EncodeHelper.convertHTMLEntities(pub.getName(language))%></b></a>
    <% if (pubUpdater != null && pub.getUpdateDate() != null) { %>
      <br/><view:username userId="<%=pubUpdater.getId() %>"/> - <%=DateUtil.getOutputDate(pub.getUpdateDate(), language)%>
    <% } else if (pubUpdater != null && pub.getUpdateDate() == null) { %>
      <br/><view:username userId="<%=pubUpdater.getId() %>"/>
    <% } %>
    <% if (pubUpdater == null && pub.getUpdateDate() != null) { %>
      <br/><%=DateUtil.getOutputDate(pub.getUpdateDate(), language) %>
    <% } %>
    <% if ("checked".equalsIgnoreCase(pref.getValue("displayDescription", "")) && StringUtil.isDefined(pub.getDescription(language))) { %>
      <br/><%=EncodeHelper.javaStringToHtmlParagraphe(EncodeHelper.convertHTMLEntities(pub.getDescription(language))) %>
    <% } %>
    
    <% i++; %>
     
<% } %>
<br/>
<c:if test="${rssUrl != null}">
<a href="<c:url value="${rssUrl}" />" class="rss_link"><img src="<c:url value="/util/icons/rss.gif" />" border="0" alt="rss"/></a>
</c:if>
