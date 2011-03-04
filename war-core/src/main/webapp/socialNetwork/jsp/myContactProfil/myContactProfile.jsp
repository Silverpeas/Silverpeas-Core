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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserFull"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@page import="com.silverpeas.directory.control.DirectorySessionController"%>
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager" %>
<%@page import="com.stratelia.silverpeas.notificationManager.NotificationParameters"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@page import="com.silverpeas.directory.model.Member"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="java.util.List"%>
<c:set var="browseContext" value="${requestScope.browseContext}" />
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%  
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

    String language = request.getLocale().getLanguage();
    UserFull userFull = (UserFull) request.getAttribute("UserFull");
    String view = (String) request.getAttribute("View");
    
    List contacts = (List) request.getAttribute("Contacts");
    int nbContacts = ((Integer) request.getAttribute("ContactsNumber")).intValue();
    boolean showAllContactLink = !contacts.isEmpty();
    
    List commonContacts = (List) request.getAttribute("CommonContacts");
    int nbCommonContacts = ((Integer) request.getAttribute("CommonContactsNumber")).intValue();
    boolean showAllCommonContactLink = !commonContacts.isEmpty();
    
    String m_context = URLManager.getApplicationURL();
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel />
</head>
<body id="myProfile">
<view:window>

<div id="myProfileFiche" >
  
	<div class="info tableBoard">
 		<h2 class="userName"><%=userFull.getFirstName() %> <br /><%=userFull.getLastName() %></h2>
       	<p class="statut">
			<%=userFull.getStatus() %>
        </p>  
        <div class="profilPhoto">
			<img src="<%=m_context + userFull.getAvatar()%>" alt="viewUser" class="avatar"/>
        </div>  
        <br clear="all" />
 	</div>
 		
	<h3><%=nbContacts %> <fmt:message key="myProfile.contacts" /></h3>
	<!-- allContact  -->  
	<div id="allContact">
  	<% 
  		for (int i=0; i<contacts.size(); i++) {
  		  UserDetail contact = (UserDetail) contacts.get(i);
  	%>
		<!-- unContact  -->  
     	<div class="unContact">
        	<div class="profilPhotoContact">
        		<a href="<%=m_context %>/Rprofil/jsp/Main?userId=<%=contact.getId() %>"><img class="avatar" alt="viewUser" src="<%=m_context+contact.getAvatar() %>" /></a>
        	</div>
	        <a href="<%=m_context %>/Rprofil/jsp/Main?userId=<%=contact.getId() %>" class="contactName"><%=contact.getDisplayedName() %></a>
	   	</div> <!-- /unContact  -->
  	<% } %>
    
    <% if (showAllContactLink) { %>
	     <br clear="all" />
	     <a href="<%=m_context %>/Rdirectory/jsp/Main?UserId=<%=userFull.getId() %>" class="link"><fmt:message key="myProfile.contacts.all" /></a>
	     <br clear="all" />  
    <% } %>
	</div><!-- /allContact  -->  
	
	<h3><%=nbCommonContacts %> <fmt:message key="myContactProfile.contacts.common" /></h3>
 	<!-- contactCommun  -->  
 	<div id="contactCommun"> 
  	<% 
  		for (int i=0; i<commonContacts.size(); i++) {
  		  UserDetail contact = (UserDetail) commonContacts.get(i);
  	%>
		<!-- unContact  -->  
	    <div class="unContact">
	    	<div class="profilPhotoContact">
        		<a href="<%=m_context %>/Rprofil/jsp/Main?userId=<%=contact.getId() %>"><img class="avatar" alt="viewUser" src="<%=m_context+contact.getAvatar() %>" /></a>
        	</div>
	        <a href="<%=m_context %>/Rprofil/jsp/Main?userId=<%=contact.getId() %>" class="contactName"><%=contact.getDisplayedName() %></a>
	    </div> <!-- /unContact  -->
	<% } %>
	      
	<% if (showAllCommonContactLink) { %>
		<br clear="all" />
	    <a href="<%=m_context %>/Rdirectory/jsp/CommonContacts?UserId=<%=userFull.getId() %>" class="link"><fmt:message key="myProfile.contacts.all" /></a>
	    <br clear="all" />
	<% } %>
 	</div><!-- /contactCommun  -->    
      
</div>

<div id="publicProfileContenu">

	<fmt:message key="myContactProfile.tab.profile" var="profile" />
	<fmt:message key="myContactProfile.tab.wall" var="wall" />
	<view:tabs>
    	<view:tab label="${wall}" action="<%="Main?userId="+userFull.getId()%>" selected="<%=Boolean.toString("Wall".equals(view)) %>" />
    	<view:tab label="${profile}" action="<%="Infos?userId="+userFull.getId()%>" selected="<%=Boolean.toString("Infos".equals(view)) %>" />
	</view:tabs>
	
	<% if ("Infos".equals(view)) { %>
		<%@include file="myContactProfileTabIdentity.jsp" %>
	<% } else if ("Wall".equals(view)) {
	  	view = MyProfileRoutes.MyWall.toString();
	%>
		<%@include file="../myProfil/myProfileTabWall.jsp" %>
	<% } %>
              
</div>
</view:window>
    
</body>
</html>