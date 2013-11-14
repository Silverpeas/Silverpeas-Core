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

<%@ page import="com.stratelia.webactiv.beans.admin.ComponentInstLight" %>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<portlet:defineObjects/>

<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
	Map<String, Collection> mapSpaceApplis = (Map<String, Collection>) pReq.getAttribute("MapApplications");
	Iterator<Map.Entry<String, Collection>> iter = mapSpaceApplis.entrySet().iterator();
%>
<%

if (! iter.hasNext()) { %>
	<%=portletsBundle.getString("portlets.portlet.myLastAccessedAppli.none") %>
<% } else {
	boolean first = true;
  	while (iter.hasNext()) {
    	Map.Entry<String, Collection> entry = iter.next();
    	String spaceId = entry.getKey();
    	Collection<ComponentInstLight> applications = entry.getValue();
  
		for (ComponentInstLight appli : applications) {
	  
			String pathIconAppli = m_sContext + "/util/icons/component/";
			if(appli.isWorkflow()) {
			  pathIconAppli += "processManager";
			} else {
			  pathIconAppli += appli.getName();
			}
			pathIconAppli += "Small.gif";
			
			String url = "";
			String target = "";
			if (URLManager.displayUniversalLinks()) {
		      url = URLManager.getSimpleURL(URLManager.URL_COMPONENT, appli.getId());
		      target ="_top";
		    } else {
		      url = m_sContext + URLManager.getURL(appli.getName(), "useless", appli.getId()) + "Main";
		      target ="MyMain";
		    }
			
			if (!first) {
%>			
<br/><br/>
<% 
			} else {
				first = false;
			}
%>
	<!-- Display component icon -->
	&nbsp;<img src="<%=pathIconAppli%>" border="0" width="15" align="top" alt=""/>&nbsp;
	
	<!-- Display component link -->
	<a href="<%=url%>" target="<%=target%>"><b><%=EncodeHelper.convertHTMLEntities(appli.getLabel(language))%></b></a>
	
	<!-- Display space path -->
	chemin id <%=appli.getDomainFatherId() %> : <%=appli.getPath(" > ") %>
<%  
		} 
  	}
  }
%>
<br/>
