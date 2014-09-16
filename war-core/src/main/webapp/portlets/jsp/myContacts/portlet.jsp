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

<%@page import="com.stratelia.webactiv.beans.admin.UserDetail" %>
<%@page import="com.silverpeas.directory.model.Member"%>

<%@ include file="../portletImport.jsp"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<portlet:defineObjects/>

<fmt:setLocale value="${sessionScope[SilverSessionController].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle"/>
<view:includePlugin name="messageme"/>
<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
    List<UserDetail> contactsConnected = (List<UserDetail>) pReq.getAttribute("ContactsConnected");
    List<UserDetail> contactsNotConnected = (List<UserDetail>) pReq.getAttribute("ContactsNotConnected");
%>

<%
if (contactsConnected.isEmpty() && contactsNotConnected.isEmpty()) { %>
	<%=portletsBundle.getString("portlets.portlet.myContacts.none") %>
<% } else {
%>
	<div id="portlet-myContact">
	<ul id="listing-portlet-myContact" class="listing ">
<% 
	for (UserDetail contact : contactsConnected) {
	  	Member member = new Member(contact);	
%>			
		<li class="user online">
			<img class="avatar" alt="avatar" src="<%=m_sContext + contact.getAvatar() %>" />
			
      		<span class="userName">
      			<%=(contact.getLastName() + " " + contact.getFirstName()).trim() %>
      			<img src="<%=m_sContext %>/util/icons/connected.png" 
      				alt="<fmt:message key="GML.user.online.for" /> <%=member.getDuration() %>" 
      				title="<fmt:message key="GML.user.online.for" /> <%=member.getDuration() %>"/>
      		</span>
      		
      		<div class="userStatut">
      			<p title="<%=contact.getStatus() %>"><%=contact.getStatus() %></p>
      		</div>
      		
      		<a href="#" title="<fmt:message key="ToContact" />" class="contact-user notification" 
      			rel="<%=contact.getId() %>,<%=contact.getDisplayedName()%>">
      			<img src="<%=m_sContext %>/util/icons/email.gif" 
      				alt="<fmt:message key="ToContact" />"
      				title="<fmt:message key="ToContact" />"/>
      		</a>
      		
      		<a href="#" title="<fmt:message key="tchat" />"  class="accessTchat-user"
      			onclick="javascript:window.open('<%=m_sContext %>/RcommunicationUser/jsp/OpenDiscussion?userId=<%=contact.getId() %>',
   				'popupDiscussion<%=contact.getId() %>','menubar=no, status=no, scrollbars=no, menubar=no, width=600, height=450')">
      			<img src="<%=m_sContext %>/util/icons/talk2user.gif" 
      				alt="<fmt:message key="tchat" />"
      				title="<fmt:message key="tchat" />"/>
      		</a>
      	</li>
<%  } 
	
	for (UserDetail contact : contactsNotConnected) {
%>			
		<li class="user offline">
			<img class="avatar" alt="avatar" src="<%=m_sContext + contact.getAvatar() %>" />
			
			<span class="userName">
      			<%=(contact.getLastName() + " " + contact.getFirstName()).trim() %>
      		</span>
      		
      		<div class="userStatut">
      			<p title="<%=contact.getStatus() %>"><%=contact.getStatus() %></p>
      		</div>
      		
      		<a href="#" title="<fmt:message key="ToContact" />" class="contact-user notification"
      			rel="<%=contact.getId() %>,<%=contact.getDisplayedName()%>">
      			<img src="<%=m_sContext %>/util/icons/email.gif" 
      				alt="<fmt:message key="ToContact" />"
      				title="<fmt:message key="ToContact" />"/>
      		</a>
      	</li>
<%  } 
%>
	</ul>
	<br clear="all" />
	</div>
<%  }
%>
