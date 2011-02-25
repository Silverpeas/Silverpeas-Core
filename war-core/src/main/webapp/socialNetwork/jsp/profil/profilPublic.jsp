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
<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager" %>
<%@page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@page import="com.silverpeas.directory.model.Member"%>
<%@page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<%  
	ResourcesWrapper resource = (ResourcesWrapper) request.getAttribute("resources");

    UserFull userFull = (UserFull) request.getAttribute("userFull");
    Member member = (Member) request.getAttribute("Member");

    String m_context = URLManager.getApplicationURL();
%>

<html>
  <head>
    <view:looknfeel />
    <script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="/silverpeas/util/javaScript/checkForm.js"></script>
    <script type="text/javascript">    
      function OpenPopupInvitaion(usersId,name){
        options="directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no, alwaysRaised";
        SP_openWindow('<%=m_context%>/Rinvitation/jsp/invite?Recipient='+usersId, 'strWindowName', '350', '200',options);
      }
    </script>
  </head>
  <body id="publicProfile">
    <view:window>

<!-- profilPublicFiche  -->  
<div id="publicProfileFiche" >
  
	<!-- info  -->           
  	<div class="info tableBoard">
    	<h2 class="userName"><%=member.getFirstName() %> <br /><%=member.getLastName() %></h2>
        <p class="infoConnection">
        	<% if (member.isConnected()) { %>
				<img src="<%=m_context%>/util/icons/online.gif" alt="connected"/> <fmt:message key="GML.user.online.for" /> <%=member.getDuration()%>
			<% } else { %>
            	<img src="<%=m_context%>/util/icons/offline.gif" alt="deconnected"/> <fmt:message key="GML.user.offline" />
            <% } %>
        </p>  
               
	    <!-- action  -->
        <div class="action">
        	<a href="#" class="link invitation" onclick="OpenPopupInvitaion(268,'admin ');"><fmt:message key="notification.sendMessage" /></a>
            <br />
            <a href="#" class="link notification" onclick="initNotification(<%=member.getId() %>,'<%=userFull.getDisplayedName() %>')"><fmt:message key="GML.notification.send" /></a>
        </div> <!-- /action  -->              

        <!-- profilPhoto  -->  
		<div class="profilPhoto">
			<img src="<%=m_context + member.getUserDetail().getAvatar()%>" alt="viewUser" class="avatar"/>
        </div>  
             
        <p class="statut">
        	
        </p>
         
        <br clear="all" />
 	</div><!-- /info  -->          
      
</div><!-- /profilPublicFiche  -->      

<!-- profilPublicContenu  -->   
<div id="publicProfileContenu">

	<!-- sousNav  --> 
	<div class="sousNavBulle">
		<p><fmt:message key="profil.subnav.display" /> : <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a></p>
	</div><!-- /sousNav  --> 

	<div class="tab-content">
	<table width="100%" cellspacing="0" cellpadding="5" border="0">
	<%
		if (userFull != null) {
        	//  récupérer toutes les propriétés de ce User
            String[] properties = userFull.getPropertiesNames();

            String property = null;
            for (int p = 0; p < properties.length; p++) {
	            property = properties[p];
                if (StringUtil.isDefined(userFull.getValue(property)) && resource.getSetting(property, true)) {
            %>
                <tr>
                  <td class="txtlibform" width="30%"><%= userFull.getSpecificLabel(resource.getLanguage(), property)%></td>
                  <td >
                    <%=userFull.getValue(property)%>
                  </td>
                </tr>
    		<%
                }
            }
		}
	%>
	</table>
	</div>              

</div><!-- /publicProfileContenu  -->   
</view:window>
    
    <%@include file="../notificationDialog.jsp" %>
    
  </body>
</html>