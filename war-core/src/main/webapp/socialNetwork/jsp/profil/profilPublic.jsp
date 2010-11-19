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
<c:set var="browseContext" value="${requestScope.browseContext}" />
<%--<c:set var="level" value="byGroup" />
 <c:url value="/Rdirectory/Main" var="GroupUrl" >
                <c:param name="level" value="byGroup"></c:param>
            </c:url>--%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="GML" />
<view:setBundle basename="com.silverpeas.directory.multilang.DirectoryBundle" var="DML" />

<%  
	ResourceLocator settings = (ResourceLocator) request.getAttribute("Settings");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

    String language = request.getLocale().getLanguage();
    ResourceLocator multilang = new ResourceLocator("com.silverpeas.socialNetwork.multilang.socialNetworkBundle", language);
    ResourceLocator multilangG = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", language);
    UserFull userFull = (UserFull) request.getAttribute("userFull");
    Member member = (Member) request.getAttribute("Member");

    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>


<html>
  <head>
    <view:looknfeel />
    <script type="text/javascript" src="/silverpeas/util/javaScript/animation.js"></script>
    <script type="text/javascript" src="/silverpeas/util/javaScript/checkForm.js"></script>
    <script language="JavaScript">
	    function OpenPopup(){
	      $("#profileDialog").dialog("option", "title", "<%=member.getUserDetail().getDisplayedName()%>");
	  	  $("#profileDialog").dialog("open");
	    }
	
	    function sendNotification(userId) {
	        var title = stripInitialWhitespace($("#txtTitle").val());
	        var errorMsg = "";
	        if (isWhitespace(title)) {
	            errorMsg = "<fmt:message key="GML.thefield" bundle="${GML}"/>"+ " <fmt:message key="notification.object" bundle="${DML}" />"+ " <fmt:message key="GML.isRequired" bundle="${GML}"/>";
	        }
	        if (errorMsg == "") {
	        	$.getJSON("<%=m_context%>/DirectoryJSON",
	                	{ 
	        				IEFix: new Date().getTime(),
	        				Action: "SendMessage",
	        				Title: $("#txtTitle").val(),
	        				Message: $("#txtMessage").val(),
	        				TargetUserId: <%=member.getId()%>
	                	},
	        			function(data){
	            			if (data.success) {
	                			closeDialog();
	            			} else {
	                			alert(data.error);
	            			}
	        			});
	        } else {
	          window.alert(errorMsg);
	        }
	    }
	
	    function closeDialog() {
	    	$("#profileDialog").dialog("close");
	    	$("#txtTitle").val("");
	    	$("#txtMessage").val("");
	    }
    
      function OpenPopupInvitaion(usersId,name){
        options="directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no, alwaysRaised";
        SP_openWindow('<%=m_context%>/Rinvitation/jsp/invite?Recipient='+usersId, 'strWindowName', '350', '200',options);
      }

      $(document).ready(function(){

	        var dialogOpts = {
	                modal: true,
	                autoOpen: false,
	                height: 250,
	                width: 600
	        };
	
	        $("#profileDialog").dialog(dialogOpts);    //end dialog
    });
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
				<img src="<%=m_context%>/util/icons/online.gif" alt="connected"/> <fmt:message key="directory.connected" bundle="${DML}" /> <%=member.getDuration()%>
			<% } else { %>
            	<img src="<%=m_context%>/util/icons/offline.gif" alt="deconnected"/> <fmt:message key="directory.deconnected" bundle="${DML}" />
            <% } %>
        </p>  
               
	    <!-- action  -->
        <div class="action">
        	<a href="#" class="link invitation" onclick="OpenPopupInvitaion(268,'admin ');"><fmt:message key="notification.sendMessage" bundle="${DML}" /></a>
            <br />
            <a href="#" class="link notification" onclick="OpenPopup()"><fmt:message key="notification.sendMessage" bundle="${DML}" /></a>
        </div> <!-- /action  -->              

        <!-- profilPhoto  -->  
		<div class="profilPhoto">
			<img src="<%=m_context + member.getProfilPhoto()%>" alt="viewUser" class="avatar"/>
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
		<p><fmt:message key="profil.subnav.display" /> : <a class="active" href="#"><fmt:message key="profil.subnav.identity" /></a> <!-- <a href="#">Personnelles</a> <a href="#">Personnelles</a> --></p>
	</div><!-- /sousNav  --> 

	<table width="100%" cellspacing="0" cellpadding="5" border="0">
	<%
		if (userFull != null) {
        	//  récupérer toutes les propriétés de ce User
            String[] properties = userFull.getPropertiesNames();

            String property = null;
            for (int p = 0; p < properties.length; p++) {
	            property = properties[p];
                if (StringUtil.isDefined(userFull.getValue(property)) && settings.getBoolean(property, true)) {
            %>
                <tr>
                  <td class="txtlibform" width="30%"><%= userFull.getSpecificLabel(language, property)%></td>
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
              
</div><!-- /publicProfileContenu  -->   
    </view:window>
    
    
    <div id="profileDialog">
    <view:board>
        <form name="notificationSenderForm" action="SendMessage" method="post">
        	<table>
          <tr>
            <td class="txtlibform">
              <fmt:message key="notification.object" bundle="${DML}" /> :
            </td>
            <td>
              <input type="text" name="txtTitle" id="txtTitle" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" size="50" value=""/>
              <img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" alt="mandatoryField" />
            </td>
          </tr>
          <tr>
            <td class="txtlibform">
              <fmt:message key="notification.message" bundle="${DML}" /> :
            </td>
            <td>
              <textarea name="txtMessage" id="txtMessage" cols="49" rows="4"></textarea>
            </td>
          </tr>
          <tr>
            <td colspan="2">
	    (<img src="<%=m_context%>/util/icons/mandatoryField.gif" width="5" height="5" alt="mandatoryField" /> <fmt:message key="GML.requiredField" bundle="${GML}"/>)
            </td>
          </tr>
          </table>
        </form>
        </view:board>
        <div align="center">
          <%
			ButtonPane buttonPane = gef.getButtonPane();
			buttonPane.addButton((Button) gef.getFormButton("Envoyer", "javascript:sendNotification()", false));
			buttonPane.addButton((Button) gef.getFormButton("Cancel", "javascript:closeDialog()", false));
			out.println(buttonPane.print());
          %>
        </div>
    </div>
    
  </body>
</html>